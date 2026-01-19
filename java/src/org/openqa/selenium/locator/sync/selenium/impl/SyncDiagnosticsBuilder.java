package org.openqa.selenium.locator.sync.selenium.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.sync.GateStatus;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.SyncDiagnostics;
import org.openqa.selenium.locator.sync.policy.Gate;

final class SyncDiagnosticsBuilder {
  private final Intent intent;
  private final BidiCapability bidiCapability;
  private final Map<String, GateStatus> gates = new LinkedHashMap<>();
  private final List<String> notes = new ArrayList<>();

  private String currentUrl;
  private String title;
  private String readyState;
  private NetworkSnapshot networkSnapshot;
  private ConsoleSnapshot consoleSnapshot;
  private NavigationSnapshot navigationSnapshot;
  private List<StabilitySnapshot> stabilitySamples;

  SyncDiagnosticsBuilder(Intent intent, BidiCapability bidiCapability) {
    this.intent = intent;
    this.bidiCapability = bidiCapability;
  }

  void addNote(String note) {
    if (note != null && !note.isBlank()) {
      notes.add(note);
    }
  }

  void markGateSkipped(Gate gate, String reason) {
    gates.put(
        gate.name(),
        new GateStatus(false, false, reason, null, null));
  }

  void recordGate(Gate gate, GateCheckResult res, GateRuntimeState st, boolean quietOk) {
    gates.put(
        gate.name(),
        new GateStatus(
            true,
            quietOk,
            res.reason(),
            st.trueSinceEpochMs() >= 0 ? st.trueSinceEpochMs() : null,
            st.lastChangedAtEpochMs() >= 0 ? st.lastChangedAtEpochMs() : null));
  }

  void recordBasics(WebDriver driver, JavascriptExecutor js) {
    currentUrl = safeRead(() -> driver.getCurrentUrl());
    title = safeRead(() -> driver.getTitle());
    String rs = safeRead(() -> String.valueOf(js.executeScript("return document.readyState")));
    if (rs != null) {
      readyState = rs;
    }
  }

  void recordReadyState(String state) {
    if (state != null) {
      readyState = state;
    }
  }

  void recordNetworkSnapshot(NetworkSnapshot snapshot) {
    if (snapshot != null) {
      networkSnapshot = snapshot;
    }
  }

  void recordConsoleSnapshot(ConsoleSnapshot snapshot) {
    if (snapshot != null) {
      consoleSnapshot = snapshot;
    }
  }

  void recordNavigationSnapshot(NavigationSnapshot snapshot) {
    if (snapshot != null) {
      navigationSnapshot = snapshot;
    }
  }

  void recordStabilitySamples(List<StabilitySnapshot> samples) {
    if (samples != null) {
      stabilitySamples = samples;
    }
  }

  SyncDiagnostics buildSuccess(long elapsedMillis) {
    return build(elapsedMillis);
  }

  SyncDiagnostics buildTimeout(long elapsedMillis) {
    return build(elapsedMillis);
  }

  private SyncDiagnostics build(long elapsedMillis) {
    return new SyncDiagnostics(
        intent,
        currentUrl,
        title,
        elapsedMillis,
        Map.copyOf(gates),
        bidiCapability,
        networkSnapshot,
        consoleSnapshot,
        navigationSnapshot,
        stabilitySamples == null ? List.of() : List.copyOf(stabilitySamples),
        readyState,
        List.copyOf(notes));
  }

  private String safeRead(SupplierWithException<String> supplier) {
    try {
      return supplier.get();
    } catch (Exception ignored) {
      return null;
    }
  }

  private interface SupplierWithException<T> {
    T get() throws Exception;
  }
}
