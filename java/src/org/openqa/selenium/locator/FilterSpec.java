package org.openqa.selenium.locator;

/**
 * Filter definitions applied to locator matches.
 */
public sealed interface FilterSpec permits TextEquals, TextContains, AttrEquals {}

public record TextEquals(String value) implements FilterSpec {}

public record TextContains(String value) implements FilterSpec {}

public record AttrEquals(String attr, String value) implements FilterSpec {}
