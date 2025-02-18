package views;

import static j2html.TagCreator.div;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import play.twirl.api.Content;

public class HtmlBundleTest {

  @Test
  public void testSetTitle() {
    HtmlBundle bundle = new HtmlBundle();
    bundle.setTitle("My title");

    Content content = bundle.render();
    assertThat(content.body()).contains("<title>My title</title>");
  }

  @Test
  public void emptyBundleRendersOutline() {
    HtmlBundle bundle = new HtmlBundle();

    Content content = bundle.render();
    assertThat(content.body())
        .contains("<body><header></header><main></main><footer></footer></body>");
  }

  @Test
  public void rendersContentInOrder() {
    HtmlBundle bundle = new HtmlBundle();
    bundle.addMainContent(div("One"));
    bundle.addMainContent(div("Two"));

    Content content = bundle.render();
    assertThat(content.body()).contains("<main><div>One</div><div>Two</div></main>");
  }
}
