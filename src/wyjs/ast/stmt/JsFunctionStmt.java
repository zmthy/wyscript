package wyjs.ast.stmt;

import java.util.List;
import java.util.Set;

import wyjs.ast.JsFunction;
import wyjs.ast.util.JsFormatter;

/**
 * A named Javascript function declaration.
 * 
 * @author Timothy Jones
 */
public class JsFunctionStmt extends JsFunction implements JsStmt {

  /**
   * An empty function.
   * 
   * @param name
   *          The name of the function.
   */
  public JsFunctionStmt(String name) {
    this(name, null, null);
  }

  /**
   * A function without parameters.
   * 
   * @param name
   *          The name of the function.
   * @param body
   *          The body of the function.
   */
  public JsFunctionStmt(String name, List<? extends JsStmt> body) {
    this(name, null, body);
  }

  /**
   * A function with parameters and a body.
   * 
   * @param name
   *          The name of the function.
   * @param params
   *          The parameters of the function.
   * @param body
   *          The body of the function.
   */
  public JsFunctionStmt(String name, List<String> params,
      List<? extends JsStmt> body) {
    super(name, params, body);
    assert name != null;
  }

  @Override
  public String compile(JsFormatter ws) {
    return ws.idt + super.compile(ws) + ws.ln;
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
  }

  /**
   * A helper function for reducing boilerplate.
   * 
   * @param name
   *          The name of the function.
   * @return The generated function.
   */
  public static JsFunctionStmt fn(String name) {
    return new JsFunctionStmt(name);
  }

  /**
   * A helper function for reducing boilerplate.
   * 
   * @param name
   *          The name of the function.
   * @param body
   *          The body of the function.
   * @return The generated function.
   */
  public static JsFunctionStmt fn(String name, List<? extends JsStmt> body) {
    return new JsFunctionStmt(name, body);
  }

  /**
   * A helper function for reducing boilerplate.
   * 
   * @param name
   *          The name of the function.
   * @param params
   *          The parameters of the function.
   * @param body
   *          The body of the function.
   * @return The generated function.
   */
  public static JsFunctionStmt fn(String name, List<String> params,
      List<? extends JsStmt> body) {
    return new JsFunctionStmt(name, params, body);
  }

}
