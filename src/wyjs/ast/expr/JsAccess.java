package wyjs.ast.expr;

import java.util.Set;

import wyjs.ast.expr.JsAssign.JsAssignable;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsRegex;

/**
 * A Javascript property access, either in dot or bracket notation.
 * 
 * @author Timothy Jones
 */
public class JsAccess implements JsAssignable {

  private final JsExpr value;
  private final JsExpr prop;
  private final boolean dot;
  
  /**
   * Access a property. Will use the dot notation if the property name is a
   * valid identifier.
   * 
   * @param value The value to access a property of.
   * @param prop The name of the property to access.
   */
  public JsAccess(JsExpr value, String prop) {
    assert !prop.isEmpty();
    this.value = value;
    
    // Whether the property name is a valid identifier.
    this.dot = JsRegex.isIdentifier(prop);
    
    if (!dot) {
      char escape = prop.contains("'") && !prop.contains("\"") ? '"' : '\'';
      prop = prop.replace("\\", "\\\\");
      prop = prop.replace(Character.toString(escape), "\\" + escape);
      prop = escape + prop + escape;
    }
    this.prop = new JsLiteral(prop);
  }
  
  /**
   * Access a property as an array access. Always uses the bracket notation.
   * 
   * @param value The value to access a property of.
   * @param prop The index of the property to access.
   */
  public JsAccess(JsExpr value, int prop) {
    this.value = value;
    this.prop = new JsLiteral(Integer.toString(prop));
    this.dot = false;
  }
  
  /**
   * Access a property with an unevaluated expression. Always uses bracket
   * notation.
   * 
   * @param value The value to access a property of.
   * @param prop The expression for the property to access.
   */
  public JsAccess(JsExpr value, JsExpr prop) {
    this.value = value;
    this.prop = prop;
    this.dot = false;
  }
  
  @Override
  public String compile(JsFormatter ws) {
    return value.compile(ws) + (dot ? "." : "[") + prop.compile(ws) +
        (dot ? "" : "]");
  }
  
  @Override
  public void collectAssignments(Set<String> assignments) {}
  
  @Override
  public String getVar() {
    return null;
  }
  
  /**
   * A helper function for building accesses.
   * 
   * The first property is accessed from the value, and each subsequent access
   * is from the previous access value.
   * 
   * @param value The initial value to access from.
   * @param props The names to access.
   * @return A outermost value in a chain of accesses.
   */
  public static JsAccess acc(JsExpr value, String... props) {
    assert props.length > 1;
    
    if (props.length == 1) {
      return new JsAccess(value, props[0]);
    }
    
    String[] next = new String[props.length - 1];
    for (int i = 0; i < props.length - 1; ++i) {
      next[i] = props[i];
    }
    
    return new JsAccess(acc(value, next), props[props.length - 1]);
  }

}
