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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultProbableCauseClassifier implements ProbableCauseClassifier {
  @Override
  public ProbableCause classify(ClassificationInput input) {
    if (input == null) {
      return ProbableCause.TIMEOUT_UNKNOWN;
    }

    Throwable throwable = input.throwable();
    if (isStrictnessViolation(throwable)) {
      return ProbableCause.STRICTNESS_VIOLATION;
    }

    if (hasClickIntercepted(input.actionDiagnostics())) {
      return ProbableCause.CLICK_INTERCEPTED_OVERLAY;
    }

    if (hasMovingElement(input.stabilitySamples())) {
      return ProbableCause.ELEMENT_MOVING_ANIMATION;
    }

    if (hasNetworkInflight(input.bidiSnapshotBundle())) {
      return ProbableCause.NETWORK_BUSY;
    }

    if (hasConsoleDenylist(input.bidiSnapshotBundle())) {
      return ProbableCause.CONSOLE_ERROR_BLOCKING;
    }

    if (isDriverDisconnected(throwable)) {
      return ProbableCause.DRIVER_DISCONNECTED;
    }

    return ProbableCause.TIMEOUT_UNKNOWN;
  }

  private boolean isStrictnessViolation(Throwable throwable) {
    if (throwable == null) {
      return false;
    }
    String name = throwable.getClass().getName();
    String message = String.valueOf(throwable.getMessage());
    return name.contains("LocatorAmbiguousException")
        || name.contains("Strictness")
        || message.toLowerCase().contains("strict");
  }

  private boolean hasClickIntercepted(Map<String, Object> actionDiagnostics) {
    Map<String, Object> map = safeMap(actionDiagnostics);
    return isTrue(map, "coveredByElement")
        || isTrue(map, "clickIntercepted")
        || isTrue(map, "intercepted")
        || containsToken(map, "reason", "covered")
        || containsToken(map, "message", "intercepted");
  }

  private boolean hasMovingElement(List<Map<String, Object>> stabilitySamples) {
    List<Map<String, Object>> samples = safeList(stabilitySamples);
    for (Map<String, Object> sample : samples) {
      if (isTrue(sample, "moving")
          || isTrue(sample, "unstable")
          || isTrue(sample, "rectChanged")
          || containsToken(sample, "reason", "moving")) {
        return true;
      }
    }
    return false;
  }

  private boolean hasNetworkInflight(Map<String, Object> bidiSnapshotBundle) {
    Map<String, Object> map = safeMap(bidiSnapshotBundle);
    Object inflight = map.get("inflightRequests");
    if (inflight instanceof Number number) {
      return number.intValue() > 0;
    }
    if (inflight instanceof String string) {
      return parsePositiveInt(string);
    }
    return false;
  }

  private boolean hasConsoleDenylist(Map<String, Object> bidiSnapshotBundle) {
    Map<String, Object> map = safeMap(bidiSnapshotBundle);
    return isTrue(map, "consoleDenylistMatch")
        || isTrue(map, "consoleErrorBlocking")
        || containsToken(map, "console", "denylist");
  }

  private boolean isDriverDisconnected(Throwable throwable) {
    if (throwable == null) {
      return false;
    }
    String name = throwable.getClass().getName();
    String message = String.valueOf(throwable.getMessage()).toLowerCase();
    return name.contains("NoSuchSessionException")
        || name.contains("SessionNotFound")
        || message.contains("disconnected")
        || message.contains("invalid session");
  }

  private boolean parsePositiveInt(String value) {
    try {
      return Integer.parseInt(value.trim()) > 0;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private Map<String, Object> safeMap(Map<String, Object> value) {
    if (value == null) {
      return Map.of();
    }
    return value.entrySet().stream()
        .filter(entry -> entry.getKey() != null)
        .collect(
            Collectors.toMap(
                entry -> String.valueOf(entry.getKey()), Map.Entry::getValue, (a, b) -> a));
  }

  private List<Map<String, Object>> safeList(List<Map<String, Object>> value) {
    if (value == null) {
      return List.of();
    }
    return value.stream()
        .filter(Objects::nonNull)
        .map(this::safeMap)
        .collect(Collectors.toList());
  }

  private boolean isTrue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof String string) {
      return Objects.equals("true", string.toLowerCase());
    }
    return false;
  }

  private boolean containsToken(Map<String, Object> map, String key, String token) {
    Object value = map.get(key);
    if (value == null) {
      return false;
    }
    return String.valueOf(value).toLowerCase().contains(token.toLowerCase());
  }
}
