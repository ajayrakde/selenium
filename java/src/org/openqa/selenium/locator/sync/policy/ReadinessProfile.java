package org.openqa.selenium.locator.sync.policy;

import java.util.Set;

public record ReadinessProfile(
    Set<Gate> requiredGates,
    QuietWindows quietWindows,
    ConsolePolicy consolePolicy,
    NetworkPolicy networkPolicy,
    StabilityPolicy stabilityPolicy) {}
