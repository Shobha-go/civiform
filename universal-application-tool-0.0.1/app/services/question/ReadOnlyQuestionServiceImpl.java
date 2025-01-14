package services.question;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import models.LifecycleStage;
import models.Question;
import models.Version;
import services.question.exceptions.QuestionNotFoundException;
import services.question.types.EnumeratorQuestionDefinition;
import services.question.types.QuestionDefinition;

public final class ReadOnlyQuestionServiceImpl implements ReadOnlyQuestionService {

  private final ImmutableMap<Long, QuestionDefinition> questionsById;
  private final ImmutableSet<QuestionDefinition> upToDateQuestions;
  private final ActiveAndDraftQuestions activeAndDraftQuestions;

  public ReadOnlyQuestionServiceImpl(Version activeVersion, Version draftVersion) {
    checkNotNull(activeVersion);
    checkState(
        activeVersion.getLifecycleStage().equals(LifecycleStage.ACTIVE),
        "Supposedly active version not ACTIVE");
    checkNotNull(draftVersion);
    checkState(
        draftVersion.getLifecycleStage().equals(LifecycleStage.DRAFT),
        "Supposedly draft version not DRAFT");
    ImmutableMap.Builder<Long, QuestionDefinition> questionIdMap = ImmutableMap.builder();
    ImmutableSet.Builder<QuestionDefinition> upToDateBuilder = ImmutableSet.builder();
    Set<String> namesFoundInDraft = new HashSet<>();
    for (QuestionDefinition qd :
        draftVersion.getQuestions().stream()
            .map(Question::getQuestionDefinition)
            .collect(Collectors.toList())) {
      upToDateBuilder.add(qd);
      questionIdMap.put(qd.getId(), qd);
      namesFoundInDraft.add(qd.getName());
    }
    for (QuestionDefinition qd :
        activeVersion.getQuestions().stream()
            .map(Question::getQuestionDefinition)
            .collect(Collectors.toList())) {

      questionIdMap.put(qd.getId(), qd);
      if (!namesFoundInDraft.contains(qd.getName())) {
        upToDateBuilder.add(qd);
      }
    }
    questionsById = questionIdMap.build();
    upToDateQuestions = upToDateBuilder.build();
    activeAndDraftQuestions = new ActiveAndDraftQuestions(activeVersion, draftVersion);
  }

  @Override
  public ActiveAndDraftQuestions getActiveAndDraftQuestions() {
    return activeAndDraftQuestions;
  }

  @Override
  public ImmutableList<QuestionDefinition> getAllQuestions() {
    return questionsById.values().asList();
  }

  @Override
  public ImmutableList<QuestionDefinition> getUpToDateQuestions() {
    return upToDateQuestions.asList();
  }

  @Override
  public ImmutableList<EnumeratorQuestionDefinition> getUpToDateEnumeratorQuestions() {
    return getUpToDateQuestions().stream()
        .filter(QuestionDefinition::isEnumerator)
        .map(questionDefinition -> (EnumeratorQuestionDefinition) questionDefinition)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public ImmutableList<EnumeratorQuestionDefinition> getAllEnumeratorQuestions() {
    return getAllQuestions().stream()
        .filter(QuestionDefinition::isEnumerator)
        .map(questionDefinition -> (EnumeratorQuestionDefinition) questionDefinition)
        .collect(ImmutableList.toImmutableList());
  }

  // TODO(https://github.com/seattle-uat/civiform/issues/673): delete this when question definitions
  //  don't need paths

  @Override
  public QuestionDefinition getQuestionDefinition(long id) throws QuestionNotFoundException {
    if (questionsById.containsKey(id)) {
      return questionsById.get(id);
    }
    throw new QuestionNotFoundException(id);
  }
}
