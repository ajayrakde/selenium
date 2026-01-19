package org.openqa.selenium.locator.bidi.api;

import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;

public interface NavigationTracker extends Tracker {
  NavigationSnapshot snapshot();
}
