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

package org.openqa.selenium.locator.selenium.trace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.locator.trace.Artifact;
import org.openqa.selenium.locator.trace.ArtifactKind;
import org.openqa.selenium.locator.trace.BinaryArtifact;
import org.openqa.selenium.locator.trace.ClassificationInput;
import org.openqa.selenium.locator.trace.DefaultProbableCauseClassifier;
import org.openqa.selenium.locator.trace.FailureBundle;
import org.openqa.selenium.locator.trace.FailureInfo;
import org.openqa.selenium.locator.trace.FailureType;
import org.openqa.selenium.locator.trace.JsonArtifact;
import org.openqa.selenium.locator.trace.ProbableCause;
import org.openqa.selenium.locator.trace.ProbableCauseClassifier;
import org.openqa.selenium.locator.trace.StepInfo;
import org.openqa.selenium.locator.trace.StepScope;
import org.openqa.selenium.locator.trace.TestInfo;
import org.openqa.selenium.locator.trace.TextArtifact;
import org.openqa.selenium.locator.trace.TraceEvent;
import org.openqa.selenium.locator.trace.TraceLog;
import org.openqa.selenium.locator.trace.TraceManager;
import org.openqa.selenium.locator.trace.TraceOptions;
import org.openqa.selenium.locator.trace.TraceSession;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.ArtifactRef;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.FailureJson;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.FailureRef;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.RunJson;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.Snapshots;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.StepJson;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.StepRef;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.TargetRef;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.TestJson;
import org.openqa.selenium.locator.trace.model.TraceJsonModels.TraceLogLine;

/**
 * Skeleton implementation:
 *
 * <ul>
 *   <li>Writes run.json once per run (on construction)
 *   <li>Creates per-test TraceSession that writes:
 *       <ul>
 *         <li>test.json on close
 *         <li>step-XXXX.json on each step close
 *         <li>failure.json when markedFailure()
 *       </ul>
 *   <li>Captures screenshot on failure when supported (best effort).
 * </ul>
 *
 * <p>This is intentionally minimal and safe:
 *
 * <ul>
 *   <li>bounded data
 *   <li>no waits
 *   <li>never throws if diagnostics collection fails
 * </ul>
 */
public final class SeleniumTraceManager implements TraceManager {

  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;
  private final WebDriver driver;
  private final Json json;
  private final Path rootDir;
  private final RunJson runJson;
  private final TraceOptions options;
  private final SnapshotCollector snapshotCollector;
  private final ProbableCauseClassifier classifier;

  public SeleniumTraceManager(
      WebDriver driver, Path rootDir, RunJson runJson, TraceOptions options) {
    this(
        driver,
        rootDir,
        runJson,
        options,
        new NoopSnapshotCollector(),
        new DefaultProbableCauseClassifier());
  }

  public SeleniumTraceManager(
      WebDriver driver,
      Path rootDir,
      RunJson runJson,
      TraceOptions options,
      SnapshotCollector snapshotCollector,
      ProbableCauseClassifier classifier) {
    this.driver = Objects.requireNonNull(driver, "driver");
    this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    this.runJson = Objects.requireNonNull(runJson, "runJson");
    this.options = options == null ? TraceOptions.defaults() : options;
    this.snapshotCollector =
        snapshotCollector == null ? new NoopSnapshotCollector() : snapshotCollector;
    this.classifier = classifier == null ? new DefaultProbableCauseClassifier() : classifier;

    this.json = new Json();

    try {
      Files.createDirectories(rootDir);
      writeJson(rootDir.resolve("run.json"), runJson, json);
    } catch (IOException e) {
      System.err.println("Tracing: failed to init run.json: " + e.getMessage());
    }
  }

  @Override
  public TraceSession startTest(TestInfo testInfo) {
    if (!options.enabled()) {
      return new NoopTraceSession();
    }
    return new SeleniumTraceSession(
        driver, json, rootDir, runJson.runId(), testInfo, options, snapshotCollector, classifier);
  }

  // -----------------------------------------
  // TraceSession implementation
  // -----------------------------------------
  private static final class SeleniumTraceSession implements TraceSession {

    private final WebDriver driver;
    private final Json json;
    private final Path testDir;
    private final String runId;
    private final String testId;
    private final String suite;
    private final String name;
    private final List<String> tags;
    private final TraceOptions options;
    private final SnapshotCollector snapshotCollector;
    private final ProbableCauseClassifier classifier;

