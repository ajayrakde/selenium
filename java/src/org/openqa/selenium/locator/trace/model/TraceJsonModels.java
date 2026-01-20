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

package org.openqa.selenium.locator.trace.model;

import java.util.List;
import java.util.Map;

/**
 * All records in this package are Selenium-free and JSON-first.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>All strings should be bounded/sanitized by the writer layer (selenium module).
 *   <li>Paths are stored as relative strings (portable) not java.nio.Path.
 * </ul>
 */
public final class TraceJsonModels {
  private TraceJsonModels() {}

  // =========================
  // run.json
  // =========================

  public record RunJson(
      String schemaVersion, // e.g., "1.0"
      String runId, // stable id per run
      String startedAtIso, // ISO-8601 UTC string
      String timezone, // e.g., "UTC"
      FrameworkInfo framework,
      JavaInfo java,
      OsInfo os,
      DriverInfo driver,
      BrowserInfo browser,
      Map<String, Object> extra // optional, bounded
      ) {}

  public record FrameworkInfo(
      String name, // e.g., "locator"
      String version, // semver or build tag
      String gitSha, // optional
      String buildId // optional
      ) {}

  public record JavaInfo(
      String version, // e.g., "21.0.2"
      String vendor // e.g., "Eclipse Adoptium"
      ) {}

  public record OsInfo(
      String name, // e.g., "Windows 11"
      String version, // OS version/build
      String arch // e.g., "amd64"
      ) {}

  public record DriverInfo(
      String mode, // "local" | "remote"
      String remoteUrl, // nullable
      String sessionId, // nullable, best effort
      Map<String, Object> caps // nullable; keep bounded and filtered
      ) {}

  public record BrowserInfo(
      String name, // e.g., "chrome"
      String version, // nullable
      String platform // nullable
      ) {}

  // =========================
  // test.json
  // =========================

  public record TestJson(
      String schemaVersion, // "1.0"
      String runId,
      String testId, // stable id per test execution
      String suite,
      String name,
      List<String> tags,
      String startedAtIso,
      String endedAtIso, // nullable until finished
      long durationMs, // 0 until finished
      String outcome, // "PASS" | "FAIL" | "SKIP"
      String failureRef, // relative path to failure.json if failed
      List<StepRef> steps,
      Map<String, Object> extra
      ) {}

  public record StepRef(
      int stepIndex,
      String file // relative, e.g., "step-0001.json"
      ) {}

  // =========================
  // step-XXXX.json
  // =========================

  public record StepJson(
      String schemaVersion, // "1.0"
      String runId,
      String testId,
      int stepIndex,
      String type, // "ACTION" | "SYNC" | "ASSERT"
      String name,
      String intent, // "CLICK"|"TYPE"|... or null
      String startedAtIso,
      String endedAtIso, // nullable until closed
      long durationMs, // 0 until closed
      String result, // "SUCCESS" | "FAIL"
      TargetRef target, // nullable
      FailureRef failure, // nullable
      Snapshots snapshots, // never null; may contain null fields
      List<ArtifactRef> artifacts, // never null
      List<TraceLogLine> logs, // bounded
      Map<String, Object> extra
      ) {}

  // Target details are intentionally generic and bounded.
  public record TargetRef(
      String selectorKind, // "data-testid"|"css"|"xpath"|...
      String selector, // bounded
      String strictness, // "ONE"|"ALL"
      Integer resolvedCount, // nullable
      ElementSummary chosen // nullable
      ) {}

  public record ElementSummary(
      String tag, // nullable
      String id, // nullable
      String classes, // nullable (bounded)
      Rect rect, // nullable
      String textSnippet // nullable (bounded, e.g., 80 chars)
      ) {}

  public record Rect(double x, double y, double w, double h) {}

  // Failure reference stored inside step file (lightweight).
  public record FailureRef(
      String failureType, // enum name, e.g., "ACTION_FAILURE"
      String probableCause, // enum name, e.g., "CLICK_INTERCEPTED_OVERLAY"
      String message, // bounded
      String exceptionClass // bounded
      ) {}

  // Snapshots = structured diagnostic payloads that you already produce in #2/#3/#4/#5.
  // Keep them as Map<String,Object> so core does not depend on those modules.
  // The writer should only include bounded, sanitized data.
  public record Snapshots(
      Map<String, Object> actionDiagnostics, // from #2 (nullable)
      Map<String, Object> syncDiagnostics, // from #3 (nullable)
      Map<String, Object> bidiSnapshotBundle, // from #4 (nullable)
      List<Map<String, Object>> stabilitySamples // from #5 (bounded, nullable)
      ) {}

  public record ArtifactRef(
      String kind, // "screenshot"|"text"|"json"|"log"...
      String file, // relative path
      String contentType, // e.g., "image/png", "application/json"
      String description // nullable
      ) {}

  public record TraceLogLine(
      String tsIso,
      String level, // "TRACE"|"INFO"|"WARN"|"ERROR"
      String message // bounded
      ) {}

  // =========================
  // failure.json
  // =========================

  public record FailureJson(
      String schemaVersion, // "1.0"
      String runId,
      String testId,
      String failureType, // "ACTION_FAILURE"|"SYNC_TIMEOUT"|...
      String probableCause, // enum name
      String message, // bounded
      String exceptionClass, // bounded
      List<String> stacktrace, // bounded lines
      int primaryStepIndex,
      String occurredAtIso,
      List<ArtifactRef> artifacts, // e.g., screenshot
      Map<String, Object> snapshots, // bundled final snapshots at failure
      Map<String, Object> extra
      ) {}
}
