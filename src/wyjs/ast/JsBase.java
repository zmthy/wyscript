package wyjs.ast;

import java.util.List;
import java.util.Set;

import wyjs.ast.stmt.JsStmt;
import wyjs.ast.util.JsFormatter;

public class JsBase implements JsNode {

  private final JsNode base;

  public JsBase(List<? extends JsStmt> children) {
    this.base = null;
  }

  @Override
  public String compile(JsFormatter ws) {
    return base.compile(ws);
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
    base.collectAssignments(assignments);
  }

}
