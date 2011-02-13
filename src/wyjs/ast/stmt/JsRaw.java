package wyjs.ast.stmt;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

/**
 * A raw line of Javascript code, which is simply inlined as is.
 * 
 * @author Timothy Jones
 */
public class JsRaw implements JsStmt {

  private final String content;

  public JsRaw(String content) {
    this.content = content;
  }

  public String compile(JsFormatter ws) {
    return content;
  }

  public void collectAssignments(Set<String> assignments) {}

}
