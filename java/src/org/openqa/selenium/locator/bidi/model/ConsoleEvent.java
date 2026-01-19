package org.openqa.selenium.locator.bidi.model;

public record ConsoleEvent(
    long tsEpochMs,
    ConsoleLevel level,
    String text,
    String source,
    String url) {}
