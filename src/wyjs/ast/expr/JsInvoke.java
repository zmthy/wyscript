package wyjs.ast.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wyjs.ast.JsFunction;
import wyjs.ast.stmt.JsStmt;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;

/**
 * A Javascript function invocation.
 * 
 * @author Timothy Jones
 */
public class JsInvoke implements JsExpr {

  private final JsExpr value;
  private final List<JsExpr> args = new ArrayList<JsExpr>();

  /**
   * An invocation with no arguments.
   * 
   * @param value The value to invoke.
   */
  public JsInvoke(JsExpr value) {
    this(value, (List<JsExpr>) null);
  }
  
  /**
   * An invocation with a single argument.
   * 
   * @param value The value to invoke.
   * @param arg The argument to pass.
   */
  public JsInvoke(JsExpr value, JsExpr arg) {
    this(value, (List<JsExpr>) null);
    this.args.add(arg);
  }

  /**
   * An invocation with arguments.
   * 
   * @param value The value to invoke.
   * @param args The arguments to pass.
   */
  public JsInvoke(JsExpr value, List<? extends JsExpr> args) {
    this.value = value instanceof JsFunction ? new JsParens(value) : value;

    if (args != null) {
      this.args.addAll(args);
    }
  }

  @Override
  public String compile(JsFormatter ws) {
    return value.compile(ws) + JsLists.compileArgs(args, ws);
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
    value.collectAssignments(assignments);
  }

  /**
   * A helper function to reduce boilerplate.
   * 
   * @param value The value to invoke.
   * @return The generated invocation.
   */
  public static JsInvoke inv(JsExpr value) {
    return new JsInvoke(value);
  }

  /**
   * A helper function for invoking a value's method.
   * 
   * @param value The value who's method is to be invoked.
   * @param method The name of the method to invoke.
   * @return The generated invocation.
   */
  public static JsInvoke invm(JsExpr value, String method) {
    return new JsInvoke(new JsAccess(value, method));
  }

  /**
   * A helper function for invoking an anonymous function.
   * 
   * @param body The body of the anonymous function that will be generated.
   * @return The generated invocation, wrapping the generated function.
   */
  public static JsInvoke cl(List<? extends JsStmt> body) {
    return new JsInvoke(new JsFunctionExpr(body));
  }

}
