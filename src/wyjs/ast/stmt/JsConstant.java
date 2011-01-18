package wyjs.ast.stmt;

import java.util.Set;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.util.JsFormatter;

/**
 * A Javascript constant declaration.
 * 
 * @author Timothy Jones
 */
public class JsConstant implements JsStmt {

  private final String name;
  private final JsExpr value;

  /**
   * @param name
   *          The name of the constant.
   * @param value
   *          The value to assign to the constant.
   */
  public JsConstant(String name, JsExpr value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public String compile(JsFormatter ws) {
    return ws.idt + "const" + ws.ss + name + ws.s + "=" + ws.s
        + value.compile(ws) + ws.ln;
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
  }

}
