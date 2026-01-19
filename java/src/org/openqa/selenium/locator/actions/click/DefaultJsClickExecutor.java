package org.openqa.selenium.locator.actions.click;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DefaultJsClickExecutor implements JsClickExecutor {
  @Override
  public void click(WebDriver driver, WebElement element) {
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
  }
}
