package views.admin.programs;

import static j2html.TagCreator.form;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;

import forms.ProgramForm;
import j2html.tags.ContainerTag;
import services.program.ProgramDefinition;
import views.BaseHtmlView;
import views.components.FieldWithLabel;

/**
 * Builds a program form for rendering. If the program was previously created, the {@code adminName}
 * field is disabled, since it cannot be edited once set.
 */
public class ProgramFormBuilder extends BaseHtmlView {

  /** Builds the form using program form data. */
  public static ContainerTag buildProgramForm(ProgramForm program, boolean editExistingProgram) {
    return buildProgramForm(
        program.getAdminName(),
        program.getAdminDescription(),
        program.getLocalizedDisplayName(),
        program.getLocalizedDisplayDescription(),
        editExistingProgram);
  }

  /** Builds the form using program definition data. */
  public static ContainerTag buildProgramForm(
      ProgramDefinition program, boolean editExistingProgram) {
    return buildProgramForm(
        program.adminName(),
        program.adminDescription(),
        program.localizedName().getDefault(),
        program.localizedDescription().getDefault(),
        editExistingProgram);
  }

  private static ContainerTag buildProgramForm(
      String adminName,
      String adminDescription,
      String displayName,
      String displayDescription,
      boolean editExistingProgram) {
    ContainerTag formTag = form().withMethod("POST");
    formTag.with(
        h2("Internal program information"),
        h3("This will only be visible to administrators"),
        FieldWithLabel.input()
            .setId("program-name-input")
            .setFieldName("adminName")
            .setLabelText("Enter internal name or nickname of this program")
            .setValue(adminName)
            .setDisabled(editExistingProgram)
            .getContainer(),
        FieldWithLabel.textArea()
            .setId("program-description-textarea")
            .setFieldName("adminDescription")
            .setLabelText("Describe this program for administrative use")
            .setValue(adminDescription)
            .getContainer(),
        h2("Public program information"),
        h3("This will be visible to the public"),
        FieldWithLabel.input()
            .setId("program-display-name-input")
            .setFieldName("localizedDisplayName")
            .setLabelText("Enter the publicly displayed name for this program")
            .setValue(displayName)
            .getContainer(),
        FieldWithLabel.textArea()
            .setId("program-display-description-textarea")
            .setFieldName("localizedDisplayDescription")
            .setLabelText("Describe this program for the public")
            .setValue(displayDescription)
            .getContainer(),
        submitButton("Save").withId("program-update-button"));
    return formTag;
  }
}
