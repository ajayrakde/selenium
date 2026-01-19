package org.openqa.selenium.locator;

import java.util.Objects;

/**
 * Selector step in a locator plan.
 */
public final class SelectorStep {
  public enum Kind {
    TEST_ID,
    CSS,
    XPATH,
    ROLE,
    LABEL
  }

  private final Kind kind;
  private final String value;
  private final AriaRole role;
  private final String name;

  private SelectorStep(Kind kind, String value, AriaRole role, String name) {
    this.kind = Objects.requireNonNull(kind, "kind");
    this.value = value;
    this.role = role;
    this.name = name;
  }

  public static SelectorStep testId(String testId) {
    return new SelectorStep(Kind.TEST_ID, Objects.requireNonNull(testId, "testId"), null, null);
  }

  public static SelectorStep css(String css) {
    return new SelectorStep(Kind.CSS, Objects.requireNonNull(css, "css"), null, null);
  }

  public static SelectorStep xpath(String xpath) {
    return new SelectorStep(Kind.XPATH, Objects.requireNonNull(xpath, "xpath"), null, null);
  }

  public static SelectorStep role(AriaRole role, String name) {
    return new SelectorStep(Kind.ROLE, null, Objects.requireNonNull(role, "role"), name);
  }

  public static SelectorStep label(String labelText) {
    return new SelectorStep(Kind.LABEL, Objects.requireNonNull(labelText, "labelText"), null, null);
  }

  public Kind kind() {
    return kind;
  }

  public String value() {
    return value;
  }

  public AriaRole role() {
    return role;
  }

  public String name() {
    return name;
  }
}
