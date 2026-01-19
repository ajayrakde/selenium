package org.openqa.selenium.locator.actions.scroll;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface ScrollManager {
  void scrollIntoView(WebDriver driver, WebElement element, ScrollAlignment alignment);
}
