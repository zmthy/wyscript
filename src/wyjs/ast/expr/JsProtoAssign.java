package wyjs.ast.expr;

import java.util.List;

import wyjs.ast.stmt.JsLine;
import wyjs.ast.stmt.JsStmt;

/**
 * A helper class for generating an assignment to a Javascript function's
 * prototype.
 * 
 * @author Timothy Jones
 */
public class JsProtoAssign extends JsAssign {

  /**
   * @param lhs
   *          The value whose prototype is to be attached to.
   * @param prop
   *          The name of the property to attach.
   * @param rhs
   *          The value to attach.
   */
  public JsProtoAssign(JsAssignable lhs, String prop, JsExpr rhs) {
    super(new JsAccess(new JsAccess(lhs, "prototype"), prop), rhs);
  }

  /**
   * A helper function to reduce boilerplate.
   * 
   * @param lhs
   *          The value whose prototype is to be attached to.
   * @param prop
   *          The name of the property to attach.
   * @param rhs
   *          The value to attach.
   * @return The generated prototype assignment.
   */
  public static JsProtoAssign pro(JsAssignable lhs, String prop, JsExpr rhs) {
    return new JsProtoAssign(lhs, prop, rhs);
  }

  /**
   * A helper function for attaching an empty method.
   * 
   * @param lhs
   *          The value to attach a method to.
   * @param name
   *          The name of the method.
   * @return The generated assignment as a statement.
   */
  public static JsLine meth(JsAssignable lhs, String name) {
    return new JsLine(pro(lhs, name, new JsFunctionExpr()));
  }

  /**
   * A helper function for attaching a method.
   * 
   * @param lhs
   *          The value to attach a method to.
   * @param name
   *          The name of the method.
   * @param body
   *          The body of the method to attach.
   * @return The generated assignment as a statement.
   */
  public static JsLine meth(JsAssignable lhs, String name,
      List<? extends JsStmt> body) {
    return new JsLine(pro(lhs, name, new JsFunctionExpr(body)));
  }

  /**
   * 
   * @param lhs
   * @param name
   * @param params
   * @param body
   * @return
   */
  public static JsLine meth(JsAssignable lhs, String name, List<String> params,
      List<? extends JsStmt> body) {
    return new JsLine(pro(lhs, name, new JsFunctionExpr(params, body)));
  }

}
