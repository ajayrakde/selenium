package org.openqa.selenium.locator.sync.policy;

import java.util.List;
import java.util.regex.Pattern;

public record ConsolePolicy(
    boolean requiredWhenSupported,
    boolean treatWarnAsFailure,
    List<Pattern> readinessDenylist,
    List<Pattern> readinessAllowlist) {}
