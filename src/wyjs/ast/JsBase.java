package wyjs.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wyjs.ast.stmt.JsStmt;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;

public class JsBase implements JsNode {

  // private final JsNode base;
  private final List<JsNode> base = new ArrayList<JsNode>();

  public JsBase(List<JsStmt> children) throws IOException {
    // File file = new File("lib/stdlib.min.js");
    // FileReader reader = new FileReader(file);
    // char[] cbuf = new char[(int) file.length()];
    // reader.read(cbuf);
    // children.add(0, new JsLine(new JsLiteral(new String(cbuf))));
    // this.base = JsInvoke.cl(children);
    base.addAll(children);
  }

  public String compile(JsFormatter ws) {
    return JsLists.compile(base, ws);
  }

  public void collectAssignments(Set<String> assignments) {
    for (JsNode node : base) {
      node.collectAssignments(assignments);
    }
  }

}
