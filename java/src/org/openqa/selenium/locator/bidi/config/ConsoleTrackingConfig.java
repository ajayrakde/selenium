package org.openqa.selenium.locator.bidi.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.bidi.model.ConsoleLevel;

public record ConsoleTrackingConfig(
    boolean enabled,
    int ringBufferSize,
    Set<ConsoleLevel> levels,
    int maxTextLength,
    List<Pattern> readinessDenylist,
    List<Pattern> readinessAllowlist) {
  public ConsoleTrackingConfig {
    levels = levels == null ? ImmutableSet.of() : ImmutableSet.copyOf(levels);
    readinessDenylist =
        readinessDenylist == null ? ImmutableList.of() : ImmutableList.copyOf(readinessDenylist);
    readinessAllowlist =
        readinessAllowlist == null ? ImmutableList.of() : ImmutableList.copyOf(readinessAllowlist);
  }
}
