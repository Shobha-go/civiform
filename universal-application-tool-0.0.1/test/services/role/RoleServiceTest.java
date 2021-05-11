package services.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import models.Account;
import models.Program;
import org.junit.Before;
import org.junit.Test;
import repository.UserRepository;
import repository.WithPostgresContainer;
import services.CiviFormError;
import services.program.ProgramNotFoundException;
import support.ProgramBuilder;

public class RoleServiceTest extends WithPostgresContainer {

  private UserRepository userRepository;
  private RoleService service;

  @Before
  public void setup() {
    userRepository = instanceOf(UserRepository.class);
    service = instanceOf(RoleService.class);
  }

  @Test
  public void makeProgramAdmins_allPromoted() throws ProgramNotFoundException {
    String email1 = "fake@email.com";
    String email2 = "fake2@email.com";
    Account account1 = new Account();
    account1.setEmailAddress(email1);
    account1.save();
    Account account2 = new Account();
    account2.setEmailAddress(email2);
    account2.save();

    String programName = "test program";
    Program program = ProgramBuilder.newDraftProgram(programName).build();

    Optional<CiviFormError> result =
        service.makeProgramAdmins(program.id, ImmutableSet.of(email1, email2));

    assertThat(result).isEmpty();

    account1 = userRepository.lookupAccount(email1).get();
    account2 = userRepository.lookupAccount(email2).get();

    assertThat(account1.getAdministeredProgramNames()).containsOnly(programName);
    assertThat(account2.getAdministeredProgramNames()).containsOnly(programName);
  }

  @Test
  public void makeProgramAdmins_programNotFound_throwsException() {
    assertThatThrownBy(() -> service.makeProgramAdmins(1234L, ImmutableSet.of()))
        .isInstanceOf(ProgramNotFoundException.class);
  }
}