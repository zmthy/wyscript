package wyjs.ast.stmt;

import java.util.Set;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.util.JsFormatter;

/**
 * A Javascript return statement.
 * 
 * @author Timothy Jones
 */
public class JsReturn implements JsStmt {

  private final JsExpr value;

  /**
   * An empty return statement.
   */
  public JsReturn() {
    this(null);
  }

  /**
   * A non-empty return statement.
   * 
   * @param value
   *          The value to return.
   */
  public JsReturn(JsExpr value) {
    this.value = value;
  }

  /**
   * @return If the statement returns a value or not.
   */
  public boolean hasValue() {
    return value == null;
  }

  public String compile(JsFormatter ws) {
    return ws.idt + "return" + (this.value == null ? "" : " " + value.compile(ws))
        + ws.ln;
  }

  public void collectAssignments(Set<String> assignments) {
    value.collectAssignments(assignments);
  }

}
