package auth;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import javax.inject.Provider;
import models.Account;
import models.Applicant;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.oidc.profile.creator.OidcProfileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.UserRepository;

/**
 * This class ensures that the OidcProfileCreator that both the AD and IDCS clients use will
 * generate a UatProfile object. This is necessary for merging those accounts with existing accounts
 * - that's not usually needed in web applications which is why we have to write this class - pac4j
 * doesn't come with it. It's abstract because AD and IDCS need slightly different implementations
 * of the two abstract methods.
 */
public abstract class UatProfileAdapter extends OidcProfileCreator {
  protected final ProfileFactory profileFactory;
  protected final Provider<UserRepository> applicantRepositoryProvider;

  private static Logger LOG = LoggerFactory.getLogger(UatProfileAdapter.class);

  public UatProfileAdapter(
      OidcConfiguration configuration,
      OidcClient client,
      ProfileFactory profileFactory,
      Provider<UserRepository> applicantRepositoryProvider) {
    super(configuration, client);
    this.profileFactory = Preconditions.checkNotNull(profileFactory);
    this.applicantRepositoryProvider = applicantRepositoryProvider;
  }

  protected abstract String emailAttributeName();

  protected abstract ImmutableSet<Roles> roles(UatProfile profile, OidcProfile oidcProfile);

  protected abstract void adaptForRole(UatProfile profile, ImmutableSet<Roles> roles);

  /** Merge the two provided profiles into a new UatProfileData. */
  public UatProfileData mergeUatProfile(UatProfile uatProfile, OidcProfile oidcProfile) {
    String emailAddress = oidcProfile.getAttribute(emailAttributeName(), String.class);
    uatProfile.setEmailAddress(emailAddress).join();
    uatProfile.getProfileData().addAttribute(CommonProfileDefinition.EMAIL, emailAddress);
    // Meaning: whatever you signed in with most recently is the role you have.
    ImmutableSet<Roles> roles = roles(uatProfile, oidcProfile);
    for (Roles role : roles) {
      uatProfile.getProfileData().addRole(role.toString());
    }
    adaptForRole(uatProfile, roles);
    return uatProfile.getProfileData();
  }

  /** Create a totally new UAT profile from the provided OidcProfile. */
  public abstract UatProfileData uatProfileFromOidcProfile(OidcProfile oidcProfile);

  @Override
  public Optional<UserProfile> create(
      Credentials cred, WebContext context, SessionStore sessionStore) {
    ProfileUtils profileUtils = new ProfileUtils(sessionStore, profileFactory);
    possiblyModifyConfigBasedOnCred(cred);
    Optional<UserProfile> oidcProfile = super.create(cred, context, sessionStore);

    if (oidcProfile.isEmpty()) {
      LOG.warn("Didn't get a valid profile back from OIDC.");
      return Optional.empty();
    }

    if (!(oidcProfile.get() instanceof OidcProfile)) {
      LOG.warn(
          "Got a profile from OIDC callback but it wasn't an OIDC profile: %s", oidcProfile.get());
      return Optional.empty();
    }

    OidcProfile profile = (OidcProfile) oidcProfile.get();
    // Check if we already have a profile in the database for the user returned to us by OIDC.
    Optional<Applicant> existingApplicant =
        applicantRepositoryProvider
            .get()
            .lookupApplicant(profile.getAttribute(emailAttributeName(), String.class))
            .toCompletableFuture()
            .join();

    // Now we have a three-way merge situation.  We might have
    // 1) an applicant in the database (`existingApplicant`),
    // 2) a guest profile in the browser cookie (`existingProfile`)
    // 3) an OIDC account in the callback from the OIDC server (`profile`).
    // We will merge 1 and 2, if present, into `existingProfile`, then merge in `profile`.

    Optional<UatProfile> existingProfile = profileUtils.currentUserProfile(context);
    if (existingApplicant.isPresent()) {
      if (existingProfile.isEmpty()) {
        // Easy merge case - we have an existing applicant, but no guest profile.
        // This will be the most common.
        existingProfile = Optional.of(profileFactory.wrap(existingApplicant.get()));
      } else {
        // Merge the two applicants and prefer the newer one.
        // For account, use the existing account and ignore the guest account.
        Applicant guestApplicant = existingProfile.get().getApplicant().join();
        Account existingAccount = existingApplicant.get().getAccount();
        Applicant mergedApplicant =
            applicantRepositoryProvider
                .get()
                .mergeApplicants(guestApplicant, existingApplicant.get(), existingAccount)
                .toCompletableFuture()
                .join();
        existingProfile = Optional.of(profileFactory.wrap(mergedApplicant));
      }
    }

    // Now merge in the information sent to us by the OIDC server.
    if (existingProfile.isEmpty()) {
      LOG.debug("Found no existing profile in session cookie.");
      return Optional.of(uatProfileFromOidcProfile(profile));
    } else {
      return Optional.of(mergeUatProfile(existingProfile.get(), profile));
    }
  }

  protected abstract void possiblyModifyConfigBasedOnCred(Credentials cred);
}
