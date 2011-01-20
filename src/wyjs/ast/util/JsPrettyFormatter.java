package wyjs.ast.util;

public class JsPrettyFormatter extends JsFormatter {

  private static final String in = "  ";

  private final JsPrettyFormatter prev;
  private JsPrettyFormatter next = null;

  public JsPrettyFormatter() {
    this(null, "");
  }

  private JsPrettyFormatter(JsPrettyFormatter old, String idt) {
    super("\n", "", " ", idt);
    this.prev = old;
  }

  
  public JsFormatter next() {
    if (next == null) {
      return next = new JsPrettyFormatter(this, idt + in);
    }

    return next;
  }

  
  public JsFormatter prev() {
    return prev;
  }

}
