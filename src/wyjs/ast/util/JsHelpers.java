package wyjs.ast.util;

import java.util.ArrayList;
import java.util.List;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.expr.JsInvoke;
import wyjs.ast.expr.JsList;
import wyjs.ast.expr.JsLiteral;
import wyjs.ast.expr.JsVariable;

public abstract class JsHelpers {

  public static JsInvoke assertion(JsExpr v) {
    return new JsInvoke(new JsVariable("$assert"), v);
  }

  public static JsInvoke debug(JsExpr m) {
    return new JsInvoke(new JsVariable("$debug"), m);
  }

  public static JsInvoke in(JsExpr a, JsExpr e) {
    return new JsInvoke(new JsVariable("$in"), e);
  }

  public static JsInvoke intersect(JsExpr a, JsExpr b) {
    return new JsInvoke(new JsVariable("$intersect"), JsLists.wrap(a, b));
  }

  public static JsInvoke newMap(List<? extends JsExpr> k,
      List<? extends JsExpr> v) {
    List<JsExpr> args = new ArrayList<JsExpr>();
    args.add(new JsList(k));
    args.add(new JsList(v));
    return new JsInvoke(new JsVariable("$newMap"), args);
  }

  public static JsInvoke newSet(JsExpr... e) {
    return newSet(JsLists.wrap(e));
  }

  public static JsInvoke newSet(List<JsExpr> e) {
    return new JsInvoke(new JsVariable("$newSet"), e);
  }

  public static JsInvoke subset(JsExpr a, JsExpr b, boolean e) {
    return new JsInvoke(new JsVariable("$subset"), JsLists.wrap(a, b,
        new JsLiteral(e)));
  }

}
