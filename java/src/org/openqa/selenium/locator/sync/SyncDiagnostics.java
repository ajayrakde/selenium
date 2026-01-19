package org.openqa.selenium.locator.sync;

import java.util.List;
import java.util.Map;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;

public record SyncDiagnostics(
    Intent intent,
    String currentUrl,
    String title,
    long elapsedMillis,
    Map<String, GateStatus> gates,
    BidiCapability bidiCapability,
    NetworkSnapshot networkSnapshot,
    ConsoleSnapshot consoleSnapshot,
    NavigationSnapshot navigationSnapshot,
    List<StabilitySnapshot> stabilitySamples,
    String readyState,
    List<String> notes) {}

