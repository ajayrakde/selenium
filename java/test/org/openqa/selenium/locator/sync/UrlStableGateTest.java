package org.openqa.selenium.locator.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.StubDriver;
import org.openqa.selenium.locator.sync.policy.ConsolePolicy;
import org.openqa.selenium.locator.sync.policy.Gate;
import org.openqa.selenium.locator.sync.policy.NetworkPolicy;
import org.openqa.selenium.locator.sync.policy.QuietWindows;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.policy.StabilityPolicy;
import org.openqa.selenium.locator.sync.selenium.gates.UrlStableGate;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;

class UrlStableGateTest {
  @Test
  void tracksUrlChanges() {
    UrlDriver driver = new UrlDriver("https://example.test");
    UrlStableGate gate = new UrlStableGate(driver);
    ReadinessProfile profile = profile();

    GateCheckResult first = gate.check(Intent.CLICK, new WaitTargetNone(), profile, 1000L);
    assertThat(first.isTrueNow()).isFalse();

    GateCheckResult stable = gate.check(Intent.CLICK, new WaitTargetNone(), profile, 1100L);
    assertThat(stable.isTrueNow()).isTrue();

    driver.setUrl("https://example.test/next");
    GateCheckResult changed = gate.check(Intent.CLICK, new WaitTargetNone(), profile, 1200L);
    assertThat(changed.isTrueNow()).isFalse();
  }

  private ReadinessProfile profile() {
    return new ReadinessProfile(
        Set.of(Gate.URL_STABLE),
        new QuietWindows(Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO),
        new ConsolePolicy(false, false, java.util.List.of(), java.util.List.of()),
        new NetworkPolicy(false, java.util.Set.of(), java.util.List.of(), true),
        new StabilityPolicy(false, false, 0.5));
  }

  private static class UrlDriver extends StubDriver {
    private String url;

    UrlDriver(String url) {
      this.url = url;
    }

    void setUrl(String url) {
      this.url = url;
    }

    @Override
    public String getCurrentUrl() {
      return url;
    }
  }
}
