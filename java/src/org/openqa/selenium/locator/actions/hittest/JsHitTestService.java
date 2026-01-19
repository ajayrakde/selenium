package org.openqa.selenium.locator.actions.hittest;

import java.util.Map;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JsHitTestService implements HitTestService {
  @Override
  public HitTestResult hitTest(WebDriver driver, WebElement element, Point clickPoint) {
    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>)
            ((JavascriptExecutor) driver)
                .executeScript(
                    "var el = arguments[0];"
                        + "var x = arguments[1];"
                        + "var y = arguments[2];"
                        + "var target = document.elementFromPoint(x, y);"
                        + "var hit = !!target && (target === el || el.contains(target));"
                        + "var summary = null;"
                        + "if (target) {"
                        + "  var id = target.id ? ('#' + target.id) : '';"
                        + "  var cls = target.className ? ('.' + target.className) : '';"
                        + "  summary = target.tagName.toLowerCase() + id + cls;"
                        + "}"
                        + "return {hit: hit, summary: summary};",
                    element,
                    clickPoint.getX(),
                    clickPoint.getY());
    boolean hit = Boolean.TRUE.equals(result.get("hit"));
    String summary = result.get("summary") == null ? null : result.get("summary").toString();
    return new HitTestResult(hit, summary);
  }
}
