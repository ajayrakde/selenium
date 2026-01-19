package org.openqa.selenium.locator.bidi.model;

import java.util.List;
import java.util.Map;

public record NetworkSnapshot(
    long tsEpochMs,
    int inflight,
    long lastActivityEpochMs,
    Map<NetworkResourceType, Integer> inflightByType,
    int failuresCount,
    List<NetworkEvent> lastEvents) {}
