package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * A Javascript assignment, to a valid assignable value.
 * 
 * @author Timothy Jones
 */
public class JsAssign implements JsExpr {

  private final JsAssignable lhs;
  private final JsExpr rhs;
  
  /**
   * @param lhs The value to assign to.
   * @param rhs The value to assign.
   */
  public JsAssign(JsAssignable lhs, JsExpr rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  @Override
  public String compile(JsFormatter ws) {
    return lhs.compile(ws) + ws.s + "=" + ws.s + rhs.compile(ws);
  }
  
  @Override
  public void collectAssignments(Set<String> assignments) {
    String lhs = this.lhs.getVar();
    if (lhs != null) {
      assignments.add(lhs);
    }
    rhs.collectAssignments(assignments);
  }
  
  /**
   * A node should implement this interface if it is a valid assignable value.
   * 
   * @author Timothy Jones
   */
  public static interface JsAssignable extends JsExpr {
    
    /**
     * If the value is a local variable, this method must return the name of
     * the variable in order to have its scope correctly set with a
     * <code>var</code> statement. If not, it should return null.
     * 
     * @return The name of the local variable or null.
     */
    public String getVar();
    
  }

}
