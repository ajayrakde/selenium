// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.DefaultDiagnosticsSink;
import org.openqa.selenium.locator.DefaultLocatorFactory;
import org.openqa.selenium.locator.DefaultLocatorResolver;
import org.openqa.selenium.locator.DefaultPolicy;
import org.openqa.selenium.locator.DefaultUnsafe;
import org.openqa.selenium.locator.AutomationContext;
import org.openqa.selenium.locator.CssStep;
import org.openqa.selenium.locator.Locator;
import org.openqa.selenium.locator.LocatorAmbiguousException;
import org.openqa.selenium.locator.LocatorNotFoundException;
import org.openqa.selenium.locator.LocatorFactory;
import org.openqa.selenium.locator.LocatorPlan;
import org.openqa.selenium.locator.LocatorPolicyViolationException;
import org.openqa.selenium.locator.LocatorScopeException;
import org.openqa.selenium.locator.Policy;
import org.openqa.selenium.locator.ResolutionResult;
import org.openqa.selenium.locator.ResolveOptions;
import org.openqa.selenium.locator.RootScope;
import org.openqa.selenium.locator.SelectorTier;
import org.openqa.selenium.locator.Strictness;
import org.openqa.selenium.locator.TextNormalization;
import org.openqa.selenium.locator.TierPolicy;

class LocatorEngineTest {

  @Test
  void strictOneNotFoundThrows() {
    FakeDriver driver = new FakeDriver(List.of());
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);
    Locator locator = context.locator().byTestId("missing");

