package wyjs.ast.stmt;

import java.util.Set;

import wyjs.ast.JsNode;
import wyjs.ast.expr.JsExpr;
import wyjs.ast.util.JsFormatter;

/**
 * A Javascript line, terminated by a semicolon or a line break (depending on
 * the formatter used). Applies to expressions only, as statements end
 * themselves.
 * 
 * @author Timothy Jones
 */
public class JsLine implements JsStmt {

  private final JsExpr node;

  /**
   * @param node The node to line terminate.
   */
  public JsLine(JsExpr node) {
    this.node = node;
  }

  /**
   * @return The node that is line terminated.
   */
  public JsNode getNode() {
    return node;
  }

  
  public String compile(JsFormatter ws) {
    String content = node.compile(ws);
    if (content.length() == 0) {
      return content;
    }

    return ws.idt + content + ws.lb;
  }

  
  public void collectAssignments(Set<String> assignments) {
    node.collectAssignments(assignments);
  }

  /**
   * A helper function to reduce boilerplate.
   * 
   * @param node The node to line terminate.
   * @return The generated line.
   */
  public static JsLine ln(JsExpr node) {
    return new JsLine(node);
  }

}
