package services.question;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import models.Question;
import repository.QuestionRepository;
import repository.VersionRepository;
import services.CiviFormError;
import services.ErrorAnd;
import services.question.exceptions.InvalidUpdateException;
import services.question.types.QuestionDefinition;

public final class QuestionServiceImpl implements QuestionService {

  private final QuestionRepository questionRepository;
  private final Provider<VersionRepository> versionRepositoryProvider;

  @Inject
  public QuestionServiceImpl(
      QuestionRepository questionRepository,
      Provider<VersionRepository> versionRepositoryProvider) {
    this.questionRepository = checkNotNull(questionRepository);
    this.versionRepositoryProvider = checkNotNull(versionRepositoryProvider);
  }

  @Override
  public ErrorAnd<QuestionDefinition, CiviFormError> create(QuestionDefinition questionDefinition) {
    ImmutableSet<CiviFormError> validationErrors = questionDefinition.validate();
    ImmutableSet<CiviFormError> conflictErrors = checkConflicts(questionDefinition);
    ImmutableSet<CiviFormError> errors =
        ImmutableSet.<CiviFormError>builder()
            .addAll(validationErrors)
            .addAll(conflictErrors)
            .build();
    if (!errors.isEmpty()) {
      return ErrorAnd.error(errors);
    }
    Question question = new Question(questionDefinition);
    question.addVersion(versionRepositoryProvider.get().getDraftVersion());
    questionRepository.insertQuestionSync(question);
    return ErrorAnd.of(question.getQuestionDefinition());
  }

  @Override
  public CompletionStage<ReadOnlyQuestionService> getReadOnlyQuestionService() {
    return listQuestionDefinitionsAsync()
        .thenApply(
            questionDefinitions ->
                new ReadOnlyQuestionServiceImpl(
                    versionRepositoryProvider.get().getActiveVersion(),
                    versionRepositoryProvider.get().getDraftVersion()));
  }

  @Override
  public ErrorAnd<QuestionDefinition, CiviFormError> update(QuestionDefinition questionDefinition)
      throws InvalidUpdateException {
    if (!questionDefinition.isPersisted()) {
      throw new InvalidUpdateException("question definition is not persisted");
    }
    ImmutableSet<CiviFormError> validationErrors = questionDefinition.validate();

    Optional<Question> maybeQuestion =
        questionRepository.lookupQuestion(questionDefinition.getId()).toCompletableFuture().join();
    if (maybeQuestion.isEmpty()) {
      throw new InvalidUpdateException(
          String.format("question with id %d does not exist", questionDefinition.getId()));
    }
    Question question = maybeQuestion.get();
    ImmutableSet<CiviFormError> immutableMemberErrors =
        validateQuestionImmutableMembers(question.getQuestionDefinition(), questionDefinition);

    ImmutableSet<CiviFormError> errors =
        ImmutableSet.<CiviFormError>builder()
            .addAll(validationErrors)
            .addAll(immutableMemberErrors)
            .build();
    if (!errors.isEmpty()) {
      return ErrorAnd.error(errors);
    }

    question = questionRepository.updateOrCreateDraft(questionDefinition);
    return ErrorAnd.of(question.getQuestionDefinition());
  }

  private CompletionStage<ImmutableList<QuestionDefinition>> listQuestionDefinitionsAsync() {
    return questionRepository
        .listQuestions()
        .thenApply(
            questions ->
                questions.stream()
                    .map(question -> question.getQuestionDefinition())
                    .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Check for conflicts with other questions. This is to be only used with new questions because
   * questions being updated will likely conflict with themselves, and new versions of previous
   * questions will conflict with their previous versions.
   *
   * <p>Questions conflict if they have the same enumerator id reference and the same question path
   * segment.
   */
  private ImmutableSet<CiviFormError> checkConflicts(QuestionDefinition questionDefinition) {
    Optional<Question> maybeConflict =
        questionRepository.findConflictingQuestion(questionDefinition);
    if (maybeConflict.isPresent()) {
      Question conflict = maybeConflict.get();
      String errorMessage;
      if (questionDefinition.getEnumeratorId().isEmpty()) {
        errorMessage =
            String.format(
                "Question '%s' conflicts with question id: %s",
                questionDefinition.getQuestionPathSegment(), conflict.id);
      } else {
        errorMessage =
            String.format(
                "Question '%s' with Enumerator ID %d conflicts with question id: %d",
                questionDefinition.getQuestionPathSegment(),
                questionDefinition.getEnumeratorId().get(),
                conflict.id);
      }
      return ImmutableSet.of(CiviFormError.of(errorMessage));
    }
    return ImmutableSet.of();
  }

  /**
   * Validates that a question's updates do not change its immutable members.
   *
   * <p>Question immutable members are: name, enumerator id, path, and type.
   */
  private ImmutableSet<CiviFormError> validateQuestionImmutableMembers(
      QuestionDefinition questionDefinition, QuestionDefinition toUpdate) {
    ImmutableSet.Builder<CiviFormError> errors = new ImmutableSet.Builder<>();

    if (!questionDefinition.getName().equals(toUpdate.getName())) {
      errors.add(
          CiviFormError.of(
              String.format(
                  "question names mismatch: %s does not match %s",
                  questionDefinition.getName(), toUpdate.getName())));
    }

    if (!questionDefinition.getEnumeratorId().equals(toUpdate.getEnumeratorId())) {
      errors.add(
          CiviFormError.of(
              String.format(
                  "question enumerator ids mismatch: %s does not match %s",
                  questionDefinition
                      .getEnumeratorId()
                      .map(String::valueOf)
                      .orElse("[no enumerator]"),
                  toUpdate.getEnumeratorId().map(String::valueOf).orElse("[no enumerator]"))));
    }

    if (!questionDefinition.getQuestionPathSegment().equals(toUpdate.getQuestionPathSegment())) {
      errors.add(
          CiviFormError.of(
              String.format(
                  "question path segment mismatch: %s does not match %s",
                  questionDefinition.getQuestionPathSegment(), toUpdate.getQuestionPathSegment())));
    }

    if (!questionDefinition.getQuestionType().equals(toUpdate.getQuestionType())) {
      errors.add(
          CiviFormError.of(
              String.format(
                  "question types mismatch: %s does not match %s",
                  questionDefinition.getQuestionType(), toUpdate.getQuestionType())));
    }
    return errors.build();
  }
}