    assertThatThrownBy(() -> locator.text(Duration.ZERO))
        .isInstanceOf(LocatorNotFoundException.class)
        .hasMessageContaining("did not match");
  }

  @Test
  void strictOneAmbiguousThrows() {
    FakeDriver driver = new FakeDriver(List.of(element("one"), element("two")));
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);
    Locator locator = context.locator().byTestId("dup");

    assertThatThrownBy(() -> locator.text(Duration.ZERO))
        .isInstanceOf(LocatorAmbiguousException.class)
        .hasMessageContaining("multiple");
  }

  @Test
  void allCountReturnsMatches() {
    FakeDriver driver = new FakeDriver(List.of(element("one"), element("two")));
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);
    Locator locator = context.locator().byTestId("items");

    assertThat(locator.all().count(Duration.ZERO)).isEqualTo(2);
  }

  @Test
  void nthSelectsFromMany() {
    FakeDriver driver = new FakeDriver(List.of(element("one"), element("two")));
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);
    Locator locator = context.locator().byTestId("items");

    assertThat(locator.all().nth(1).text(Duration.ZERO)).isEqualTo("two");
  }

  @Test
  void withinScopesToContainer() {
    FakeElement childOne = element("child-one");
    FakeElement childTwo = element("child-two");
    FakeElement container = element("container");
    container.setChildren(List.of(childOne, childTwo));

    FakeDriver driver = new FakeDriver(List.of(container));
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);

    Locator modal = context.locator().byTestId("modal");
    Locator scoped = context.locator().byCss(".child").within(modal);

    assertThat(scoped.all().count(Duration.ZERO)).isEqualTo(2);
  }

  @Test
  void withinMissingContainerThrowsScopeException() {
    FakeDriver driver = new FakeDriver(List.of());
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);

    Locator modal = context.locator().byTestId("modal");
    Locator scoped = context.locator().byCss(".child").within(modal);

    assertThatThrownBy(() -> scoped.all().count(Duration.ZERO))
        .isInstanceOf(LocatorScopeException.class);
  }

  @Test
  void strictPolicyBlocksXpath() {
    FakeDriver driver = new FakeDriver(List.of());
    AutomationContext context = createContext(driver, TierPolicy.STRICT);

    assertThatThrownBy(() -> context.locator().byXpath("//div"))
        .isInstanceOf(LocatorPolicyViolationException.class);
  }

  @Test
  void candidatePreviewsOnlyOnFailure() {
    FakeDriver driver = new FakeDriver(List.of(element("ok")));
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);
    DefaultLocatorResolver resolver = new DefaultLocatorResolver();

    LocatorPlan plan = new LocatorPlan(
        "css=.ok",
        new RootScope(),
        List.of(new CssStep(".ok")),
        List.of(),
        Strictness.ONE,
        SelectorTier.TIER3_CSS,
        Map.of());
    ResolutionResult result = resolver.resolve(
        context,
        plan,
        new ResolveOptions(Duration.ZERO, Duration.ZERO, false));

    assertThat(result.diagnostics().candidates()).isEmpty();
    assertThat(result.strictness()).isEqualTo(Strictness.ONE);
  }

  @Test
  void pollsStopEarlyOnSuccess() {
    FakeDriver driver = new FakeDriver(List.of(element("ok")));
    AutomationContext context = createContext(driver, TierPolicy.STANDARD);
    DefaultLocatorResolver resolver = new DefaultLocatorResolver();

    LocatorPlan plan = new LocatorPlan(
        "css=.ok",
        new RootScope(),
        List.of(new CssStep(".ok")),
        List.of(),
        Strictness.ONE,
        SelectorTier.TIER3_CSS,
        Map.of());

    ResolutionResult result = resolver.resolve(
        context,
        plan,
        new ResolveOptions(Duration.ofSeconds(1), Duration.ofMillis(10), false));

    assertThat(result.diagnostics().polls()).isEqualTo(1);
  }

  private AutomationContext createContext(FakeDriver driver, TierPolicy tierPolicy) {
    DefaultPolicy policy = new DefaultPolicy(
        "data-testid",
        Duration.ofSeconds(1),
        Duration.ofMillis(1),
        tierPolicy,
        5,
        TextNormalization.ON);
    DefaultLocatorResolver resolver = new DefaultLocatorResolver();
    TestAutomationContext context = new TestAutomationContext(policy, driver);
    DefaultLocatorFactory factory = new DefaultLocatorFactory(context, resolver);
    context.setLocatorFactory(factory);
    return context;
  }

  private static FakeElement element(String text) {
    return new FakeElement(text);
  }

  private static class FakeDriver extends StubDriver {
    private final List<WebElement> elements;

    FakeDriver(List<WebElement> elements) {
      this.elements = new ArrayList<>(elements);
    }

    @Override
    public List<WebElement> findElements(By by) {
      return List.copyOf(elements);
    }

    @Override
    public WebElement findElement(By by) {
      if (elements.isEmpty()) {
        throw new NoSuchElementException("none");
      }
      return elements.get(0);
    }

    @Override
    public String getCurrentUrl() {
      return "http://example.test";
    }

    @Override
    public String getTitle() {
      return "Example";
    }
  }

  private static class TestAutomationContext implements AutomationContext {
    private final Policy policy;
    private final DefaultDiagnosticsSink diagnostics = new DefaultDiagnosticsSink();
    private final DefaultUnsafe unsafe;
    private LocatorFactory locatorFactory;

    TestAutomationContext(Policy policy, WebDriver driver) {
      this.policy = policy;
      this.unsafe = new DefaultUnsafe(driver);
    }

    void setLocatorFactory(LocatorFactory locatorFactory) {
      this.locatorFactory = locatorFactory;
    }

    @Override
    public LocatorFactory locator() {
      return locatorFactory;
    }

    @Override
    public Policy policy() {
      return policy;
    }

    @Override
    public DefaultDiagnosticsSink diagnostics() {
      return diagnostics;
    }

    @Override
    public DefaultUnsafe unsafe() {
      return unsafe;
    }
  }

  private static class FakeElement implements WebElement {
    private final String text;
    private List<WebElement> children = List.of();

    FakeElement(String text) {
      this.text = text;
    }

    void setChildren(List<WebElement> children) {
      this.children = new ArrayList<>(children);
    }

    @Override
    public void click() {
      throw new UnsupportedOperationException("click");
    }

    @Override
    public void submit() {
      throw new UnsupportedOperationException("submit");
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
      throw new UnsupportedOperationException("sendKeys");
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException("clear");
    }

    @Override
    public String getTagName() {
      return "div";
    }

    @Override
    public String getAttribute(String name) {
      return switch (name) {
        case "id" -> text + "-id";
        case "class" -> "class-" + text;
        case "data-testid" -> text;
        default -> null;
      };
    }

    @Override
    public boolean isSelected() {
      return false;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public List<WebElement> findElements(By by) {
      return List.copyOf(children);
    }

    @Override
    public WebElement findElement(By by) {
      if (children.isEmpty()) {
        throw new NoSuchElementException("none");
      }
      return children.get(0);
    }

    @Override
    public boolean isDisplayed() {
      return true;
    }

    @Override
    public Point getLocation() {
      return new Point(0, 0);
    }

    @Override
    public Dimension getSize() {
      return new Dimension(0, 0);
    }

    @Override
    public Rectangle getRect() {
      return new Rectangle(getLocation(), getSize());
    }

    @Override
    public String getCssValue(String propertyName) {
      return "";
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
      throw new UnsupportedOperationException("getScreenshotAs");
    }
  }
}
