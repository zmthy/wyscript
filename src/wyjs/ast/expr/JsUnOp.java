package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * Valid Javascript unary operators which have relevance to this translator.
 * 
 * @author Timothy Jones
 */
public enum JsUnOp {
  
  Positive, Negative;
  
  /**
   * Makes a new AST node of the given unary operator.
   * 
   * @param value The value to apply the operation to.
   * @return An AST node of the operation.
   */
  public JsExpr newNode(JsExpr value) {
    return new UN_OP(value);
  }
  
  /**
   * Converts the unary operator into its Javascript representation.
   * 
   * @return The unary operator as it would appear in Javascript.
   */
  public String compile() {
    switch (this) {
    case Negative: return "-";
    default: return "+";
    }
  }
  
  @Override
  public String toString() {
    return compile();
  }
  
  /**
   * The internal nodes for each unary operator.
   * 
   * @author Timothy Jones
   */
  private class UN_OP implements JsExpr {

    private final JsExpr value;
    
    /**
     * @param value The value to apply the operation to.
     */
    public UN_OP(JsExpr value) {
      this.value = value;
    }
    
    @Override
    public String compile(JsFormatter ws) {
      return JsUnOp.this.toString() + value.compile(ws);
    }

    @Override
    public void collectAssignments(Set<String> assignments) {
      value.collectAssignments(assignments);
    }
  
  }

}
