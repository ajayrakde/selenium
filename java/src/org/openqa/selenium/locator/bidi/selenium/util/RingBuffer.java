package org.openqa.selenium.locator.bidi.selenium.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class RingBuffer<T> {
  private final int capacity;
  private final Deque<T> deque;
  private final ReentrantLock lock = new ReentrantLock();

  public RingBuffer(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be > 0");
    }
    this.capacity = capacity;
    this.deque = new ArrayDeque<>(capacity);
  }

  public int capacity() {
    return capacity;
  }

  public void add(T item) {
    if (item == null) {
      return;
    }
    lock.lock();
    try {
      if (deque.size() == capacity) {
        deque.removeFirst();
      }
      deque.addLast(item);
    } finally {
      lock.unlock();
    }
  }

  public List<T> snapshot() {
    lock.lock();
    try {
      return new ArrayList<>(deque);
    } finally {
      lock.unlock();
    }
  }

  public int size() {
    lock.lock();
    try {
      return deque.size();
    } finally {
      lock.unlock();
    }
  }

  public void clear() {
    lock.lock();
    try {
      deque.clear();
    } finally {
      lock.unlock();
    }
  }
}
