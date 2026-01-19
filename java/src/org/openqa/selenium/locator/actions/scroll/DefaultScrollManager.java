package org.openqa.selenium.locator.actions.scroll;

import java.util.Objects;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DefaultScrollManager implements ScrollManager {
  @Override
  public void scrollIntoView(WebDriver driver, WebElement element, ScrollAlignment alignment) {
    Objects.requireNonNull(driver, "driver");
    Objects.requireNonNull(element, "element");
    Objects.requireNonNull(alignment, "alignment");
    String block = switch (alignment) {
      case CENTER -> "center";
      case NEAREST -> "nearest";
      case TOP -> "start";
      case BOTTOM -> "end";
    };
    ((JavascriptExecutor) driver)
        .executeScript(
            "arguments[0].scrollIntoView({block: arguments[1], inline: 'nearest'});",
            element,
            block);
  }
}
