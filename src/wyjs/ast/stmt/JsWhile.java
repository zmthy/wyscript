package wyjs.ast.stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;
import wyjs.ast.util.JsRegex;

/**
 * A Javascript <code>while</code> statement.
 * 
 * @author Timothy Jones
 */
public class JsWhile implements JsStmt {

  private final String label;
  private final JsExpr condition;
  private final List<JsStmt> body = new ArrayList<JsStmt>();

  /**
   * A one statement loop.
   * 
   * @param condition The loop condition.
   * @param body The body of the loop.
   */
  public JsWhile(JsExpr condition, JsStmt body) {
    this(condition, body, null);
  }

  /**
   * A basic loop.
   * 
   * @param condition The loop condition.
   * @param body The body of the loop.
   */
  public JsWhile(JsExpr condition, List<JsStmt> body) {
    this(condition, body, null);
  }

  /**
   * A one statement labelled loop.
   * 
   * @param condition The loop condition.
   * @param body The body of the loop.
   * @param label The loop label.
   */
  public JsWhile(JsExpr condition, JsStmt body, String label) {
    this(condition, (List<JsStmt>) null, label);
    this.body.add(body);
  }

  /**
   * A labelled loop.
   * 
   * @param condition The loop condition.
   * @param body The body of the loop.
   * @param label The loop label.
   */
  public JsWhile(JsExpr condition, List<JsStmt> body, String label) {
    if (label != null) {
      assert JsRegex.isIdentifier(label);
    }

    this.condition = condition;
    this.label = label;

    if (body != null) {
      this.body.addAll(body);
    }
  }

  
  public String compile(JsFormatter ws) {
    return ws.idt + (label == null ? "" : label + ":" + ws.s) + "while" + ws.s
        + "(" + condition.compile(ws) + ")" + ws.s + "{" + ws.ln
        + JsLists.compile(body, ws.next()) + ws.idt + "}" + ws.ln;
  }

  
  public void collectAssignments(Set<String> assignments) {
    condition.collectAssignments(assignments);
    for (JsStmt stmt : body) {
      stmt.collectAssignments(assignments);
    }
  }

}
