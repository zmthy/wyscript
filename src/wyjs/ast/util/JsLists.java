package wyjs.ast.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import wyjs.ast.expr.JsExpr;
import wyjs.ast.stmt.JsStmt;

public abstract class JsLists {

  public static <T> List<T> wrap(T... values) {
    List<T> list = new ArrayList<T>();
    for (T value : values) {
      list.add(value);
    }
    return list;
  }

  public static String join(Collection<String> values, String on) {
    if (values == null || values.isEmpty()) {
      return "";
    }

    String out = "";
    Iterator<String> it = values.iterator();
    while (it.hasNext()) {
      out += it.next() + (it.hasNext() ? on : "");
    }

    return out;
  }

  public static String compile(List<? extends JsStmt> nodes, JsFormatter ws) {
    return compile(nodes, ws, "");
  }

  public static String compile(List<? extends JsStmt> nodes, JsFormatter ws,
      String sep) {
    if (nodes == null || nodes.isEmpty()) {
      return "";
    }

    String out = "";
    int length = nodes.size();
    for (int i = 0; i < length; ++i) {
      out += nodes.get(i).compile(ws) + (i < length - 1 ? sep : "");
    }
    return out;
  }

  public static String compileParams(List<String> params, JsFormatter ws) {
    return "(" + join(params, "," + ws.s) + ")";
  }

  public static String compileArgs(List<? extends JsExpr> args, JsFormatter ws) {
    List<String> values = new ArrayList<String>(args.size());
    for (JsExpr expr : args) {
      values.add(expr.compile(ws));
    }

    return "(" + join(values, "," + ws.s) + ")";
  }

}
