package wyjs.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wyjs.ast.stmt.JsLine;
import wyjs.ast.stmt.JsReturn;
import wyjs.ast.stmt.JsStmt;
import wyjs.ast.stmt.JsVar;
import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;
import wyjs.ast.util.JsRegex;

/**
 * A Javascript function.
 * 
 * Because of IE's memory issues, we can't name a function that is used as an
 * expression. In order for this to correctly fit into the AST, the actual nodes
 * are implemented as either an expression or a statement, to ensure that a
 * function expression cannot have a name.
 * 
 * @author Timothy Jones
 */
public abstract class JsFunction implements JsNode {

  private static final String fn = "function";

  private final String name;
  private final List<String> params = new ArrayList<String>();
  private List<JsStmt> body = new ArrayList<JsStmt>();

  /**
   * An empty function.
   * 
   * Note that if the function has no name, pass <code>null</code>, not the
   * empty string, otherwise it will fail verification.
   * 
   * @param name The name of the function.
   */
  public JsFunction(String name) {
    this(name, null, null);
  }

  /**
   * A function with no parameters.
   * 
   * Note that if the function has no name, pass <code>null</code>, not the
   * empty string, otherwise it will fail verification.
   * 
   * @param name The name of the function.
   * @param body The body of the function.
   */
  public JsFunction(String name, List<? extends JsStmt> body) {
    this(name, null, body);
  }

  /**
   * A function with parameters and a body.
   * 
   * Note that if the function has no name, pass <code>null</code>, not the
   * empty string, otherwise it will fail verification.
   * 
   * @param name The name of the function.
   * @param params The parameters of the function.
   * @param body The body of the function.
   */
  public JsFunction(String name, List<String> params,
      List<? extends JsStmt> body) {
    assert name == null || JsRegex.isIdentifier(name);
    this.name = name;

    if (params != null) {
      this.params.addAll(params);
    }

    if (body != null && !body.isEmpty()) {
      this.body.addAll(body);
      removeRedundantReturn();
      collectVars();
    }
  }

  
  public String compile(JsFormatter ws) {
    JsFormatter next = ws.next();
    return fn
        + (name == null ? ws.s : ws.ss + name)
        + JsLists.compileParams(params, ws)
        + ws.s
        + "{"
        + (body.isEmpty() ? ws.e : ws.ln + JsLists.compile(body, next) + ws.idt)
        + "}";
  }

  /**
   * Removes a <code>return</code> statement from the end of the function if it
   * does not return anything.
   */
  private void removeRedundantReturn() {
    int end = body.size() - 1;
    JsStmt last = body.get(end);
    if (last instanceof JsLine) {
      JsNode node = ((JsLine) last).getNode();
      if (node instanceof JsReturn) {
        if (!((JsReturn) node).hasValue()) {
          body.remove(end);
        }
      }
    }
  }

  /**
   * Collects all assignments to variables in this function, so that it may
   * grant them local scope with a <code>var</code> keyword.
   */
  private void collectVars() {
    Set<String> vars = new HashSet<String>();
    for (JsStmt stmt : body) {
      stmt.collectAssignments(vars);
    }

    body.add(0, new JsVar(vars));
  }

}
