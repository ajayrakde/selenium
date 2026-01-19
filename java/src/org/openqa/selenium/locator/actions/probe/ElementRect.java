package org.openqa.selenium.locator.actions.probe;

public record ElementRect(double left, double top, double right, double bottom, double width, double height) {
  public double centerX() {
    return left + width / 2.0;
  }

  public double centerY() {
    return top + height / 2.0;
  }
}
