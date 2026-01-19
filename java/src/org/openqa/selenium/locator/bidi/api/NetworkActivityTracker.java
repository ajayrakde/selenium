package org.openqa.selenium.locator.bidi.api;

import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;

public interface NetworkActivityTracker extends Tracker {
  NetworkSnapshot snapshot();
}
