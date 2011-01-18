package wyjs.ast.util;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.expr.JsInvoke;
import wyjs.ast.expr.JsLiteral;
import wyjs.ast.expr.JsVariable;

public abstract class JsHelpers {

  public static JsInvoke debug(JsExpr m) {
    return new JsInvoke(new JsVariable("$debug"), m);
  }
  
  public static JsInvoke in(JsExpr a, JsExpr e) {
    return new JsInvoke(new JsVariable("$in"), e);
  }

  public static JsInvoke intersect(JsExpr a, JsExpr b) {
    return new JsInvoke(new JsVariable("$intersect"), JsLists.wrap(a, b));
  }

  public static JsInvoke subset(JsExpr a, JsExpr b, boolean e) {
    return new JsInvoke(new JsVariable("$subset"), JsLists.wrap(a, b,
        new JsLiteral(e)));
  }

}
