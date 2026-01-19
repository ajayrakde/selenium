package org.openqa.selenium.locator.actions.probe;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface ElementStateProbe {
  ElementState probe(WebDriver driver, WebElement element);
}
