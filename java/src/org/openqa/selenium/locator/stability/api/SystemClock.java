package org.openqa.selenium.locator.stability.api;

public class SystemClock implements Clock {
  @Override
  public long nowEpochMs() {
    return System.currentTimeMillis();
  }
}
