package org.openqa.selenium.locator.actions.probe;

public record ElementState(
    boolean attached,
    boolean visible,
    boolean enabled,
    boolean pointerEvents,
    boolean inViewport,
    ElementRect rect,
    double viewportWidth,
    double viewportHeight) {}