    private final List<StepRef> stepRefs = new ArrayList<>();
    private final AtomicInteger stepCounter = new AtomicInteger(0);
    private final AtomicBoolean failureWritten = new AtomicBoolean(false);

    private String startedAtIso;
    private String endedAtIso;
    private String outcome = "PASS";
    private String failureRef = null;
    private FailureInfo failureInfo = null;

    SeleniumTraceSession(
        WebDriver driver,
        Json json,
        Path rootDir,
        String runId,
        TestInfo testInfo,
        TraceOptions options,
        SnapshotCollector snapshotCollector,
        ProbableCauseClassifier classifier) {
      this.driver = driver;
      this.json = json;
      this.runId = runId;
      this.options = options;
      this.snapshotCollector = snapshotCollector;
      this.classifier = classifier;

      this.suite = nullToEmpty(testInfo.suite());
      this.name = nullToEmpty(testInfo.name());
      this.tags = testInfo.tags() == null ? List.of() : List.copyOf(testInfo.tags());

      this.testId = testInfo.testId() != null ? testInfo.testId() : UUID.randomUUID().toString();
      this.startedAtIso = ISO.format(Instant.now());

      String ts = safeForPath(this.startedAtIso.replace(":", "-"));
      String shortId = this.testId.length() > 8 ? this.testId.substring(0, 8) : this.testId;
      this.testDir =
          rootDir.resolve(safeForPath(this.suite)).resolve(safeForPath(this.name)).resolve(ts + "-" + shortId);

      try {
        Files.createDirectories(testDir.resolve("screenshots"));
        Files.createDirectories(testDir.resolve("attachments"));
      } catch (IOException e) {
        System.err.println("Tracing: failed to create test dir: " + e.getMessage());
      }
    }

    @Override
    public StepScope startStep(StepInfo stepInfo) {
      int idx = stepCounter.incrementAndGet();
      if (idx > options.maxSteps()) {
        return new NoopStepScope();
      }

      String stepFile = String.format("step-%04d.json", idx);
      stepRefs.add(new StepRef(idx, stepFile));

      return new SeleniumStepScope(
          driver, json, testDir, runId, testId, idx, stepFile, stepInfo, options, this);
    }

    @Override
    public void recordEvent(TraceEvent event) {
      // Optional: support global events. For now ignore or write to an events.jsonl.
    }

    @Override
    public void markFailure(FailureInfo failureInfo) {
      if (failureInfo == null) {
        return;
      }
      markFailure(TraceSession.bundleFromFailureInfo(failureInfo));
    }

    public void markFailure(FailureBundle bundle) {
      if (bundle == null || bundle.failureInfo() == null) {
        return;
      }

      if (!failureWritten.compareAndSet(false, true)) {
        return;
      }

      this.outcome = "FAIL";
      this.failureRef = "failure.json";

      FailureInfo baseInfo = bundle.failureInfo();
      this.failureInfo = baseInfo;

      List<ArtifactRef> artifacts = new ArrayList<>();
      if (bundle.artifacts() != null) {
        artifacts.addAll(bundle.artifacts());
      }

      if (options.includeScreenshots()) {
        ArtifactRef failureScreenshot = captureFailureScreenshot();
        if (failureScreenshot != null) {
          artifacts.add(failureScreenshot);
        }
      }

      Map<String, Object> actionDiagnostics =
          mergeMaps(bundle.actionDiagnostics(), snapshotCollector::collectActionDiagnostics);
      Map<String, Object> syncDiagnostics =
          mergeMaps(bundle.syncDiagnostics(), snapshotCollector::collectSyncDiagnostics);
      Map<String, Object> bidiSnapshotBundle =
          mergeMaps(bundle.bidiSnapshotBundle(), snapshotCollector::collectBidiSnapshotBundle);
      List<Map<String, Object>> stabilitySamples =
          mergeStability(bundle.stabilitySamples(), snapshotCollector::collectStabilitySamples);

      if (bidiSnapshotBundle == null || bidiSnapshotBundle.isEmpty()) {
        bidiSnapshotBundle = Map.of("status", "UNSUPPORTED");
      }

      FailureBundle resolvedBundle =
          new FailureBundle(
              baseInfo,
              actionDiagnostics,
              syncDiagnostics,
              stabilitySamples,
              bidiSnapshotBundle,
              List.copyOf(artifacts));

      FailureJson failureJson =
          FailureJsonFactory.from(resolvedBundle, runId, testId, options, classifier);

      try {
        writeJson(testDir.resolve("failure.json"), failureJson, json);
      } catch (IOException e) {
        System.err.println("Tracing: failed to write failure.json: " + e.getMessage());
      }

    }

