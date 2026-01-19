package org.openqa.selenium.locator;

/**
 * Factory for creating locators with selector governance.
 */
public interface LocatorFactory {
  Locator byTestId(String testId);

  Locator byCss(String css);

  Locator byXpath(String xpath);

  Locator byRole(AriaRole role, String name);

  Locator byLabel(String labelText);

  Locator root();
}
