package wyjs.ast.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;

/**
 * A Javascript list literal.
 * 
 * @author Timothy Jones
 */
public class JsList implements JsExpr {

  private final List<JsExpr> values = new ArrayList<JsExpr>();
  
  /**
   * An empty list literal.
   */
  public JsList() {}
  
  /**
   * A basic list literal.
   * 
   * @param values The values to have in the list.
   */
  public JsList(List<? extends JsExpr> values) {
    if (values != null) {
      this.values.addAll(values);
    }
  }
  
  
  public String compile(JsFormatter ws) {
    if (values.isEmpty()) {
      return "[]";
    }
    
    return "[" + ws.s + JsLists.compile(values, ws, "," + ws.s) + ws.s + "]";
  }

  
  public void collectAssignments(Set<String> assignments) {
    for (JsExpr expr : values) {
      expr.collectAssignments(assignments);
    }
  }
  
  public static JsList list() {
    return new JsList();
  }

}
