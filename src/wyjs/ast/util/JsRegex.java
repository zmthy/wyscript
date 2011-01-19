package wyjs.ast.util;

public abstract class JsRegex {

  public static boolean isIdentifier(String value) {
    return value.matches("^[a-Z$_][a-Z0-9$_]+$");
  }

  public static boolean isNumber(String value) {
    return value.matches("^[+-]*(0x)?\\d+(\\.\\d*)?(e[+-]?\\d+)?")
        || value.matches("^[+-]*(0x)?\\d*\\.\\d+(e[+-]?\\d+)?");
  }

  public static String stringify(String value) {
    char quotes = value.contains("'") && !value.contains("\"") ? '"' : '\'';
    return quotes
        + value.replace("\\", "\\\\").replace(Character.toString(quotes),
            "\\" + quotes) + quotes;
  }
}
