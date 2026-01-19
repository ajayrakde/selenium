package org.openqa.selenium.locator.actions.probe;

import java.util.Map;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JsElementStateProbe implements ElementStateProbe {
  @Override
  public ElementState probe(WebDriver driver, WebElement element) {
    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>)
            ((JavascriptExecutor) driver)
                .executeScript(
                    "var el = arguments[0];"
                        + "var rect = el.getBoundingClientRect();"
                        + "var style = window.getComputedStyle(el);"
                        + "var attached = !!el.isConnected;"
                        + "var opacity = parseFloat(style.opacity || '1');"
                        + "var visible = attached && rect.width > 0 && rect.height > 0"
                        + " && style.display !== 'none' && style.visibility !== 'hidden' && opacity > 0.01;"
                        + "var enabled = !el.disabled && el.getAttribute('aria-disabled') !== 'true';"
                        + "var pointerEvents = style.pointerEvents !== 'none';"
                        + "var vw = window.innerWidth;"
                        + "var vh = window.innerHeight;"
                        + "var inViewport = rect.bottom > 0 && rect.right > 0"
                        + " && rect.top < vh && rect.left < vw;"
                        + "return {"
                        + "attached: attached,"
                        + "visible: visible,"
                        + "enabled: enabled,"
                        + "pointerEvents: pointerEvents,"
                        + "inViewport: inViewport,"
                        + "rect: {left: rect.left, top: rect.top, right: rect.right, bottom: rect.bottom,"
                        + "width: rect.width, height: rect.height},"
                        + "viewportWidth: vw,"
                        + "viewportHeight: vh"
                        + "};",
                    element);
    Map<String, Object> rect = castMap(result.get("rect"));
    return new ElementState(
        getBoolean(result, "attached"),
        getBoolean(result, "visible"),
        getBoolean(result, "enabled"),
        getBoolean(result, "pointerEvents"),
        getBoolean(result, "inViewport"),
        new ElementRect(
            getDouble(rect, "left"),
            getDouble(rect, "top"),
            getDouble(rect, "right"),
            getDouble(rect, "bottom"),
            getDouble(rect, "width"),
            getDouble(rect, "height")),
        getDouble(result, "viewportWidth"),
        getDouble(result, "viewportHeight"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> castMap(Object value) {
    return (Map<String, Object>) value;
  }

  private boolean getBoolean(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value instanceof Boolean && (Boolean) value;
  }

  private double getDouble(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return 0;
  }
}
