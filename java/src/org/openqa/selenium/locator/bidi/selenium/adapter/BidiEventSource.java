package org.openqa.selenium.locator.bidi.selenium.adapter;

import java.util.Set;
import java.util.function.Consumer;
import org.openqa.selenium.locator.bidi.model.BidiFeature;

interface BidiEventSource extends AutoCloseable {
  void start();

  void stop();

  Set<BidiFeature> supportedFeatures();

  void subscribeNetwork(Consumer<RawBidiEvent> handler);

  void subscribeConsole(Consumer<RawBidiEvent> handler);

  void subscribeNavigation(Consumer<RawBidiEvent> handler);

  @Override
  void close();
}
