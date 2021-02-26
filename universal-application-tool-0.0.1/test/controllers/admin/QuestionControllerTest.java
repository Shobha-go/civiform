package controllers.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static play.api.test.CSRFTokenHelper.addCSRFToken;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import models.Question;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Request;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import repository.WithPostgresContainer;
import services.question.QuestionDefinitionBuilder;
import services.question.QuestionType;
import services.question.UnsupportedQuestionTypeException;
import views.html.helper.CSRF;

public class QuestionControllerTest extends WithPostgresContainer {
  private QuestionController controller;

  @Before
  public void setup() {
    controller = app.injector().instanceOf(QuestionController.class);
  }

  @Test
  public void create_addsQuestionDefinition() throws UnsupportedQuestionTypeException {
    buildQuestionsList();
    ImmutableMap.Builder<String, String> formData = ImmutableMap.builder();
    formData
        .put("questionName", "name")
        .put("questionDescription", "desc")
        .put("questionPath", "my.question.path")
        .put("questionType", "TEXT")
        .put("questionText", "Hi mom!")
        .put("questionHelpText", ":-)");
    RequestBuilder requestBuilder = Helpers.fakeRequest().bodyForm(formData.build());
    controller
        .create(requestBuilder.build())
        .thenAccept(
            result -> {
              assertThat(contentAsString(result)).contains("Total Questions: 2");
              assertThat(contentAsString(result)).contains("All Questions");
            });
  }

  @Test
  public void edit_invalidPathRedirectsToNew() throws UnsupportedQuestionTypeException {
    buildQuestionsList();
    Request request = addCSRFToken(Helpers.fakeRequest()).build();
    // Attempts to go to /admin/questions/edit/invalid.path then redirects to /admin/questions/new
    controller
        .edit(request, "invalid.path")
        .thenAccept(
            result -> {
              assertThat(result.status()).isEqualTo(OK);
              assertThat(contentAsString(result)).contains("New Question");
              assertThat(contentAsString(result))
                  .contains(CSRF.getToken(request.asScala()).value());
            });
  }

  @Test
  public void edit_returnsPopulatedForm() throws UnsupportedQuestionTypeException {
    buildQuestionsList();
    Request request = addCSRFToken(Helpers.fakeRequest()).build();
    controller
        .edit(request, "the.ultimate.question")
        .thenAccept(
            result -> {
              assertThat(result.status()).isEqualTo(OK);
              assertThat(contentAsString(result)).contains("Edit Question");
            });
  }

  @Test
  public void index_returnsQuestions() throws UnsupportedQuestionTypeException {
    buildQuestionsList();
    controller
        .index("table")
        .thenAccept(
            result -> {
              assertThat(result.status()).isEqualTo(OK);
              assertThat(result.contentType()).hasValue("text/html");
              assertThat(result.charset()).hasValue("utf-8");
              assertThat(contentAsString(result)).contains("Total Questions: 1");
              assertThat(contentAsString(result)).contains("All Questions");
            });
  }

  @Test
  public void index_withNoQuestions() {
    controller
        .index("table")
        .thenAccept(
            result -> {
              assertThat(result.status()).isEqualTo(OK);
              assertThat(result.contentType()).hasValue("text/html");
              assertThat(result.charset()).hasValue("utf-8");
              assertThat(contentAsString(result)).contains("Total Questions: 0");
              assertThat(contentAsString(result)).contains("All Questions");
            });
  }

  @Test
  public void newOne_returnsExpectedForm() {
    Request request = addCSRFToken(Helpers.fakeRequest()).build();
    Result result = controller.newOne(request);

    assertThat(result.status()).isEqualTo(OK);
    assertThat(contentAsString(result)).contains("New Question");
    assertThat(contentAsString(result)).contains(CSRF.getToken(request.asScala()).value());
  }

  @Test
  public void update_failsGracefully() throws UnsupportedQuestionTypeException {
    // TODO: Update isn't implemented yet, so we just redirect to the questions list for now.
    // https://github.com/seattle-uat/universal-application-tool/issues/103
    buildQuestionsList();
    ImmutableMap.Builder<String, String> formData = ImmutableMap.builder();
    formData
        .put("questionName", "name")
        .put("questionDescription", "desc")
        .put("questionPath", "my.question.path")
        .put("questionType", "TEXT")
        .put("questionText", "Hi mom!")
        .put("questionHelpText", ":-)");
    RequestBuilder requestBuilder = Helpers.fakeRequest().bodyForm(formData.build());
    controller
        .update(requestBuilder.build(), 1L)
        .thenAccept(
            result -> {
              assertThat(contentAsString(result)).contains("Total Questions: 1");
              assertThat(contentAsString(result)).contains("All Questions");
            });
  }

  private void buildQuestionsList() throws UnsupportedQuestionTypeException {
    QuestionDefinitionBuilder builder =
        new QuestionDefinitionBuilder()
            .setVersion(1L)
            .setName("First Question")
            .setDescription("This is the first question.")
            .setPath("the.ultimate.question")
            .setQuestionText(
                ImmutableMap.of(Locale.ENGLISH, "What is the answer to the ultimate question?"))
            .setQuestionHelpText(ImmutableMap.of())
            .setQuestionType(QuestionType.TEXT);
    Question question = new Question(builder.build());
    question.save();
  }
}