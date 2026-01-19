package org.openqa.selenium.locator.sync.selenium.js;

import org.openqa.selenium.JavascriptExecutor;

public class JsReadyStateProbe {
  private final JavascriptExecutor js;

  public JsReadyStateProbe(JavascriptExecutor js) {
    this.js = js;
  }

  public String readReadyState() {
    try {
      return String.valueOf(js.executeScript("return document.readyState"));
    } catch (Exception ignored) {
      return "unknown";
    }
  }
}
