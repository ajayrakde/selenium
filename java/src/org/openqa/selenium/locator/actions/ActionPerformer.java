package org.openqa.selenium.locator.actions;

import java.time.Duration;
import org.openqa.selenium.Keys;
import org.openqa.selenium.locator.LocatorPlan;

public interface ActionPerformer {
  void click(LocatorPlan plan, ClickOptions options);

  void fill(LocatorPlan plan, String text, Duration timeout);

  void clearAndFill(LocatorPlan plan, String text, Duration timeout);

  void pressKey(LocatorPlan plan, Keys key, Duration timeout);

  void hover(LocatorPlan plan, Duration timeout);

  void check(LocatorPlan plan, Duration timeout);

  void uncheck(LocatorPlan plan, Duration timeout);

  void selectOption(LocatorPlan plan, String text, Duration timeout);
}
