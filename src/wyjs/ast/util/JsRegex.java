package wyjs.ast.util;

public abstract class JsRegex {

  public static boolean isIdentifier(String value) {
    return value.matches("^[a-Z$_][a-Z0-9$_]+$");
  }

  public static boolean isNumber(String value) {
    return value.matches("^[+-]*(0x)?\\d+(\\.\\d*)?(e[+-]?\\d+)?")
        || value.matches("^[+-]*(0x)?\\d*\\.\\d+(e[+-]?\\d+)?");
  }

}
