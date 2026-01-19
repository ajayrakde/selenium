package org.openqa.selenium.locator;

import org.openqa.selenium.WebDriver;

/**
 * Unsafe access to the underlying driver for compatibility scenarios.
 */
public interface Unsafe {
  WebDriver driver();
}
