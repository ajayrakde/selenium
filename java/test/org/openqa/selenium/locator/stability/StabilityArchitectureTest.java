package org.openqa.selenium.locator.stability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTests")
class StabilityArchitectureTest {
  private static final List<String> STABILITY_CLASS_NAMES =
      List.of(
          "org.openqa.selenium.locator.stability.api.Clock",
          "org.openqa.selenium.locator.stability.api.StabilityEvaluator",
          "org.openqa.selenium.locator.stability.api.StabilitySampler",
          "org.openqa.selenium.locator.stability.api.StabilityService",
          "org.openqa.selenium.locator.stability.model.Rect",
          "org.openqa.selenium.locator.stability.model.VisibilityFlags",
          "org.openqa.selenium.locator.stability.model.StabilitySnapshot",
          "org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot",
          "org.openqa.selenium.locator.stability.model.StabilityHistory",
          "org.openqa.selenium.locator.stability.model.StabilityConfig",
          "org.openqa.selenium.locator.stability.api.SystemClock",
          "org.openqa.selenium.locator.stability.selenium.SeleniumStabilityService",
          "org.openqa.selenium.locator.stability.selenium.impl.DefaultStabilityEvaluator",
          "org.openqa.selenium.locator.stability.selenium.impl.DefaultStabilityService",
          "org.openqa.selenium.locator.stability.selenium.impl.JsStabilitySampler",
          "org.openqa.selenium.locator.stability.selenium.js.JsResultParser",
          "org.openqa.selenium.locator.stability.selenium.js.JsScripts");

  @Test
  void stabilityPackagesDoNotReferenceWaitsOrSleeps() throws IOException, ClassNotFoundException {
    for (String className : STABILITY_CLASS_NAMES) {
      Class<?> clazz = Class.forName(className);
      byte[] classBytes = readClassBytes(clazz);
      assertThat(classBytes)
          .as("Class %s should avoid Thread.sleep", clazz.getName())
          .doesNotContain("java/lang/Thread".getBytes());
      assertThat(classBytes)
          .as("Class %s should avoid WebDriverWait", clazz.getName())
          .doesNotContain("org/openqa/selenium/support/ui/WebDriverWait".getBytes());
      assertThat(classBytes)
          .as("Class %s should avoid ExpectedConditions", clazz.getName())
          .doesNotContain("org/openqa/selenium/support/ui/ExpectedConditions".getBytes());
    }
  }

  private byte[] readClassBytes(Class<?> clazz) throws IOException {
    String resource = clazz.getName().replace('.', '/') + ".class";
    try (InputStream stream = clazz.getClassLoader().getResourceAsStream(resource)) {
      if (stream == null) {
        throw new IOException("Missing class resource: " + resource);
      }
      return stream.readAllBytes();
    }
  }
}
