package org.openqa.selenium.locator.bidi.model;

import java.util.List;
import java.util.Map;

public record ConsoleSnapshot(
    long tsEpochMs,
    Map<ConsoleLevel, Integer> countsByLevel,
    long lastActivityEpochMs,
    List<ConsoleEvent> lastEvents) {}
