package org.openqa.selenium.locator.stability.model;

public record GlobalStabilitySnapshot(
    long timestampEpochMs,
    boolean animationsSupported,
    Boolean animationsRunning,
    String notes) {}
