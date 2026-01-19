package org.openqa.selenium.locator.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.api.ConsoleTracker;
import org.openqa.selenium.locator.bidi.api.NavigationTracker;
import org.openqa.selenium.locator.bidi.api.NetworkActivityTracker;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.BidiSnapshotBundle;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.sync.policy.ConsolePolicy;
import org.openqa.selenium.locator.sync.policy.Gate;
import org.openqa.selenium.locator.sync.policy.NetworkPolicy;
import org.openqa.selenium.locator.sync.policy.QuietWindows;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.policy.StabilityPolicy;
import org.openqa.selenium.locator.sync.policy.SyncPolicy;
import org.openqa.selenium.locator.sync.selenium.impl.DefaultSyncKernel;

class DefaultSyncKernelTest {
  @Test
  void waitReadyStopsOnSuccess() {
    FakeDriver driver = new FakeDriver("https://example.test", "complete");
    SyncPolicy policy = policyWithUrlStable(Duration.ofMillis(20));
    DefaultSyncKernel kernel = new DefaultSyncKernel(driver, policy, null, new FakeStabilityService());

    WaitResult result = kernel.waitReady(Intent.CLICK, new WaitTargetNone(), WaitOptions.defaults());
    assertThat(result.success()).isTrue();
  }

  @Test
  void waitReadyTimesOutWhenGateNeverSatisfied() {
    FakeDriver driver = new FakeDriver("https://example.test", "complete") {
      private int counter = 0;

      @Override
      public String getCurrentUrl() {
        counter += 1;
        return counter % 2 == 0 ? "https://example.test/a" : "https://example.test/b";
      }
    };
    SyncPolicy policy = policyWithUrlStable(Duration.ofMillis(10));
    DefaultSyncKernel kernel = new DefaultSyncKernel(driver, policy, null, new FakeStabilityService());

    WaitResult result = kernel.waitReady(Intent.CLICK, new WaitTargetNone(), WaitOptions.defaults());
    assertThat(result.success()).isFalse();
  }

  @Test
  void skipsBidiGateWhenUnsupported() {
    FakeDriver driver = new FakeDriver("https://example.test", "complete");
    SyncPolicy policy = policyWithNetworkGate();
    DefaultSyncKernel kernel =
        new DefaultSyncKernel(driver, policy, new UnsupportedBidiSignals(), new FakeStabilityService());

    WaitResult result = kernel.waitReady(Intent.CLICK, new WaitTargetNone(), WaitOptions.defaults());
    assertThat(result.success()).isTrue();
  }

  private SyncPolicy policyWithUrlStable(Duration timeout) {
    Map<Intent, ReadinessProfile> profiles = new EnumMap<>(Intent.class);
    profiles.put(
        Intent.CLICK,
        new ReadinessProfile(
            Set.of(Gate.URL_STABLE),
            new QuietWindows(Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO),
            new ConsolePolicy(false, false, List.of(), List.of()),
            new NetworkPolicy(false, Set.of(), List.of(), true),
            new StabilityPolicy(false, false, 0.5)));
    Map<Intent, Duration> timeouts = new EnumMap<>(Intent.class);
    timeouts.put(Intent.CLICK, timeout);

    return new SyncPolicy() {
      @Override
      public Duration defaultTimeout(Intent intent) {
        return timeouts.get(intent);
      }

      @Override
      public Duration pollInterval() {
        return Duration.ofMillis(1);
      }

      @Override
      public Map<Intent, ReadinessProfile> profiles() {
        return profiles;
      }
    };
  }

  private SyncPolicy policyWithNetworkGate() {
    Map<Intent, ReadinessProfile> profiles = new EnumMap<>(Intent.class);
    profiles.put(
        Intent.CLICK,
        new ReadinessProfile(
            Set.of(Gate.NETWORK_QUIET),
            new QuietWindows(Duration.ZERO, Duration.ofMillis(50), Duration.ZERO, Duration.ZERO, Duration.ZERO),
            new ConsolePolicy(false, false, List.of(), List.of()),
            new NetworkPolicy(true, Set.of(), List.of(), true),
            new StabilityPolicy(false, false, 0.5)));
    Map<Intent, Duration> timeouts = new EnumMap<>(Intent.class);
    timeouts.put(Intent.CLICK, Duration.ofMillis(20));

    return new SyncPolicy() {
      @Override
      public Duration defaultTimeout(Intent intent) {
        return timeouts.get(intent);
      }

      @Override
      public Duration pollInterval() {
        return Duration.ofMillis(1);
      }

      @Override
      public Map<Intent, ReadinessProfile> profiles() {
        return profiles;
      }
    };
  }

  private static class FakeDriver implements WebDriver, JavascriptExecutor {
    private final String url;
    private final String readyState;

    FakeDriver(String url, String readyState) {
      this.url = url;
      this.readyState = readyState;
    }

    @Override
    public String getCurrentUrl() {
      return url;
    }

    @Override
    public String getTitle() {
      return "title";
    }

    @Override
    public Object executeScript(String script, Object... args) {
      if (script.contains("document.readyState")) {
        return readyState;
      }
      return null;
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
      return null;
    }

    @Override
    public void get(String url) {
      throw new UnsupportedOperationException("get");
    }

    @Override
    public java.util.List<WebElement> findElements(By by) {
      throw new UnsupportedOperationException("findElements");
    }

    @Override
    public WebElement findElement(By by) {
      throw new UnsupportedOperationException("findElement");
    }

    @Override
    public String getPageSource() {
      throw new UnsupportedOperationException("getPageSource");
    }

    @Override
    public void close() {
      throw new UnsupportedOperationException("close");
    }

    @Override
    public void quit() {
      throw new UnsupportedOperationException("quit");
    }

    @Override
    public Set<String> getWindowHandles() {
      throw new UnsupportedOperationException("getWindowHandles");
    }

    @Override
    public String getWindowHandle() {
      throw new UnsupportedOperationException("getWindowHandle");
    }

    @Override
    public TargetLocator switchTo() {
      throw new UnsupportedOperationException("switchTo");
    }

    @Override
    public Navigation navigate() {
      throw new UnsupportedOperationException("navigate");
    }

    @Override
    public Options manage() {
      throw new UnsupportedOperationException("manage");
    }
  }

  private static class FakeStabilityService implements StabilityService {
    @Override
    public StabilitySnapshot sample(Object elementHandle) {
      throw new UnsupportedOperationException("sample");
    }

    @Override
    public StabilitySnapshot sample(Object elementHandle, StabilityConfig cfg) {
      throw new UnsupportedOperationException("sample");
    }

    @Override
    public GlobalStabilitySnapshot sampleGlobal() {
      return new GlobalStabilitySnapshot(0L, false, null, "");
    }

    @Override
    public GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg) {
      return sampleGlobal();
    }

    @Override
    public StabilityHistory append(
        StabilityHistory history, StabilitySnapshot snapshot, StabilityConfig cfg) {
      return history;
    }
  }

  private static class UnsupportedBidiSignals implements BidiSignals {
    @Override
    public BidiCapability capability() {
      return new BidiCapability(BidiCapabilityStatus.UNSUPPORTED, "no", Set.of());
    }

    @Override
    public NetworkActivityTracker network() {
      throw new UnsupportedOperationException("network");
    }

    @Override
    public ConsoleTracker console() {
      throw new UnsupportedOperationException("console");
    }

    @Override
    public NavigationTracker navigation() {
      throw new UnsupportedOperationException("navigation");
    }

    @Override
    public BidiSnapshotBundle snapshotAll() {
      throw new UnsupportedOperationException("snapshotAll");
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
      return false;
    }
  }
}
