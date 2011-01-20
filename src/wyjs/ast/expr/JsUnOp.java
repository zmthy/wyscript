package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.util.JsBareFormatter;
import wyjs.ast.util.JsFormatter;

/**
 * Valid Javascript unary operators which have relevance to this translator.
 * 
 * @author Timothy Jones
 */
public enum JsUnOp {

  NOT, NEG, TYPEOF;

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
  public String compile(JsFormatter ws) {
    switch (this) {
    case NOT:
      return "!";
    case NEG:
      return "-";
    case TYPEOF:
      return "typeof" + ws.ss;
    }
    
    // We can't reach this, but need to satisfy the compiler.
    throw new RuntimeException("Unrecognised unary operator.");
  }

  
  public String toString() {
    return compile(new JsBareFormatter());
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

    
    public String compile(JsFormatter ws) {
      return JsUnOp.this.compile(ws) + value.compile(ws);
    }

    
    public void collectAssignments(Set<String> assignments) {
      value.collectAssignments(assignments);
    }

  }
  
  public static JsExpr tof(JsExpr value) {
    return TYPEOF.newNode(value);
  }

}
