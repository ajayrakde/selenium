package org.openqa.selenium.locator.sync.policy;

import java.time.Duration;

public record QuietWindows(
    Duration urlQuiet,
    Duration networkQuiet,
    Duration consoleQuiet,
    Duration animationsQuiet,
    Duration stabilityQuiet) {}