    ProbableCause classifyFailure(FailureBundle bundle) {
      FailureInfo baseInfo = bundle.failureInfo();
      return classifier.classify(
          new ClassificationInput(
              baseInfo == null ? null : baseInfo.throwable(),
              baseInfo == null ? null : baseInfo.failureType(),
              bundle.actionDiagnostics(),
              bundle.syncDiagnostics(),
              bundle.stabilitySamples(),
              bundle.bidiSnapshotBundle()));
    }

    @Override
    public void close() {
      this.endedAtIso = ISO.format(Instant.now());
      long durationMs = durationMs(startedAtIso, endedAtIso);

      TestJson tj =
          new TestJson(
              "1.0",
              runId,
              testId,
              suite,
              name,
              tags,
              startedAtIso,
              endedAtIso,
              durationMs,
              outcome,
              failureRef,
              List.copyOf(stepRefs),
              Map.of());

      try {
        writeJson(testDir.resolve("test.json"), tj, json);
      } catch (IOException e) {
        System.err.println("Tracing: failed to write test.json: " + e.getMessage());
      }
    }

    private ArtifactRef captureFailureScreenshot() {
      try {
        if (!(driver instanceof TakesScreenshot ts)) {
          return null;
        }
        byte[] png = ts.getScreenshotAs(OutputType.BYTES);
        String fn = "screenshots/failure.png";
        Path p = testDir.resolve(fn);
        Files.write(p, png, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new ArtifactRef("screenshot", fn, "image/png", "Failure screenshot");
      } catch (Exception e) {
        return null;
      }
    }

    private Map<String, Object> mergeMaps(
        Map<String, Object> primary, SnapshotSupplier supplier) {
      if (primary != null && !primary.isEmpty()) {
        return boundedMap(primary, 200);
      }
      try {
        Map<String, Object> fallback = supplier.get();
        return fallback == null || fallback.isEmpty() ? null : boundedMap(fallback, 200);
      } catch (Exception e) {
        return null;
      }
    }

    private List<Map<String, Object>> mergeStability(
        List<Map<String, Object>> primary, StabilitySupplier supplier) {
      List<Map<String, Object>> merged = new ArrayList<>();
      if (primary != null) {
        primary.forEach(sample -> {
          if (merged.size() < 20) {
            merged.add(boundedMap(sample, 100));
          }
        });
      }
      if (merged.size() < 20) {
        try {
          List<Map<String, Object>> fallback = supplier.get();
          if (fallback != null) {
            for (Map<String, Object> sample : fallback) {
              if (merged.size() >= 20) {
                break;
              }
              merged.add(boundedMap(sample, 100));
            }
          }
        } catch (Exception e) {
          return merged.isEmpty() ? null : List.copyOf(merged);
        }
      }
      return merged.isEmpty() ? null : List.copyOf(merged);
    }
  }

  // -----------------------------------------
  // StepScope implementation
  // -----------------------------------------
  private static final class SeleniumStepScope implements StepScope {

    private final WebDriver driver;
    private final Json json;
    private final Path testDir;
    private final Path attachmentsDir;
    private final String runId;
    private final String testId;
    private final int stepIndex;
    private final Path stepPath;
    private final TraceOptions options;
    private final SeleniumTraceSession session;

    private final List<ArtifactRef> artifacts = new ArrayList<>();
    private final List<TraceLogLine> logs = new ArrayList<>();

    private final Map<String, Object> actionDiagnostics = new LinkedHashMap<>();
    private final Map<String, Object> syncDiagnostics = new LinkedHashMap<>();
    private final Map<String, Object> bidiSnapshotBundle = new LinkedHashMap<>();
    private final List<Map<String, Object>> stabilitySamples = new ArrayList<>();

    private final String type;
    private final String name;
    private final String intent;
    private TargetRef target;

    private final String startedAtIso;
    private String endedAtIso;
    private long durationMs;
    private String result = "SUCCESS";
    private FailureRef failureRef = null;

