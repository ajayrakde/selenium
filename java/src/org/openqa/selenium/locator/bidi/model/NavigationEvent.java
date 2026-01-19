package org.openqa.selenium.locator.bidi.model;

public record NavigationEvent(
    long tsEpochMs,
    NavEventType type,
    String url,
    String navId) {}
