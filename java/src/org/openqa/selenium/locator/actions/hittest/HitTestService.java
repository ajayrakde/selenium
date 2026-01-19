package org.openqa.selenium.locator.actions.hittest;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface HitTestService {
  HitTestResult hitTest(WebDriver driver, WebElement element, Point clickPoint);
}
