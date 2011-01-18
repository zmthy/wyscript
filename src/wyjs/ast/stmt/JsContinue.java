package wyjs.ast.stmt;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * A Javascript <code>continue</code> statement.
 * 
 * @author Timothy Jones
 */
public class JsContinue implements JsStmt {

  private final String label;

  /**
   * A continue with no label.
   */
  public JsContinue() {
    this.label = null;
  }

  /**
   * A continue with a label.
   * 
   * @param label The continue's label.
   */
  public JsContinue(String label) {
    this.label = label;
  }

  @Override
  public String compile(JsFormatter ws) {
    return ws.idt + "continue" + (label == null ? "" : ws.ss + label) + ws.ln;
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
  }

}
