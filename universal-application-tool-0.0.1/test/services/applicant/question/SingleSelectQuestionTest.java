package services.applicant.question;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import models.Applicant;
import org.junit.Before;
import org.junit.Test;
import services.Path;
import services.applicant.ApplicantData;
import services.question.DropdownQuestionDefinition;

public class SingleSelectQuestionTest {

  private static final DropdownQuestionDefinition dropdownQuestionDefinition =
      new DropdownQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          "description",
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"),
          ImmutableListMultimap.of(
              Locale.US,
              "option 1",
              Locale.US,
              "option 2",
              Locale.FRANCE,
              "un",
              Locale.FRANCE,
              "deux"));

  private Applicant applicant;
  private ApplicantData applicantData;

  @Before
  public void setUp() {
    applicant = new Applicant();
    applicantData = applicant.getApplicantData();
  }

  @Test
  public void withEmptyApplicantData() {
    SingleSelectQuestion singleSelectQuestion =
        new SingleSelectQuestion(dropdownQuestionDefinition, applicantData);

    assertThat(singleSelectQuestion.getOptions()).containsOnly("option 1", "option 2");
    assertThat(singleSelectQuestion.hasErrors()).isFalse();
  }

  @Test
  public void withPresentApplicantData() {
    applicantData.putString(dropdownQuestionDefinition.getSelectionPath(), "answer");
    SingleSelectQuestion singleSelectQuestion =
        new SingleSelectQuestion(dropdownQuestionDefinition, applicantData);

    assertThat(singleSelectQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(singleSelectQuestion.hasQuestionErrors()).isFalse();
    assertThat(singleSelectQuestion.getSelectedOptionValue()).hasValue("answer");
  }

  // TODO(https://github.com/seattle-uat/civiform/issues/416): Add a test for validation failures.
}
