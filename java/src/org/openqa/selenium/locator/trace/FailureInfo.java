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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public interface FailureInfo {
  FailureType failureType();

  ProbableCause probableCause();

  String message();

  String exceptionClass();

  List<String> stacktraceLines();

  int primaryStepIndex();

  Throwable throwable();

  static FailureInfo fromThrowable(Throwable t, FailureType type, int stepIndex) {
    return new SimpleFailureInfo(
        type,
        ProbableCause.TIMEOUT_UNKNOWN,
        String.valueOf(t.getMessage()),
        t.getClass().getName(),
        stackLines(t, 200),
        stepIndex,
        t);
  }

  static FailureInfo withProbableCause(FailureInfo info, ProbableCause probableCause) {
    if (info == null) {
      return null;
    }
    if (info instanceof SimpleFailureInfo simple) {
      return new SimpleFailureInfo(
          info.failureType(),
          probableCause,
          info.message(),
          info.exceptionClass(),
          info.stacktraceLines(),
          info.primaryStepIndex(),
          info.throwable());
    }
    return new SimpleFailureInfo(
        info.failureType(),
        probableCause,
        info.message(),
        info.exceptionClass(),
        info.stacktraceLines(),
        info.primaryStepIndex(),
        info.throwable());
  }

  static List<String> stackLines(Throwable t, int maxLines) {
    String s = stackToString(t);
    String[] lines = s.split("\\R");
    int n = Math.min(maxLines, lines.length);
    return Arrays.asList(lines).subList(0, n);
  }

  static String stackToString(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}
