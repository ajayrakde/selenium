package org.openqa.selenium.locator.bidi.api;

import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiSnapshotBundle;

public interface BidiSignals extends Tracker, AutoCloseable {
  BidiCapability capability();

  NetworkActivityTracker network();

  ConsoleTracker console();

  NavigationTracker navigation();

  BidiSnapshotBundle snapshotAll();

  @Override
  default void close() {
    stop();
  }
}
