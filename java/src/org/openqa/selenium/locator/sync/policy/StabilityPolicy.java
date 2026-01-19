package org.openqa.selenium.locator.sync.policy;

public record StabilityPolicy(
    boolean requireTargetStability,
    boolean requireNoGlobalAnimations,
    double rectTolerancePx) {}
