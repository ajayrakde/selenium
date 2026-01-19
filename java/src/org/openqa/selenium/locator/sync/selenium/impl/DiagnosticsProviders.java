package org.openqa.selenium.locator.sync.selenium.impl;

import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilityHistory;

interface NetworkSnapshotProvider {
  NetworkSnapshot lastNetworkSnapshot();
}

interface ConsoleSnapshotProvider {
  ConsoleSnapshot lastConsoleSnapshot();
}

interface NavigationSnapshotProvider {
  NavigationSnapshot lastNavigationSnapshot();
}

interface GlobalStabilitySnapshotProvider {
  GlobalStabilitySnapshot lastGlobalSnapshot();
}

interface StabilityHistoryProvider {
  StabilityHistory history();
}

interface ReadyStateProvider {
  String lastReadyState();
}

interface DomTokenProvider {
  String lastDomToken();
}
