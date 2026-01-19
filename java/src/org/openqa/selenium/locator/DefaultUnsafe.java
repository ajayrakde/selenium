package org.openqa.selenium.locator;

import java.util.Objects;
import org.openqa.selenium.WebDriver;

/**
 * Default unsafe driver access.
 */
public class DefaultUnsafe implements Unsafe {
  private final WebDriver driver;

  public DefaultUnsafe(WebDriver driver) {
    this.driver = Objects.requireNonNull(driver, "driver");
  }

  @Override
  public WebDriver driver() {
    return driver;
  }
}