    SeleniumStepScope(
        WebDriver driver,
        Json json,
        Path testDir,
        String runId,
        String testId,
        int stepIndex,
        String stepFile,
        StepInfo stepInfo,
        TraceOptions options,
        SeleniumTraceSession session) {
      this.driver = driver;
      this.json = json;
      this.testDir = testDir;
      this.attachmentsDir = testDir.resolve("attachments");
      this.runId = runId;
      this.testId = testId;
      this.stepIndex = stepIndex;
      this.stepPath = testDir.resolve(stepFile);
      this.options = options;
      this.session = session;

      this.type = nullToEmpty(stepInfo.type());
      this.name = nullToEmpty(stepInfo.name());
      this.intent = stepInfo.intent();
      this.startedAtIso = ISO.format(Instant.now());
      this.target = stepInfo.targetJson();
    }

    @Override
    public void addArtifact(Artifact artifact) {
      if (artifact == null) {
        return;
      }
      if (artifact instanceof TextArtifact textArtifact
          && !options.includeDomSnippet()
          && textArtifact.name() != null
          && textArtifact.name().toLowerCase().contains("dom-snippet")) {
        return;
      }

      String baseName = safeForPath(artifact.name());
      if (baseName.isBlank()) {
        baseName = "artifact-" + String.format("%04d", artifacts.size());
      }

      try {
        ArtifactRef ref = writeArtifact(artifact, baseName);
        if (ref != null) {
          artifacts.add(ref);
        }
      } catch (Exception e) {
        // best effort; never throw
      }
    }

    @Override
    public void addSnapshot(Object snapshot) {
      if (!(snapshot instanceof Map<?, ?> m)) {
        return;
      }

      Object kindObj = m.get("kind");
      String kind = kindObj == null ? "custom" : String.valueOf(kindObj);

      @SuppressWarnings("unchecked")
      Map<String, Object> mm = (Map<String, Object>) m;

      switch (kind) {
        case "actionDiagnostics" -> actionDiagnostics.putAll(boundedMap(mm, 200));
        case "syncDiagnostics" -> syncDiagnostics.putAll(boundedMap(mm, 200));
        case "bidiSnapshotBundle" -> bidiSnapshotBundle.putAll(boundedMap(mm, 200));
        case "stabilitySample" -> {
          if (stabilitySamples.size() < 20) {
            stabilitySamples.add(boundedMap(mm, 100));
          }
        }
        default -> actionDiagnostics.put("custom", boundedMap(mm, 200));
      }
    }

    @Override
    public void addLog(TraceLog logLine) {
      if (logs.size() >= options.maxLogsPerStep()) {
        return;
      }
      logs.add(
          new TraceLogLine(
              ISO.format(Instant.now()),
              nullToEmpty(logLine.level()),
              truncate(logLine.message(), options.maxMessageChars())));
    }

    @Override
    public void markSuccess() {
      this.result = "SUCCESS";
    }

    @Override
    public void markFailure(Throwable t, FailureType type) {
      this.result = "FAIL";

      FailureInfo baseInfo = FailureInfo.fromThrowable(t, type, stepIndex);
      FailureBundle bundle =
          new FailureBundle(
              baseInfo,
              actionDiagnostics.isEmpty() ? null : Map.copyOf(actionDiagnostics),
              syncDiagnostics.isEmpty() ? null : Map.copyOf(syncDiagnostics),
              stabilitySamples.isEmpty() ? null : List.copyOf(stabilitySamples),
              bidiSnapshotBundle.isEmpty() ? null : Map.copyOf(bidiSnapshotBundle),
              List.copyOf(artifacts));

      ProbableCause probableCause =
          session.classifyFailure(bundle);

      this.failureRef =
          new FailureRef(
              String.valueOf(type),
              String.valueOf(probableCause),
              truncate(String.valueOf(t.getMessage()), options.maxMessageChars()),
              t.getClass().getName());

      if (options.includeScreenshots()) {
        captureStepFailureScreenshot();
      }

      session.markFailure(bundle);
    }

