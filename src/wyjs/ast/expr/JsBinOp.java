package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * Valid Javascript binary operators that have relevance to this translator.
 * 
 * @author Timothy JOnes
 */
public enum JsBinOp {

  AND, OR, ADD, SUB, MUL, DIV, EQ, NEQ, LT, GT, LTE, GTE, IOF;

  /**
   * Makes a new AST node of the given binary operator.
   * 
   * @param lhs The left hand side of the operation.
   * @param rhs The right hand side of the operation.
   * @return An AST node of the operation.
   */
  public JsExpr newNode(JsExpr lhs, JsExpr rhs) {
    return new BIN_OP(lhs, rhs);
  }

  /**
   * Converts the binary operator into its Javascript representation.
   * 
   * @return The binary operator as it would appear in Javascript.
   */
  public String compile() {
    switch (this) {
    case AND:
      return "&&";
    case OR:
      return "||";
    case ADD:
      return "+";
    case SUB:
      return "-";
    case MUL:
      return "*";
    case DIV:
      return "/";
    case EQ:
      return "===";
    case NEQ:
      return "!==";
    case LT:
      return "<";
    case GT:
      return ">";
    case LTE:
      return "<=";
    case GTE:
      return ">=";
    case IOF:
      return " instanceof ";
    }
    
    // We can't reach this, but need to satisfy the compiler.
    throw new RuntimeException("Unrecognised binary operator."); 
  }

  @Override
  public String toString() {
    return compile();
  }

  /**
   * A helper function for generating an add node.
   * 
   * @param lhs The left hand side of the operation.
   * @param rhs The right hand side of the operation.
   * @return An AST node of the operation.
   */
  public static JsExpr add(JsExpr lhs, JsExpr rhs) {
    return ADD.newNode(lhs, rhs);
  }

  /**
   * The internal nodes for each binary operator.
   * 
   * @author Timothy Jones
   */
  private class BIN_OP implements JsExpr {

    private final JsExpr lhs, rhs;

    /**
     * @param lhs The left hand side of the operation.
     * @param rhs The right hand side of the operation.
     */
    public BIN_OP(JsExpr lhs, JsExpr rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public String compile(JsFormatter ws) {
      return lhs.compile(ws) + ws.s + JsBinOp.this.compile() + ws.s
          + rhs.compile(ws);
    }

    @Override
    public void collectAssignments(Set<String> assignments) {
      lhs.collectAssignments(assignments);
      rhs.collectAssignments(assignments);
    }

  }

}
