package org.openqa.selenium.locator.sync;

public record WaitResult(
    boolean success,
    long elapsedMillis,
    SyncDiagnostics diagnostics) {}
