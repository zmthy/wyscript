package wyjs.ast.util;

public class JsBareFormatter extends JsFormatter {

  public JsBareFormatter() {
    super("", ";", "", "");
  }
  
  @Override
  public JsFormatter next() {
    return this;
  }
  
  @Override
  public JsFormatter prev() {
    return this;
  }

}
