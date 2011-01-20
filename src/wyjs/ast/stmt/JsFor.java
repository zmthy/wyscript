package wyjs.ast.stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import wyjs.ast.expr.JsAccess;
import wyjs.ast.expr.JsAssign;
import wyjs.ast.expr.JsExpr;
import wyjs.ast.expr.JsVariable;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;
import wyjs.ast.util.JsRegex;

/**
 * A Javascript <code>for</code> statment, purely for looping over collections.
 * 
 * @author Timothy Jones
 */
public class JsFor implements JsStmt {

  private final JsExpr collection;
  private final String label;
  private final List<JsStmt> body = new ArrayList<JsStmt>();

  /**
   * A one statement loop.
   * 
   * @param var The variable to assign the collection values to.
   * @param collection The collection to iterate over.
   * @param body The body of the loop.
   */
  public JsFor(String var, JsExpr collection, JsStmt body) {
    this(var, collection, body, null);
  }

  /**
   * A basic loop.
   * 
   * @param var The variable to assign the collection values to.
   * @param collection The collection to iterate over.
   * @param body The body of the loop.
   */
  public JsFor(String var, JsExpr collection, List<JsStmt> body) {
    this(var, collection, body, null);
  }

  /**
   * A one statement labelled loop.
   * 
   * @param var The variable to assign the collection values to.
   * @param collection The collection to iterate over.
   * @param body The body of the loop.
   * @param label The loop label.
   */
  public JsFor(String var, JsExpr collection, JsStmt body, String label) {
    this(var, collection, (List<JsStmt>) null, label);
    this.body.add(body);
  }

  /**
   * A labelled loop.
   * 
   * @param var The variable to assign the collection values to.
   * @param collection The collection to iterate over.
   * @param body The body of the loop.
   * @param label The loop label.
   */
  public JsFor(String var, JsExpr collection, List<JsStmt> body, String label) {
    if (label != null) {
      assert JsRegex.isIdentifier(label);
    }

    this.collection = collection;
    this.label = label;

    this.body.add(new JsLine(new JsAssign(new JsVariable(var), new JsAccess(
        new JsVariable("$c"), new JsVariable("$i")))));

    if (body != null) {
      this.body.addAll(body);
    }
  }

  
  public String compile(JsFormatter ws) {
    String[][] assignments = { { "c", collection.compile(ws) }, { "i", "0" },
        { "l", "$c.length" } };

    List<String> values = new ArrayList<String>();
    for (String[] assignment : assignments) {
      values.add("$" + assignment[0] + ws.s + "=" + ws.s + assignment[1]);
    }

    return ws.idt + (label == null ? "" : label + ":" + ws.s) + "for" + ws.s
        + "(" + JsLists.join(values, "," + ws.s) + ";" + ws.s + "$i" + ws.s
        + "<" + ws.s + "$l;" + ws.s + "++$i)" + ws.s + "{" + ws.ln
        + JsLists.compile(body, ws.next()) + ws.idt + "}" + ws.ln;
  }

  
  public void collectAssignments(Set<String> assignments) {
    assignments.add("$c");
    assignments.add("$i");
    assignments.add("$l");
    collection.collectAssignments(assignments);
    for (JsStmt stmt : body) {
      stmt.collectAssignments(assignments);
    }
  }
  
  public static JsFor fora(String var, JsExpr collection, JsStmt body) {
    return new JsFor(var, collection, body);
  }

}
