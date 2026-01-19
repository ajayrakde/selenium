package org.openqa.selenium.locator.sync.selenium.impl;

public record GateCheckResult(
    boolean isTrueNow,
    String reason) {}
