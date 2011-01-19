package wyjs.ast.stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;

/**
 * A Javascript conditional statement.
 * 
 * @author Timothy Jones
 */
public class JsIfElse implements JsStmt {

  private final JsExpr condition;
  private final List<JsStmt> ifBody = new ArrayList<JsStmt>(),
      elseBody = new ArrayList<JsStmt>();

  public JsIfElse(JsExpr condition, List<JsStmt> ifBody, List<JsStmt> elseBody) {
    this.condition = condition;
    this.ifBody.addAll(ifBody);
    this.elseBody.addAll(elseBody);
  }

  @Override
  public String compile(JsFormatter ws) {
    return "if" + ws.s + "(" + condition.compile(ws) + ")" + ws.s + "{" + ws.ln
        + JsLists.compile(ifBody, ws.next()) + ws.idt + "}" + ws.s + "else"
        + ws.s + "{" + ws.ln + JsLists.compile(elseBody, ws.next()) + ws.idt + "}"
        + ws.ln;
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
    condition.collectAssignments(assignments);
    for (JsStmt stmt : ifBody) {
      stmt.collectAssignments(assignments);
    }
    for (JsStmt stmt : elseBody) {
      stmt.collectAssignments(assignments);
    }
  }

}
