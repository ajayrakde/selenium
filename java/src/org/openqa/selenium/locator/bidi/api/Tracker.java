package org.openqa.selenium.locator.bidi.api;

public interface Tracker {
  void start();

  void stop();

  boolean isRunning();
}
