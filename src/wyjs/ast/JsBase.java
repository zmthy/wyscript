package wyjs.ast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import wyjs.ast.expr.JsInvoke;
import wyjs.ast.stmt.JsStmt;
import wyjs.ast.util.JsFormatter;

public class JsBase implements JsNode {

  private final JsNode base;

  public JsBase(List<JsStmt> children) throws IOException {
    File file = new File("lib/stdlib.min.js");
    FileReader reader = new FileReader(file);
    char[] cbuf = new char[(int) file.length()];
    reader.read(cbuf);
    // children.add(0, new JsLine(new JsLiteral(new String(cbuf))));
    this.base = JsInvoke.cl(children);
  }

  
  public String compile(JsFormatter ws) {
    return base.compile(ws);
  }

  
  public void collectAssignments(Set<String> assignments) {
    base.collectAssignments(assignments);
  }

}
