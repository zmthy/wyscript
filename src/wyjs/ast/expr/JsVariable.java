package wyjs.ast.expr;

import wyjs.ast.expr.JsAssign.JsAssignable;

/**
 * A Javascript variable.
 * 
 * Any scope that assigns to this variable will claim it in its local scope,
 * with a <code>var</code> statement.
 * 
 * @author Timothy Jones
 */
public class JsVariable extends JsLiteral implements JsAssignable {
  
  /**
   * @param name The name of the variable.
   */
  public JsVariable(String name) {
    super(name);
  }
  
  @Override
  public String getVar() {
    return getValue();
  }
  
  /**
   * A helper function to reduce boilerplate.
   * 
   * @param name The name of the variable.
   * @return The generated variable.
   */
  public static JsVariable var(String name) {
    return new JsVariable(name);
  }

}
