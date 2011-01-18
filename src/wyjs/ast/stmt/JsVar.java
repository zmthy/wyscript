package wyjs.ast.stmt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;

/**
 * A Javascript <code>var</code> statement.
 * 
 * @author Timothy Jones
 */
public class JsVar implements JsStmt {

  private final Set<String> vars = new HashSet<String>();

  /**
   * @param vars The names of the variable to grant local scope.
   */
  public JsVar(Collection<String> vars) {
    if (vars != null) {
      this.vars.addAll(vars);
    }
  }

  @Override
  public String compile(JsFormatter ws) {
    if (vars.isEmpty()) {
      return "";
    }

    return ws.idt + "var" + ws.ss + JsLists.join(vars, "," + ws.s) + ws.ln;
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
  }

}
