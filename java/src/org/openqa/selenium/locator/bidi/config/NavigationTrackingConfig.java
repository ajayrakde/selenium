package org.openqa.selenium.locator.bidi.config;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.regex.Pattern;

public record NavigationTrackingConfig(
    boolean enabled,
    int ringBufferSize,
    boolean useJsHistoryHook,
    List<Pattern> ignoredUrlPatterns) {
  public NavigationTrackingConfig {
    ignoredUrlPatterns =
        ignoredUrlPatterns == null ? ImmutableList.of() : ImmutableList.copyOf(ignoredUrlPatterns);
  }
}
