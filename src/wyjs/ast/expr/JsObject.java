package wyjs.ast.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wyjs.ast.util.JsFormatter;
import wyjs.ast.util.JsLists;
import wyjs.ast.util.JsRegex;

/**
 * A Javascript object literal.
 * 
 * @author Timothy Jones
 */
public class JsObject implements JsExpr {

  private final Map<String, JsExpr> values = new HashMap<String, JsExpr>();
  
  /**
   * An empty object literal.
   */
  public JsObject() {}
  
  /**
   * A basic object literal.
   * 
   * @param values The values to place in the object.
   */
  public JsObject(Map<String, JsExpr> values) {
    if (values != null) {
      this.values.putAll(values);
    }
  }
  
  @Override
  public String compile(JsFormatter ws) {
    if (values.isEmpty()) {
      return "{}";
    }
    
    List<String> fields = new ArrayList<String>();
    for (String name : values.keySet()) {
      String value = values.get(name).compile(ws);
      if (!JsRegex.isIdentifier(name)) {
        name = JsRegex.stringify(name);
      }
      fields.add(name + ":" + ws.s + value);
    }
    
    return "{" + ws.s + JsLists.join(fields, "," + ws.s) + ws.s + "}";
  }

  @Override
  public void collectAssignments(Set<String> assignments) {
    for (JsExpr expr : values.values()) {
      expr.collectAssignments(assignments);
    }
  }
  
  public static JsObject obj() {
    return new JsObject();
  }

}
