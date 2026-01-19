package org.openqa.selenium.locator.bidi.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;

public record NetworkTrackingConfig(
    boolean enabled,
    int ringBufferSize,
    boolean recordFailures,
    Set<NetworkResourceType> ignoredTypes,
    List<Pattern> ignoredUrlPatterns,
    boolean ignoreWebSockets,
    List<Pattern> longPollUrlPatterns) {
  public NetworkTrackingConfig {
    ignoredTypes = ignoredTypes == null ? ImmutableSet.of() : ImmutableSet.copyOf(ignoredTypes);
    ignoredUrlPatterns =
        ignoredUrlPatterns == null ? ImmutableList.of() : ImmutableList.copyOf(ignoredUrlPatterns);
    longPollUrlPatterns =
        longPollUrlPatterns == null ? ImmutableList.of() : ImmutableList.copyOf(longPollUrlPatterns);
  }
}
