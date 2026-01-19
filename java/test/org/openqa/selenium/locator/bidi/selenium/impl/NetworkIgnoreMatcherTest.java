package org.openqa.selenium.locator.bidi.selenium.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.bidi.config.NetworkTrackingConfig;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;

@Tag("UnitTests")
class NetworkIgnoreMatcherTest {
  @Test
  void matchesTypeAndUrlRules() {
    NetworkTrackingConfig config =
        new NetworkTrackingConfig(
            true,
            10,
            true,
            Set.of(NetworkResourceType.IMAGE),
            List.of(Pattern.compile("analytics")),
            true,
            List.of(Pattern.compile("long-poll")));
    NetworkIgnoreMatcher matcher = new NetworkIgnoreMatcher(config);

    assertThat(matcher.shouldIgnore(NetworkResourceType.IMAGE, "https://site/img.png"))
        .isTrue();
    assertThat(matcher.shouldIgnore(NetworkResourceType.XHR, "https://site/analytics/pixel"))
        .isTrue();
    assertThat(matcher.shouldIgnore(NetworkResourceType.WS, "wss://site/ws"))
        .isTrue();
    assertThat(matcher.shouldIgnore(NetworkResourceType.XHR, "https://site/long-poll"))
        .isTrue();
    assertThat(matcher.shouldIgnore(NetworkResourceType.XHR, "https://site/api"))
        .isFalse();
  }
}
