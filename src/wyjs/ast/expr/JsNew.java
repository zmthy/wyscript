package wyjs.ast.expr;

import java.util.List;

import wyjs.ast.util.JsFormatter;

/**
 * A Javascript <code>new</code> function invocation.
 * 
 * @author Timothy Jones
 */
public class JsNew extends JsInvoke {

  /**
   * An invocation with no arguments.
   * 
   * @param value The value to invoke with <code>new</code>.
   */
  public JsNew(JsExpr value) {
    super(value);
  }

  /**
   * An invocation with arguments.
   * 
   * @param value The value to invoke with <code>new</code>.
   * @param args The arguments to pass.
   */
  public JsNew(JsExpr value, List<? extends JsExpr> args) {
    super(value, args);
  }

  @Override
  public String compile(JsFormatter ws) {
    return "new" + ws.ss + super.compile(ws);
  }

  /**
   * A helper function to reduce boilerplate.
   * 
   * @param value The value to invoke with <code>new</code>.
   * @return The generated invocation.
   */
  public static JsNew invn(JsExpr value) {
    return new JsNew(value);
  }

}
