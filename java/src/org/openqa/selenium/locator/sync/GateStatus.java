package org.openqa.selenium.locator.sync;

public record GateStatus(
    boolean active,
    boolean satisfied,
    String reason,
    Long becameTrueAtEpochMs,
    Long lastChangedAtEpochMs) {}
