package org.openqa.selenium.locator.actions.post;

import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class MinimalSettleWaiter implements PostActionWaiter {
  private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(750);

  @Override
  public void minimalSettle(WebDriver driver) {
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            "var done = arguments[arguments.length - 1];"
                + "var fired = false;"
                + "var finish = function() { if (!fired) { fired = true; done(true); } };"
                + "var timer = setTimeout(finish, arguments[0]);"
                + "var raf = window.requestAnimationFrame;"
                + "if (!raf) { clearTimeout(timer); finish(); return; }"
                + "raf(function() { raf(function() { clearTimeout(timer); finish(); }); });",
            DEFAULT_TIMEOUT.toMillis());
  }
}
