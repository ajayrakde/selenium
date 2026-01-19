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

package org.openqa.selenium.grid.node;

import org.openqa.selenium.grid.data.SessionClosedData;
import org.openqa.selenium.grid.data.SessionCreatedData;
import org.openqa.selenium.grid.data.SessionEventData;

/**
 * Service Provider Interface (SPI) for receiving session lifecycle events. Implementations of this
 * interface can be registered via Java's ServiceLoader mechanism to receive notifications when
 * sessions are created or closed on a node.
 *
 * <p>This interface is designed for sidecar services that need to perform actions in response to
 * session lifecycle events, such as:
 *
 * <ul>
 *   <li>Starting/stopping video recording
 *   <li>Collecting and uploading logs
 *   <li>Sending notifications
 *   <li>Updating external dashboards
 *   <li>Managing resources tied to session lifecycle
 * </ul>
 *
 * <h2>Registration</h2>
 *
 * <p>To register an implementation, create a file named:
 *
 * <pre>
 * META-INF/services/org.openqa.selenium.grid.node.SessionLifecycleListener
 * </pre>
 *
 * <p>containing the fully qualified class name of your implementation.
 *
 * <h2>Example Implementation</h2>
 *
 * <pre>{@code
 * public class VideoRecorderListener implements SessionLifecycleListener {
 *
 *   @Override
 *   public void onSessionCreated(SessionCreatedData data) {
 *     String sessionId = data.getSessionId().toString();
 *     System.out.println("Starting video recording for session: " + sessionId);
 *     VideoRecorder.start(sessionId);
 *   }
 *
 *   @Override
 *   public void onSessionClosed(SessionClosedData data) {
 *     String sessionId = data.getSessionId().toString();
 *     String reason = data.getReason().name();
 *     System.out.println("Stopping video recording for session: " + sessionId);
 *     VideoRecorder.stopAndUpload(sessionId, reason);
 *   }
 * }
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Implementations must be thread-safe as callbacks may be invoked concurrently from multiple
 * threads.
 *
 * <h2>Error Handling</h2>
 *
 * <p>Exceptions thrown by listener methods are caught and logged but do not affect the session
 * lifecycle. Implementations should handle their own errors gracefully.
 *
 * @see SessionCreatedData
 * @see SessionClosedData
 */
public interface SessionLifecycleListener {

  /**
   * Called when a new session is successfully created on a node.
   *
   * <p>This method is invoked after the session has been fully initialized and is ready for use.
   * The provided data includes session ID, node information, capabilities, and start time.
   *
   * @param data the session creation data containing full context about the new session
   */
  void onSessionCreated(SessionCreatedData data);

  /**
   * Called when a session is closed on a node.
   *
   * <p>This method is invoked after the session has been stopped but before cleanup is complete.
   * The provided data includes session ID, closure reason, node information, capabilities, and
   * timing information.
   *
   * @param data the session closure data containing full context about the closed session
   */
  void onSessionClosed(SessionClosedData data);

  /**
   * Called when a user-defined session event is fired from the client.
   *
   * <p>This method allows clients to send custom events through the Grid. Common use cases include:
   *
   * <ul>
   *   <li>Test lifecycle events (test:started, test:passed, test:failed)
   *   <li>Log collection requests (log:collect)
   *   <li>Screenshot capture requests (screenshot:capture)
   *   <li>Custom markers for video annotation (marker:add)
   * </ul>
   *
   * <p>Default implementation does nothing. Override to handle custom events.
   *
   * @param data the session event data containing event type, session ID, and optional payload
   */
  default void onSessionEvent(SessionEventData data) {
    // Default implementation does nothing
    // Override to handle custom events
  }
}
