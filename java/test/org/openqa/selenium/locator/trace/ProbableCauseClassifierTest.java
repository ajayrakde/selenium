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

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.LocatorAmbiguousException;

class ProbableCauseClassifierTest {

  private final DefaultProbableCauseClassifier classifier = new DefaultProbableCauseClassifier();

  @Test
  void strictnessViolationsWin() {
    LocatorAmbiguousException ex = new LocatorAmbiguousException("Strictness mismatch");
    ClassificationInput input =
        new ClassificationInput(
            ex, FailureType.ACTION_FAILURE, Map.of(), Map.of(), List.of(), Map.of());
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.STRICTNESS_VIOLATION);
  }

  @Test
  void clickInterceptedDetectedFromActionDiagnostics() {
    ClassificationInput input =
        new ClassificationInput(
            null,
            FailureType.ACTION_FAILURE,
            Map.of("coveredByElement", true),
            Map.of(),
            List.of(),
            Map.of());
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.CLICK_INTERCEPTED_OVERLAY);
  }

  @Test
  void movingElementDetectedFromStabilitySamples() {
    ClassificationInput input =
        new ClassificationInput(
            null,
            FailureType.SYNC_TIMEOUT,
            Map.of(),
            Map.of(),
            List.of(Map.of("moving", true)),
            Map.of());
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.ELEMENT_MOVING_ANIMATION);
  }

  @Test
  void networkBusyDetectedFromBidiInflight() {
    ClassificationInput input =
        new ClassificationInput(
            null,
            FailureType.SYNC_TIMEOUT,
            Map.of(),
            Map.of(),
            List.of(),
            Map.of("inflightRequests", 2));
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.NETWORK_BUSY);
  }

  @Test
  void consoleDenylistDetectedFromBidiSnapshot() {
    ClassificationInput input =
        new ClassificationInput(
            null,
            FailureType.SYNC_TIMEOUT,
            Map.of(),
            Map.of(),
            List.of(),
            Map.of("consoleDenylistMatch", true));
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.CONSOLE_ERROR_BLOCKING);
  }

  @Test
  void driverDisconnectDetectedFromThrowable() {
    RuntimeException ex = new RuntimeException("Driver disconnected unexpectedly");
    ClassificationInput input =
        new ClassificationInput(ex, FailureType.INFRA_FAILURE, Map.of(), Map.of(), List.of(), Map.of());
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.DRIVER_DISCONNECTED);
  }

  @Test
  void timeoutUnknownIsDefault() {
    ClassificationInput input =
        new ClassificationInput(null, FailureType.SYNC_TIMEOUT, Map.of(), Map.of(), List.of(), Map.of());
    assertThat(classifier.classify(input)).isEqualTo(ProbableCause.TIMEOUT_UNKNOWN);
  }
}
