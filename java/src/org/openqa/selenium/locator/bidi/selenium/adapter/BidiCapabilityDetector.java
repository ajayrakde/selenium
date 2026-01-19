package org.openqa.selenium.locator.bidi.selenium.adapter;

import java.util.EnumSet;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.module.BrowsingContextInspector;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;

public final class BidiCapabilityDetector {

  public BidiCapability detect(WebDriver driver) {
    if (!(driver instanceof HasBiDi)) {
      return new BidiCapability(
          BidiCapabilityStatus.UNSUPPORTED,
          "Driver does not implement HasBiDi",
          Set.of());
    }

    EnumSet<BidiFeature> supported = EnumSet.noneOf(BidiFeature.class);

    if (supportsNetwork(driver)) {
      supported.add(BidiFeature.NETWORK);
    }
    if (supportsConsole(driver)) {
      supported.add(BidiFeature.CONSOLE);
    }
    if (supportsNavigation(driver)) {
      supported.add(BidiFeature.NAVIGATION);
    }

    if (supported.isEmpty()) {
      return new BidiCapability(
          BidiCapabilityStatus.UNSUPPORTED, "BiDi modules unavailable", Set.of());
    }

    BidiCapabilityStatus status =
        supported.size() == BidiFeature.values().length
            ? BidiCapabilityStatus.SUPPORTED
            : BidiCapabilityStatus.DEGRADED;

    String reason =
        status == BidiCapabilityStatus.SUPPORTED
            ? "BiDi modules available"
            : "BiDi modules partially available";

    return new BidiCapability(status, reason, EnumSet.copyOf(supported));
  }

  private boolean supportsNetwork(WebDriver driver) {
    try (Network ignored = new Network(driver)) {
      return true;
    } catch (RuntimeException ex) {
      return false;
    }
  }

  private boolean supportsConsole(WebDriver driver) {
    try (LogInspector ignored = new LogInspector(driver)) {
      return true;
    } catch (RuntimeException ex) {
      return false;
    }
  }

  private boolean supportsNavigation(WebDriver driver) {
    try (BrowsingContextInspector ignored = new BrowsingContextInspector(driver)) {
      return true;
    } catch (RuntimeException ex) {
      return false;
    }
  }
}
