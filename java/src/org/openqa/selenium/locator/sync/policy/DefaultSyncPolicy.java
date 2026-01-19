package org.openqa.selenium.locator.sync.policy;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;
import org.openqa.selenium.locator.sync.Intent;

public final class DefaultSyncPolicy implements NavModeAwareSyncPolicy {
  private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);

  private final Duration pollInterval;
  private final Map<Intent, ReadinessProfile> profiles;
  private final Map<Intent, Duration> timeouts;
  private final ReadinessProfile navigateSpaProfile;

  public DefaultSyncPolicy(
      Duration pollInterval,
      Map<Intent, ReadinessProfile> profiles,
      Map<Intent, Duration> timeouts,
      ReadinessProfile navigateSpaProfile) {
    this.pollInterval = pollInterval;
    this.profiles = new EnumMap<>(profiles);
    this.timeouts = new EnumMap<>(timeouts);
    this.navigateSpaProfile = navigateSpaProfile;
  }

  public static DefaultSyncPolicy create() {
    Map<Intent, Duration> timeouts = new EnumMap<>(Intent.class);
    timeouts.put(Intent.CLICK, Duration.ofSeconds(5));
    timeouts.put(Intent.TYPE, Duration.ofSeconds(5));
    timeouts.put(Intent.OPEN_MODAL, Duration.ofSeconds(6));
    timeouts.put(Intent.CLOSE_MODAL, Duration.ofSeconds(6));
    timeouts.put(Intent.ASSERT, Duration.ofSeconds(8));
    timeouts.put(Intent.NAVIGATE, Duration.ofSeconds(15));

    ConsolePolicy consoleDisabled =
        new ConsolePolicy(false, false, List.of(), List.of());
    ConsolePolicy consoleOptional =
        new ConsolePolicy(
            false,
            false,
            List.of(
                Pattern.compile("Uncaught", Pattern.CASE_INSENSITIVE),
                Pattern.compile("TypeError", Pattern.CASE_INSENSITIVE),
                Pattern.compile("ReferenceError", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Failed to fetch", Pattern.CASE_INSENSITIVE)),
            List.of());

    NetworkPolicy networkDisabled =
        new NetworkPolicy(
            false,
            Set.of(NetworkResourceType.IMAGE, NetworkResourceType.MEDIA, NetworkResourceType.FONT),
            List.of(
                Pattern.compile("/metrics"),
                Pattern.compile("/telemetry"),
                Pattern.compile("google-analytics", Pattern.CASE_INSENSITIVE),
                Pattern.compile("segment", Pattern.CASE_INSENSITIVE),
                Pattern.compile("mixpanel", Pattern.CASE_INSENSITIVE)),
            true);
    NetworkPolicy networkRequired =
        new NetworkPolicy(
            true,
            Set.of(NetworkResourceType.IMAGE, NetworkResourceType.MEDIA, NetworkResourceType.FONT),
            List.of(
                Pattern.compile("/metrics"),
                Pattern.compile("/telemetry"),
                Pattern.compile("google-analytics", Pattern.CASE_INSENSITIVE),
                Pattern.compile("segment", Pattern.CASE_INSENSITIVE),
                Pattern.compile("mixpanel", Pattern.CASE_INSENSITIVE)),
            true);

    StabilityPolicy interactionStability = new StabilityPolicy(true, true, 0.5);
    StabilityPolicy navStability = new StabilityPolicy(false, true, 0.5);
    StabilityPolicy spaStability = new StabilityPolicy(true, true, 0.5);

    Map<Intent, ReadinessProfile> profiles = new EnumMap<>(Intent.class);
    profiles.put(
        Intent.CLICK,
        new ReadinessProfile(
            Set.of(Gate.TARGET_STABLE, Gate.GLOBAL_ANIMATIONS_QUIET),
            new QuietWindows(
                Duration.ZERO,
                Duration.ZERO,
                Duration.ZERO,
                Duration.ofMillis(200),
                Duration.ofMillis(300)),
            consoleDisabled,
            networkDisabled,
            interactionStability));
    profiles.put(
        Intent.TYPE,
        new ReadinessProfile(
            Set.of(Gate.TARGET_STABLE, Gate.GLOBAL_ANIMATIONS_QUIET),
            new QuietWindows(
                Duration.ZERO,
                Duration.ZERO,
                Duration.ZERO,
                Duration.ofMillis(200),
                Duration.ofMillis(300)),
            consoleDisabled,
            networkDisabled,
            interactionStability));
    profiles.put(
        Intent.OPEN_MODAL,
        new ReadinessProfile(
            Set.of(Gate.TARGET_STABLE, Gate.GLOBAL_ANIMATIONS_QUIET),
            new QuietWindows(
                Duration.ZERO,
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                Duration.ofMillis(250),
                Duration.ofMillis(350)),
            consoleOptional,
            networkDisabled,
            interactionStability));
    profiles.put(
        Intent.CLOSE_MODAL,
        new ReadinessProfile(
            Set.of(Gate.GLOBAL_ANIMATIONS_QUIET, Gate.TARGET_STABLE),
            new QuietWindows(
                Duration.ZERO,
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                Duration.ofMillis(300),
                Duration.ofMillis(400)),
            consoleOptional,
            networkDisabled,
            interactionStability));
    profiles.put(
        Intent.ASSERT,
        new ReadinessProfile(
            Set.of(Gate.URL_STABLE, Gate.GLOBAL_ANIMATIONS_QUIET),
            new QuietWindows(
                Duration.ofMillis(200),
                Duration.ofMillis(500),
                Duration.ofMillis(500),
                Duration.ofMillis(250),
                Duration.ofMillis(400)),
            consoleOptional,
            networkDisabled,
            navStability));
    profiles.put(
        Intent.NAVIGATE,
        new ReadinessProfile(
            Set.of(Gate.DOCUMENT_READY, Gate.URL_STABLE, Gate.NETWORK_QUIET),
            new QuietWindows(
                Duration.ofMillis(300),
                Duration.ofMillis(800),
                Duration.ofMillis(800),
                Duration.ofMillis(250),
                Duration.ofMillis(500)),
            consoleOptional,
            networkRequired,
            navStability));

    ReadinessProfile navigateSpaProfile =
        new ReadinessProfile(
            Set.of(Gate.ROUTE_EVENT_SEEN, Gate.TARGET_STABLE, Gate.GLOBAL_ANIMATIONS_QUIET),
            new QuietWindows(
                Duration.ofMillis(200),
                Duration.ofMillis(800),
                Duration.ofMillis(800),
                Duration.ofMillis(250),
                Duration.ofMillis(500)),
            consoleOptional,
            networkDisabled,
            spaStability);

    return new DefaultSyncPolicy(DEFAULT_POLL_INTERVAL, profiles, timeouts, navigateSpaProfile);
  }

  @Override
  public Duration defaultTimeout(Intent intent) {
    return timeouts.get(intent);
  }

  @Override
  public Duration pollInterval() {
    return pollInterval;
  }

  @Override
  public Map<Intent, ReadinessProfile> profiles() {
    return Map.copyOf(profiles);
  }

  @Override
  public ReadinessProfile profileFor(Intent intent, NavMode navMode) {
    if (intent == Intent.NAVIGATE && navMode == NavMode.SPA) {
      return navigateSpaProfile;
    }
    return profiles.get(intent);
  }
}
