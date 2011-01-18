package wyjs.ast.util;

public abstract class JsFormatter {

  public final String ln, lb, s, idt, ss = " ", e = "";

  public JsFormatter(String ln, String lb, String s, String idt) {
    this.ln = ln;
    this.lb = lb + ln;
    this.s = s;
    this.idt = idt;
  }

  public abstract JsFormatter next();

  public abstract JsFormatter prev();

}
