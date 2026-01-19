package org.openqa.selenium.locator;

/**
 * Selector step in a locator plan.
 */
public sealed interface SelectorStep permits TestIdStep, CssStep, XpathStep {}

public record TestIdStep(String testId) implements SelectorStep {}

public record CssStep(String css) implements SelectorStep {}

public record XpathStep(String xpath) implements SelectorStep {}
