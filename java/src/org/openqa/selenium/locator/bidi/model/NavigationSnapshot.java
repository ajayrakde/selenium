package org.openqa.selenium.locator.bidi.model;

import java.util.List;

public record NavigationSnapshot(
    long tsEpochMs,
    String currentUrl,
    long lastActivityEpochMs,
    List<NavigationEvent> lastEvents) {}
