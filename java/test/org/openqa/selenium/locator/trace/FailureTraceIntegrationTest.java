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

package org.openqa.selenium.locator.trace;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.locator.selenium.trace.NoopSnapshotCollector;
import org.openqa.selenium.locator.selenium.trace.SeleniumTraceManager;
import org.openqa.selenium.locator.trace.model.TraceJsonModels;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.BrowserInfo;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.DriverInfo;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.FrameworkInfo;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.JavaInfo;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.OsInfo;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.RunJson;
import org.openqa.selenium.testing.JupiterTestBase;

class FailureTraceIntegrationTest extends JupiterTestBase {

  private final Json json = new Json();
  private Path traceRoot;

  @BeforeEach
  void createTraceRoot() throws IOException {
    traceRoot = Files.createTempDirectory("trace-tests");
  }

  @AfterEach
  void cleanupTraceRoot() throws IOException {
    if (traceRoot != null) {
      try (Stream<Path> paths = Files.walk(traceRoot)) {
        paths.sorted((a, b) -> b.compareTo(a)).forEach(path -> path.toFile().delete());
      }
    }
  }

  @Test
  void overlayFailureProducesFailureBundle() throws IOException {
    SeleniumTraceManager manager =
        new SeleniumTraceManager(
            driver, traceRoot, newRunJson(), TraceOptions.defaults(), new NoopSnapshotCollector(), new DefaultProbableCauseClassifier());

    TestInfo testInfo = new BasicTestInfo("trace", "ClickIntercept");

    try (TraceSession session = manager.startTest(testInfo)) {
      driver.get(appServer.whereIs("trace/click-intercept.html"));
      try (StepScope step = session.startStep(new BasicStepInfo("ACTION", "Click target", "CLICK"))) {
        step.addSnapshot(Map.of("kind", "actionDiagnostics", "coveredByElement", true));
        WebElement target = driver.findElement(By.id("target"));
        Throwable failure = null;
        try {
          target.click();
        } catch (WebDriverException e) {
          failure = e;
        }
        if (failure == null) {
          failure = new RuntimeException("Expected click interception");
        }
        step.markFailure(failure, FailureType.ACTION_FAILURE);
      }
    }

    Map<String, Object> failureJson = loadFailureJson();
    assertThat(failureJson.get("probableCause")).isEqualTo("CLICK_INTERCEPTED_OVERLAY");

    Map<?, ?> snapshots = (Map<?, ?>) failureJson.get("snapshots");
    assertThat(snapshots).containsKey("actionDiagnostics");

    assertCanonicalFailureScreenshotPresent(failureJson);
  }

  @Test
  void movingElementFailureCapturesStabilitySamples() throws IOException {
    SeleniumTraceManager manager =
        new SeleniumTraceManager(
            driver, traceRoot, newRunJson(), TraceOptions.defaults(), new NoopSnapshotCollector(), new DefaultProbableCauseClassifier());

    TestInfo testInfo = new BasicTestInfo("trace", "MovingElement");

    try (TraceSession session = manager.startTest(testInfo)) {
      driver.get(appServer.whereIs("trace/moving-button.html"));
      try (StepScope step = session.startStep(new BasicStepInfo("ACTION", "Click moving", "CLICK"))) {
        step.addSnapshot(Map.of("kind", "stabilitySample", "moving", true));
        step.markFailure(new RuntimeException("forced failure"), FailureType.ACTION_FAILURE);
      }
    }

    Map<String, Object> failureJson = loadFailureJson();
    assertThat(failureJson.get("probableCause")).isEqualTo("ELEMENT_MOVING_ANIMATION");

    Map<?, ?> snapshots = (Map<?, ?>) failureJson.get("snapshots");
    assertThat(snapshots).containsKey("stabilitySamples");
  }

  @Test
  void bidiUnsupportedIsRecorded() throws IOException {
    SeleniumTraceManager manager =
        new SeleniumTraceManager(
            driver, traceRoot, newRunJson(), TraceOptions.defaults(), new NoopSnapshotCollector(), new DefaultProbableCauseClassifier());

    TestInfo testInfo = new BasicTestInfo("trace", "BidiUnsupported");

    try (TraceSession session = manager.startTest(testInfo)) {
      driver.get(appServer.whereIs("trace/click-intercept.html"));
      try (StepScope step = session.startStep(new BasicStepInfo("ACTION", "Fail without bidi", "CLICK"))) {
        step.markFailure(new RuntimeException("forced failure"), FailureType.ACTION_FAILURE);
      }
    }

    Map<String, Object> failureJson = loadFailureJson();
    Map<?, ?> snapshots = (Map<?, ?>) failureJson.get("snapshots");
    Map<?, ?> bidiSnapshot = (Map<?, ?>) snapshots.get("bidiSnapshotBundle");
    assertThat(bidiSnapshot).containsEntry("status", "UNSUPPORTED");
  }

  private Map<String, Object> loadFailureJson() throws IOException {
    Path failureJsonPath = findFailureJson();
    String content = Files.readString(failureJsonPath);
    return json.toType(content, Json.MAP_TYPE);
  }

  private Path findFailureJson() throws IOException {
    try (Stream<Path> paths = Files.walk(traceRoot)) {
      return paths
          .filter(path -> path.getFileName().toString().equals("failure.json"))
          .findFirst()
          .orElseThrow();
    }
  }

  private void assertCanonicalFailureScreenshotPresent(Map<String, Object> failureJson) {
    if (!(driver instanceof TakesScreenshot)) {
      return;
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> artifacts = (List<Map<String, Object>>) failureJson.get("artifacts");
    assertThat(artifacts)
        .anyMatch(artifact -> "screenshots/failure.png".equals(artifact.get("file")));

    assertThat(Files.exists(traceRoot.resolve(findRelativePath("screenshots/failure.png"))))
        .isTrue();
  }

  private Path findRelativePath(String relative) {
    try (Stream<Path> paths = Files.walk(traceRoot)) {
      return paths
          .filter(path -> path.toString().endsWith(relative))
          .findFirst()
          .orElseThrow();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private RunJson newRunJson() {
    return new RunJson(
        "1.0",
        UUID.randomUUID().toString(),
        Instant.now().toString(),
        "UTC",
        new FrameworkInfo("locator", "test", null, null),
        new JavaInfo(System.getProperty("java.version"), System.getProperty("java.vendor")),
        new OsInfo(
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch")),
        new DriverInfo("local", null, null, Map.of()),
        new BrowserInfo("unknown", null, null),
        Map.of());
  }

  private record BasicTestInfo(String suite, String name) implements TestInfo {
    @Override
    public String testId() {
      return suite + "-" + name;
    }

    @Override
    public List<String> tags() {
      return List.of("trace");
    }
  }

  private record BasicStepInfo(String type, String name, String intent) implements StepInfo {
    @Override
    public TraceJsonModels.TargetRef targetJson() {
      return null;
    }
  }
}
