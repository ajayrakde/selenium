package org.openqa.selenium.locator;

import java.time.Duration;
import org.openqa.selenium.Keys;
import org.openqa.selenium.locator.actions.ClickOptions;

/**
 * A live, immutable locator that represents intent.
 */
public interface Locator {
  String name();

  Locator withName(String name);

  Locator within(Locator container);

  Locator filter(FilterSpec filter);

  Locator nth(int index);

  Locator first();

  Locators all();

  boolean exists(Duration timeout);

  int count(Duration timeout);

  String text(Duration timeout);

  void click(Duration timeout);

  void click();

  void click(ClickOptions options);

  void click(Duration timeout, boolean allowJsFallback);

  void fill(String text, Duration timeout);

  void fill(String text);

  void clearAndFill(String text, Duration timeout);

  void clearAndFill(String text);

  void pressKey(Keys key, Duration timeout);

  void pressKey(Keys key);

  void hover(Duration timeout);

  void hover();

  void check(Duration timeout);

  void check();

  void uncheck(Duration timeout);

  void uncheck();

  void selectOption(String text, Duration timeout);

  void selectOption(String text);
}
