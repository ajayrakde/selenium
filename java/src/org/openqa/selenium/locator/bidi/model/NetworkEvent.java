package org.openqa.selenium.locator.bidi.model;

public record NetworkEvent(
    long tsEpochMs,
    String requestId,
    String url,
    String method,
    NetworkResourceType type,
    NetworkStage stage,
    Integer status,
    String failureReason) {}
