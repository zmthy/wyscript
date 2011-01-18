package wyjs.ast.expr;

import java.util.List;
import java.util.Set;

import wyjs.ast.JsFunction;
import wyjs.ast.stmt.JsStmt;

/**
 * A Javascript function expression. We require the distinction between
 * expression and statement for the sake of IE's memory problems.
 * 
 * @author Timothy Jones
 */
public class JsFunctionExpr extends JsFunction implements JsExpr {

  /**
   * An empty function expression.
   */
  public JsFunctionExpr() {
    this(null, null);
  }

  /**
   * A function expression with the given body.
   * 
   * @param body The body of the function.
   */
  public JsFunctionExpr(List<? extends JsStmt> body) {
    this(null, body);
  }

  /**
   * A function expression with the given parameters and body.
   * 
   * @param params The parameters of the function.
   * @param body The body of the function.
   */
  public JsFunctionExpr(List<String> params, List<? extends JsStmt> body) {
    super(null, params, body);
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
  }

}
