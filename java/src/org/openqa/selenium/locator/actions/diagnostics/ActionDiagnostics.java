package org.openqa.selenium.locator.actions.diagnostics;

import org.openqa.selenium.locator.actions.hittest.HitTestResult;
import org.openqa.selenium.locator.actions.probe.ElementState;

public record ActionDiagnostics(
    String actionType,
    boolean allowJsFallback,
    boolean usedJsFallback,
    int attempts,
    String lastExceptionType,
    String lastExceptionMessage,
    ElementState lastElementState,
    HitTestResult lastHitTest,
    long elapsedMillis,
    int polls) {}
