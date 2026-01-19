package org.openqa.selenium.locator;

/**
 * Escapes CSS attribute values for selector generation.
 */
final class CssEscaper {
  private CssEscaper() {}

  static String escapeAttributeValue(String value) {
    StringBuilder escaped = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (ch == '\\' || ch == '"') {
        escaped.append('\\');
      }
      escaped.append(ch);
    }
    return escaped.toString();
  }
}
