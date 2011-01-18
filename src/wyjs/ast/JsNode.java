package wyjs.ast;

import java.util.Set;

import wyjs.ast.util.JsFormatter;

public interface JsNode {

  public String compile(JsFormatter ws);

  public void collectAssignments(Set<String> assignments);

}
