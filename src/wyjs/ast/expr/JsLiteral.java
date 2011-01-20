package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * Any Javascript code not represented by other AST nodes.
 * 
 * Note that this class takes it on faith that the given literal is valid
 * Javascript code.
 * 
 * @author Timothy Jones
 */
public class JsLiteral implements JsExpr {

  private final String literal;

  /**
   * A custom literal value.
   * 
   * @param literal The code to insert.
   */
  public JsLiteral(String literal) {
    this.literal = literal;
  }
  
  /**
   * An integer literal value.
   * 
   * @param literal The value to insert.
   */
  public JsLiteral(int literal) {
    this.literal = Integer.toString(literal);
  }
  
  /**
   * A number literal value.
   * 
   * @param literal The value to insert.
   */
  public JsLiteral(double literal) {
    this.literal = Double.toString(literal);
  }
  
  /**
   * A boolean literal value.
   * 
   * @param literal The value to insert.
   */
  public JsLiteral(boolean literal) {
    this.literal = Boolean.toString(literal);
  }

  /**
   * @return The code of this literal.
   */
  public String getValue() {
    return literal;
  }

  
  public String compile(JsFormatter ws) {
    return literal;
  }

  
  public void collectAssignments(Set<String> assignments) {
  }

  /**
   * A helper function to reduce boilerplate.
   * 
   * @param literal The code to insert.
   * @return The generated literal.
   */
  public static JsLiteral lit(String literal) {
    return new JsLiteral(literal);
  }

}
