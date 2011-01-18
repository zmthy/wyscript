package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * Wraps a Javascript expression in parenthesis.
 * 
 * @author Timothy Jones
 */
public class JsParens implements JsExpr {

  private final JsExpr value;

  /**
   * @param value
   *          The value to wrap in parenthesis.
   */
  public JsParens(JsExpr value) {
    this.value = value;
  }

  @Override
  public String compile(JsFormatter ws) {
    return "(" + value.compile(ws) + ")";
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
    value.collectAssignments(assignments);
  }

}
