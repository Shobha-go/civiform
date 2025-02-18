package views.style;

public final class BaseStyles {

  public static final String LINK_TEXT = Styles.TEXT_BLUE_400;
  public static final String LINK_HOVER_TEXT = StyleUtils.hover(Styles.TEXT_BLUE_500);

  public static final String TABLE_CELL_STYLES = StyleUtils.joinStyles(Styles.PX_4, Styles.PY_2);

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // CiviForm color classes
  /////////////////////////////////////////////////////////////////////////////////////////////////

  public static final String BG_CIVIFORM_WHITE = "bg-civiform-white";

  public static final String BG_SEATTLE_BLUE = "bg-seattle-blue";
  public static final String TEXT_SEATTLE_BLUE = "text-seattle-blue";
  public static final String BORDER_SEATTLE_BLUE = "border-seattle-blue";

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Form style classes
  /////////////////////////////////////////////////////////////////////////////////////////////////

  public static final String FORM_FIELD_MARGIN_BOTTOM = Styles.MB_2;

  public static final String FORM_FIELD_BORDER_COLOR = Styles.BORDER_GRAY_500;
  public static final String FORM_FIELD_ERROR_BORDER_COLOR = Styles.BORDER_RED_600;

  public static final String FORM_LABEL_TEXT_COLOR = Styles.TEXT_GRAY_600;

  public static final String FORM_ERROR_TEXT_COLOR = Styles.TEXT_RED_600;
  public static final String FORM_ERROR_TEXT_XS =
      StyleUtils.joinStyles(BaseStyles.FORM_ERROR_TEXT_COLOR, Styles.TEXT_XS);
  public static final String FORM_ERROR_TEXT_BASE =
      StyleUtils.joinStyles(BaseStyles.FORM_ERROR_TEXT_COLOR, Styles.TEXT_BASE);

  private static final String INPUT_BASE =
      StyleUtils.joinStyles(
          Styles.BLOCK,
          Styles.OUTLINE_NONE,
          Styles.M_AUTO,
          Styles.PX_3,
          Styles.PY_2,
          Styles.BORDER,
          BaseStyles.FORM_FIELD_BORDER_COLOR,
          Styles.ROUNDED_LG,
          Styles.W_FULL,
          Styles.BG_WHITE,
          StyleUtils.focus(BORDER_SEATTLE_BLUE),
          Styles.TEXT_BLACK,
          Styles.TEXT_LG);

  /** For use on `input` elements that are not of type "checkbox" or "radio". */
  public static final String INPUT = StyleUtils.joinStyles(INPUT_BASE, Styles.PLACEHOLDER_GRAY_500);

  /** For use on `label` elements that label non-checkbox and non-radio `input` elements. */
  public static final String INPUT_LABEL =
      StyleUtils.joinStyles(
          Styles.POINTER_EVENTS_NONE,
          BaseStyles.FORM_LABEL_TEXT_COLOR,
          Styles.TEXT_BASE,
          Styles.PX_1,
          Styles.PY_2);

  /**
   * For use on a `label` that labels a checkbox. The label element should contain the checkbox
   * input element and its label text, e.g., <label><input type="checkbox">This is the label
   * text.</label>
   */
  public static final String CHECKBOX_LABEL =
      StyleUtils.joinStyles(INPUT_BASE, Styles.ALIGN_MIDDLE);
  /** Same as the above but for radio buttons. */
  public static final String RADIO_LABEL = CHECKBOX_LABEL;

  /** For use on an `input` of type "checkbox". */
  public static final String CHECKBOX =
      StyleUtils.joinStyles(Styles.H_4, Styles.W_4, Styles.MR_4, Styles.ALIGN_MIDDLE);
  /** For use on an `input` of type "radio". */
  public static final String RADIO = CHECKBOX;
}