    @Override
    public void close() {
      this.endedAtIso = ISO.format(Instant.now());
      this.durationMs = durationMs(startedAtIso, endedAtIso);

      StepJson sj =
          new StepJson(
              "1.0",
              runId,
              testId,
              stepIndex,
              type,
              name,
              intent,
              startedAtIso,
              endedAtIso,
              durationMs,
              result,
              target,
              failureRef,
              new Snapshots(
                  actionDiagnostics.isEmpty() ? null : Map.copyOf(actionDiagnostics),
                  syncDiagnostics.isEmpty() ? null : Map.copyOf(syncDiagnostics),
                  bidiSnapshotBundle.isEmpty() ? null : Map.copyOf(bidiSnapshotBundle),
                  stabilitySamples.isEmpty() ? null : List.copyOf(stabilitySamples)),
              List.copyOf(artifacts),
              List.copyOf(logs),
              Map.of());

      try {
        writeJson(stepPath, sj, json);
      } catch (IOException e) {
        System.err.println("Tracing: failed to write step json: " + e.getMessage());
      }
    }

    private ArtifactRef writeArtifact(Artifact artifact, String baseName) throws IOException {
      if (artifact.kind() == ArtifactKind.TEXT && artifact instanceof TextArtifact textArtifact) {
        String filename = baseName + ".txt";
        byte[] bytes =
            truncateBytes(
                textArtifact.content() == null
                    ? new byte[0]
                    : textArtifact.content().getBytes(StandardCharsets.UTF_8),
                artifact.maxBytes() > 0 ? artifact.maxBytes() : options.maxArtifactBytes());
        writeBytes(attachmentsDir.resolve(filename), bytes);
        return new ArtifactRef("text", "attachments/" + filename, "text/plain", null);
      }

      if (artifact.kind() == ArtifactKind.JSON && artifact instanceof JsonArtifact jsonArtifact) {
        String filename = baseName + ".json";
        byte[] bytes = json.toJson(jsonArtifact.payload()).getBytes(StandardCharsets.UTF_8);
        bytes = truncateBytes(bytes, artifact.maxBytes() > 0 ? artifact.maxBytes() : options.maxArtifactBytes());
        writeBytes(attachmentsDir.resolve(filename), bytes);
        return new ArtifactRef("json", "attachments/" + filename, "application/json", null);
      }

      if (artifact.kind() == ArtifactKind.BINARY && artifact instanceof BinaryArtifact binaryArtifact) {
        String filename = baseName + ".bin";
        byte[] bytes =
            truncateBytes(
                binaryArtifact.payload(),
                artifact.maxBytes() > 0 ? artifact.maxBytes() : options.maxArtifactBytes());
        writeBytes(attachmentsDir.resolve(filename), bytes);
        return new ArtifactRef(
            "binary", "attachments/" + filename, artifact.contentType(), null);
      }

      return null;
    }

