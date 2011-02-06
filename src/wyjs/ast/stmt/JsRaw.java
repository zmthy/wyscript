package wyjs.ast.stmt;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * A raw line of java script code, which is simply inlined as is.
 * 
 * @author Timothy Jones
 */
public class JsRaw implements JsStmt {

  private final String content;

  public JsRaw(String content) {
    this.content = content;
  }

  public String compile(JsFormatter ws) {
    if (content.length() == 0) {
      return content;
    }

    return ws.idt + content + ws.lb;
  }

  public void collectAssignments(Set<String> assignments) {
    // do nout
  }
}
