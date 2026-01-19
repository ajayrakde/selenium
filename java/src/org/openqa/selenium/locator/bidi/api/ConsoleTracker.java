package org.openqa.selenium.locator.bidi.api;

import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;

public interface ConsoleTracker extends Tracker {
  ConsoleSnapshot snapshot();
}
