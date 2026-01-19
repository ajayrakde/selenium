package org.openqa.selenium.locator;

import java.util.Objects;

/**
 * Filter definitions applied to locator matches.
 */
public final class FilterSpec {
  public enum Type {
    TEXT_EQUALS,
    TEXT_CONTAINS,
    ATTR_EQUALS,
    HAS,
    VISIBLE_ONLY
  }

  private final Type type;
  private final String text;
  private final String attributeName;
  private final String attributeValue;
  private final LocatorPlan childPlan;
  private final boolean visibleOnly;

  private FilterSpec(
      Type type,
      String text,
      String attributeName,
      String attributeValue,
      LocatorPlan childPlan,
      boolean visibleOnly) {
    this.type = Objects.requireNonNull(type, "type");
    this.text = text;
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
    this.childPlan = childPlan;
    this.visibleOnly = visibleOnly;
  }

  public static FilterSpec textEquals(String text) {
    return new FilterSpec(Type.TEXT_EQUALS, Objects.requireNonNull(text, "text"), null, null, null, false);
  }

  public static FilterSpec textContains(String text) {
    return new FilterSpec(Type.TEXT_CONTAINS, Objects.requireNonNull(text, "text"), null, null, null, false);
  }

  public static FilterSpec attrEquals(String attributeName, String attributeValue) {
    return new FilterSpec(
        Type.ATTR_EQUALS,
        null,
        Objects.requireNonNull(attributeName, "attributeName"),
        Objects.requireNonNull(attributeValue, "attributeValue"),
        null,
        false);
  }

  public static FilterSpec has(LocatorPlan childPlan) {
    return new FilterSpec(Type.HAS, null, null, null, Objects.requireNonNull(childPlan, "childPlan"), false);
  }

  public static FilterSpec visibleOnly(boolean visibleOnly) {
    return new FilterSpec(Type.VISIBLE_ONLY, null, null, null, null, visibleOnly);
  }

  public Type type() {
    return type;
  }

  public String text() {
    return text;
  }

  public String attributeName() {
    return attributeName;
  }

  public String attributeValue() {
    return attributeValue;
  }

  public LocatorPlan childPlan() {
    return childPlan;
  }

  public boolean visibleOnly() {
    return visibleOnly;
  }
}
