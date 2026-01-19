package org.openqa.selenium.locator.bidi.selenium.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTests")
class RingBufferTest {
  @Test
  void keepsNewestEntriesWhenOverCapacity() {
    RingBuffer<Integer> buffer = new RingBuffer<>(2);
    buffer.add(1);
    buffer.add(2);
    buffer.add(3);

    assertThat(buffer.snapshot()).containsExactly(2, 3);
    assertThat(buffer.size()).isEqualTo(2);
  }
}
