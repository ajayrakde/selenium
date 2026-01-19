package org.openqa.selenium.locator.sync.policy;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;

public record NetworkPolicy(
    boolean requiredWhenSupported,
    Set<NetworkResourceType> ignoredTypes,
    List<Pattern> ignoredUrlPatterns,
    boolean ignoreWebSockets) {}