    private void writeBytes(Path path, byte[] bytes) throws IOException {
      Files.createDirectories(path.getParent());
      Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void captureStepFailureScreenshot() {
      try {
        if (!(driver instanceof TakesScreenshot ts)) {
          return;
        }
        byte[] png = ts.getScreenshotAs(OutputType.BYTES);
        String fn = "screenshots/failure-step-" + String.format("%04d", stepIndex) + ".png";
        Path p = testDir.resolve(fn);
        Files.write(p, png, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        artifacts.add(new ArtifactRef("screenshot", fn, "image/png", "Failure screenshot"));
      } catch (Exception e) {
        // best effort; never throw
      }
    }
  }

  // -----------------------------------------
  // Noop implementations (when tracing disabled)
  // -----------------------------------------
  private static final class NoopTraceSession implements TraceSession {
    @Override
    public StepScope startStep(StepInfo stepInfo) {
      return new NoopStepScope();
    }

    @Override
    public void recordEvent(TraceEvent event) {}

    @Override
    public void markFailure(FailureInfo failureInfo) {}

    @Override
    public void close() {}
  }

  private static final class NoopStepScope implements StepScope {
    @Override
    public void addArtifact(Artifact artifact) {}

    @Override
    public void addSnapshot(Object snapshot) {}

    @Override
    public void addLog(TraceLog logLine) {}

    @Override
    public void markSuccess() {}

    @Override
    public void markFailure(Throwable t, FailureType type) {}

    @Override
    public void close() {}
  }

  // -----------------------------------------
  // Utilities
  // -----------------------------------------
  private static void writeJson(Path path, Object value, Json json) throws IOException {
    Files.createDirectories(path.getParent());
    byte[] bytes = json.toJson(value).getBytes(StandardCharsets.UTF_8);
    Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String safeForPath(String s) {
    if (s == null || s.isBlank()) {
      return "unknown";
    }
    return s.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    if (max <= 0) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max);
  }

  private static byte[] truncateBytes(byte[] bytes, int maxBytes) {
    if (bytes == null) {
      return new byte[0];
    }
    int limit = maxBytes > 0 ? maxBytes : bytes.length;
    if (bytes.length <= limit) {
      return bytes;
    }
    byte[] truncated = new byte[limit];
    System.arraycopy(bytes, 0, truncated, 0, limit);
    return truncated;
  }

  private static long durationMs(String startIso, String endIso) {
    try {
      Instant a = Instant.parse(startIso);
      Instant b = Instant.parse(endIso);
      return Math.max(0, b.toEpochMilli() - a.toEpochMilli());
    } catch (Exception e) {
      return 0;
    }
  }

  private static Map<String, Object> boundedMap(Map<String, Object> in, int maxKeys) {
    if (in == null) {
      return Map.of();
    }
    Map<String, Object> out = new LinkedHashMap<>();
    int i = 0;
    for (Map.Entry<String, Object> entry : in.entrySet()) {
      if (i++ >= maxKeys) {
        break;
      }
      Object v = entry.getValue();
      if (v instanceof String s) {
        v = truncate(s, 2000);
      }
      out.put(String.valueOf(entry.getKey()), v);
    }
    return out;
  }

  private static List<Map<String, Object>> boundedSamples(
      List<Map<String, Object>> samples, int maxSamples) {
    if (samples == null) {
      return List.of();
    }
    List<Map<String, Object>> out = new ArrayList<>();
    for (Map<String, Object> sample : samples) {
      if (out.size() >= maxSamples) {
        break;
      }
      out.add(boundedMap(sample, 100));
    }
    return out;
  }

  private interface SnapshotSupplier {
    Map<String, Object> get();
  }

  private interface StabilitySupplier {
    List<Map<String, Object>> get();
  }

  // -----------------------------------------
  // FailureJsonFactory (skeleton)
  // -----------------------------------------
  private static final class FailureJsonFactory {
    static FailureJson from(
        FailureBundle bundle,
        String runId,
        String testId,
        TraceOptions options,
        ProbableCauseClassifier classifier) {
      FailureInfo info = bundle.failureInfo();
      List<String> stack = info.stacktraceLines();
      if (stack != null && stack.size() > options.maxStackLines()) {
        stack = stack.subList(0, options.maxStackLines());
      }

      Map<String, Object> snapshots = new LinkedHashMap<>();
      if (bundle.actionDiagnostics() != null && !bundle.actionDiagnostics().isEmpty()) {
        snapshots.put("actionDiagnostics", boundedMap(bundle.actionDiagnostics(), 200));
      }
      if (bundle.syncDiagnostics() != null && !bundle.syncDiagnostics().isEmpty()) {
        snapshots.put("syncDiagnostics", boundedMap(bundle.syncDiagnostics(), 200));
      }
      if (bundle.stabilitySamples() != null && !bundle.stabilitySamples().isEmpty()) {
        snapshots.put("stabilitySamples", boundedSamples(bundle.stabilitySamples(), 20));
      }
      if (bundle.bidiSnapshotBundle() != null && !bundle.bidiSnapshotBundle().isEmpty()) {
        snapshots.put("bidiSnapshotBundle", boundedMap(bundle.bidiSnapshotBundle(), 200));
      }

      ProbableCause probableCause =
          classifier.classify(
              new ClassificationInput(
                  info.throwable(),
                  info.failureType(),
                  bundle.actionDiagnostics(),
                  bundle.syncDiagnostics(),
                  bundle.stabilitySamples(),
                  bundle.bidiSnapshotBundle()));

      return new FailureJson(
          "1.0",
          runId,
          testId,
          String.valueOf(info.failureType()),
          String.valueOf(probableCause),
          truncate(info.message(), options.maxMessageChars()),
          truncate(info.exceptionClass(), 500),
          stack == null ? List.of() : stack,
          info.primaryStepIndex(),
          ISO.format(Instant.now()),
          bundle.artifacts() == null ? List.of() : List.copyOf(bundle.artifacts()),
          snapshots.isEmpty() ? Map.of() : snapshots,
          Map.of());
    }
  }
}
