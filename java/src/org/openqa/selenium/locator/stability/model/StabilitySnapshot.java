package org.openqa.selenium.locator.stability.model;

public record StabilitySnapshot(
    long timestampEpochMs,
    Rect rect,
    VisibilityFlags visibility,
    Boolean animationsRunning,
    String notes) {}
