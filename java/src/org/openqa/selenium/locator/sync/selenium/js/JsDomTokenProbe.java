package org.openqa.selenium.locator.sync.selenium.js;

import org.openqa.selenium.JavascriptExecutor;

public class JsDomTokenProbe {
  private final JavascriptExecutor js;

  public JsDomTokenProbe(JavascriptExecutor js) {
    this.js = js;
  }

  public String readToken(Object rootElement) {
    try {
      return String.valueOf(
          js.executeScript(
              "var root = arguments[0] || document.body;"
                  + "var count = root ? root.childElementCount : 0;"
                  + "return document.title + '|' + location.href + '|' + count;",
              rootElement));
    } catch (Exception ignored) {
      return null;
    }
  }
}
