package services.applicant;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.Locale;
import models.Applicant;
import org.junit.Before;
import org.junit.Test;
import services.Path;
import services.question.AddressQuestionDefinition;
import services.question.NameQuestionDefinition;
import services.question.NumberQuestionDefinition;
import services.question.QuestionDefinitionBuilder;
import services.question.QuestionType;
import services.question.TextQuestionDefinition;
import services.question.TextQuestionDefinition.TextValidationPredicates;

public class ApplicantQuestionTest {

  private static final TextQuestionDefinition textQuestionDefinition =
      new TextQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          "description",
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final NameQuestionDefinition nameQuestionDefinition =
      new NameQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          "description",
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final NumberQuestionDefinition numberQuestionDefinition =
      new NumberQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          "description",
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final AddressQuestionDefinition addressQuestionDefinition =
      new AddressQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          "description",
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));

  private Applicant applicant;
  private ApplicantData applicantData;

  @Before
  public void setUp() {
    applicant = new Applicant();
    applicantData = applicant.getApplicantData();
  }

  @Test
  public void textQuestion_withEmptyApplicantData() {
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(textQuestionDefinition, applicantData);

    assertThat(applicantQuestion.getTextQuestion())
        .isInstanceOf(ApplicantQuestion.TextQuestion.class);
    assertThat(applicantQuestion.getQuestionText()).isEqualTo("question?");
    assertThat(applicantQuestion.hasErrors()).isFalse();
  }

  @Test
  public void textQuestion_withPresentApplicantData() {
    applicantData.putString(textQuestionDefinition.getTextPath(), "hello");
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(textQuestionDefinition, applicantData);
    ApplicantQuestion.TextQuestion textQuestion = applicantQuestion.getTextQuestion();

    assertThat(textQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(textQuestion.getTextValue().get()).isEqualTo("hello");
  }

  @Test
  public void textQuestion_withPresentApplicantData_failsValidation() throws Exception {
    TextQuestionDefinition question =
        (TextQuestionDefinition)
            new QuestionDefinitionBuilder()
                .setQuestionType(QuestionType.TEXT)
                .setVersion(1L)
                .setName("question name")
                .setPath(Path.create("applicant.my.path.name"))
                .setDescription("description")
                .setQuestionText(ImmutableMap.of(Locale.US, "question?"))
                .setQuestionHelpText(ImmutableMap.of(Locale.US, "help text"))
                .setValidationPredicates(TextValidationPredicates.create(0, 4))
                .build();
    applicantData.putString(question.getTextPath(), "hello");
    ApplicantQuestion applicantQuestion = new ApplicantQuestion(question, applicantData);
    ApplicantQuestion.TextQuestion textQuestion = applicantQuestion.getTextQuestion();

    assertThat(applicantQuestion.hasErrors()).isTrue();
    assertThat(textQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(textQuestion.getQuestionErrors())
        .containsOnly(ValidationErrorMessage.textTooLongError(4));
    assertThat(textQuestion.getTextValue().get()).isEqualTo("hello");
  }

  @Test
  public void numberQuestion_withEmptyApplicantData() {
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(numberQuestionDefinition, applicantData);

    assertThat(applicantQuestion.getNumberQuestion())
        .isInstanceOf(ApplicantQuestion.NumberQuestion.class);
    assertThat(applicantQuestion.getQuestionText()).isEqualTo("question?");
    assertThat(applicantQuestion.hasErrors()).isFalse();
  }

  @Test
  public void numberQuestion_withPresentApplicantData() {
    applicantData.putLong(numberQuestionDefinition.getNumberPath(), 800);
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(numberQuestionDefinition, applicantData);
    ApplicantQuestion.NumberQuestion numberQuestion = applicantQuestion.getNumberQuestion();

    assertThat(numberQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(numberQuestion.getNumberValue().get()).isEqualTo(800);
  }

  @Test
  public void numberQuestion_withPresentApplicantData_failsValidation() throws Exception {
    NumberQuestionDefinition question =
        (NumberQuestionDefinition)
            new QuestionDefinitionBuilder()
                .setQuestionType(QuestionType.NUMBER)
                .setVersion(1L)
                .setName("question name")
                .setPath(Path.create("applicant.my.path.name"))
                .setDescription("description")
                .setQuestionText(ImmutableMap.of(Locale.US, "question?"))
                .setQuestionHelpText(ImmutableMap.of(Locale.US, "help text"))
                .setValidationPredicates(
                    NumberQuestionDefinition.NumberValidationPredicates.create(0, 100))
                .build();
    applicantData.putLong(question.getNumberPath(), 1000000);
    ApplicantQuestion applicantQuestion = new ApplicantQuestion(question, applicantData);
    ApplicantQuestion.NumberQuestion numberQuestion = applicantQuestion.getNumberQuestion();

    assertThat(applicantQuestion.hasErrors()).isTrue();
    assertThat(numberQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(numberQuestion.getQuestionErrors())
        .containsOnly(ValidationErrorMessage.numberTooLargeError(100));
    assertThat(numberQuestion.getNumberValue().get()).isEqualTo(1000000);
  }

  @Test
  public void nameQuestion_withEmptyApplicantData() {
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(nameQuestionDefinition, applicantData);

    assertThat(applicantQuestion.getNameQuestion())
        .isInstanceOf(ApplicantQuestion.NameQuestion.class);
    assertThat(applicantQuestion.getQuestionText()).isEqualTo("question?");
    assertThat(applicantQuestion.hasErrors()).isFalse();
  }

  @Test
  public void nameQuestion_withInvalidApplicantData() {
    applicantData.putString(nameQuestionDefinition.getFirstNamePath(), "");
    applicantData.putString(nameQuestionDefinition.getLastNamePath(), "");
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(nameQuestionDefinition, applicantData);
    ApplicantQuestion.NameQuestion nameQuestion = applicantQuestion.getNameQuestion();

    assertThat(applicantQuestion.hasErrors()).isTrue();
    assertThat(nameQuestion.getFirstNameErrors())
        .contains(ValidationErrorMessage.create("First name is required."));
    assertThat(nameQuestion.getLastNameErrors())
        .contains(ValidationErrorMessage.create("Last name is required."));
  }

  @Test
  public void nameQuestion_withValidApplicantData() {
    applicantData.putString(nameQuestionDefinition.getFirstNamePath(), "Wendel");
    applicantData.putString(nameQuestionDefinition.getLastNamePath(), "Patrick");
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(nameQuestionDefinition, applicantData);
    ApplicantQuestion.NameQuestion nameQuestion = applicantQuestion.getNameQuestion();

    assertThat(applicantQuestion.hasErrors()).isFalse();
    assertThat(nameQuestion.getFirstNameValue().get()).isEqualTo("Wendel");
    assertThat(nameQuestion.getLastNameValue().get()).isEqualTo("Patrick");
  }

  @Test
  public void addressQuestion_withEmptyApplicantData() {
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(addressQuestionDefinition, applicantData);

    assertThat(applicantQuestion.getAddressQuestion())
        .isInstanceOf(ApplicantQuestion.AddressQuestion.class);
    assertThat(applicantQuestion.getQuestionText()).isEqualTo("question?");
    assertThat(applicantQuestion.hasErrors()).isFalse();
  }

  @Test
  public void addressQuestion_withInvalidApplicantData() {
    applicantData.putString(addressQuestionDefinition.getStreetPath(), "");
    applicantData.putString(addressQuestionDefinition.getCityPath(), "");
    applicantData.putString(addressQuestionDefinition.getStatePath(), "");
    applicantData.putString(addressQuestionDefinition.getZipPath(), "");

    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(addressQuestionDefinition, applicantData);
    ApplicantQuestion.AddressQuestion addressQuestion = applicantQuestion.getAddressQuestion();

    assertThat(applicantQuestion.hasErrors()).isTrue();
    assertThat(addressQuestion.getStreetErrors())
        .contains(ValidationErrorMessage.create("Street is required."));
    assertThat(addressQuestion.getCityErrors())
        .contains(ValidationErrorMessage.create("City is required."));
    assertThat(addressQuestion.getStateErrors())
        .contains(ValidationErrorMessage.create("State is required."));
    assertThat(addressQuestion.getZipErrors())
        .contains(ValidationErrorMessage.create("Zip code is required."));

    applicantData.putString(addressQuestionDefinition.getZipPath(), "not a zip code");
    addressQuestion =
        new ApplicantQuestion(addressQuestionDefinition, applicantData).getAddressQuestion();

    assertThat(addressQuestion.getZipErrors())
        .contains(ValidationErrorMessage.create("Invalid zip code."));
  }

  @Test
  public void addressQuestion_withValidApplicantData() {
    applicantData.putString(addressQuestionDefinition.getStreetPath(), "85 Pike St");
    applicantData.putString(addressQuestionDefinition.getCityPath(), "Seattle");
    applicantData.putString(addressQuestionDefinition.getStatePath(), "WA");
    applicantData.putString(addressQuestionDefinition.getZipPath(), "98101");

    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(addressQuestionDefinition, applicantData);
    ApplicantQuestion.AddressQuestion addressQuestion = applicantQuestion.getAddressQuestion();

    assertThat(applicantQuestion.hasErrors()).isFalse();
    assertThat(addressQuestion.getStreetValue().get()).isEqualTo("85 Pike St");
    assertThat(addressQuestion.getCityValue().get()).isEqualTo("Seattle");
    assertThat(addressQuestion.getStateValue().get()).isEqualTo("WA");
    assertThat(addressQuestion.getZipValue().get()).isEqualTo("98101");
  }

  @Test
  public void equals() {
    ApplicantData dataWithAnswers = new ApplicantData();
    dataWithAnswers.putString(Path.create("applicant.color"), "blue");

    new EqualsTester()
        .addEqualityGroup(
            new ApplicantQuestion(textQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(textQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(textQuestionDefinition, dataWithAnswers),
            new ApplicantQuestion(textQuestionDefinition, dataWithAnswers))
        .addEqualityGroup(
            new ApplicantQuestion(addressQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(addressQuestionDefinition, new ApplicantData()))
        .testEquals();
  }
}