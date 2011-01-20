package wyjs.ast.util;

public class JsBareFormatter extends JsFormatter {

  public JsBareFormatter() {
    super("", ";", "", "");
  }

  
  public JsFormatter next() {
    return this;
  }

  
  public JsFormatter prev() {
    return this;
  }

}
