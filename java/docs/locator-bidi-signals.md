# WebDriver BiDi Signals (Priority #4)

This document describes the **facts-only** WebDriver BiDi signals layer for Selenium Java. The
implementation provides network, console, and navigation event tracking with capability detection
and bounded history buffers. It does not make readiness or idle decisions.

## Scope

**Provided**

- Capability detection (`SUPPORTED`, `UNSUPPORTED`, `DEGRADED`)
- Ring-buffered event history (bounded memory)
- Snapshot access for network/console/navigation trackers
- Diagnostics emission for metric/logging hooks
- Safe start/stop lifecycle tied to driver usage

**Not provided**

- Waits, polling loops, or “idle” logic
- Readiness decisions

## Package layout

- `org.openqa.selenium.locator.bidi` (core models and interfaces)
- `org.openqa.selenium.locator.bidi.selenium` (Selenium BiDi adapter + trackers)

## Usage

```java
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.config.BidiTrackingConfig;
import org.openqa.selenium.locator.bidi.selenium.api.SeleniumBidiSignalsFactory;

BidiTrackingConfig cfg = new BidiTrackingConfig(
    /* network */ new NetworkTrackingConfig(true, 200, true, Set.of(), List.of(), true, List.of()),
    /* console */ new ConsoleTrackingConfig(true, 200, Set.of(), 500, List.of(), List.of()),
    /* nav */ new NavigationTrackingConfig(true, 200, false, List.of())
);

BidiSignals signals = new SeleniumBidiSignalsFactory().create(driver, cfg);
signals.start();

var snapshot = signals.snapshotAll();
```

Snapshots are read-only and can be attached to diagnostics or failures. Start/stop must be
explicitly managed by the caller.
