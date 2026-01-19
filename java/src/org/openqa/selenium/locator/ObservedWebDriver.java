package org.openqa.selenium.locator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.Logs;

/**
 * Optional wrapper that observes direct WebDriver usage without changing behavior.
 */
public class ObservedWebDriver implements WebDriver, JavascriptExecutor {
  private static final Logger LOG = Logger.getLogger(ObservedWebDriver.class.getName());

  private final WebDriver delegate;

  public ObservedWebDriver(WebDriver delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
  }

  @Override
  public void get(String url) {
    delegate.get(url);
  }

  @Override
  public String getCurrentUrl() {
    return delegate.getCurrentUrl();
  }

  @Override
  public String getTitle() {
    return delegate.getTitle();
  }

  @Override
  public List<WebElement> findElements(By by) {
    LOG.log(Level.FINE, "Direct findElements usage detected: {0}", by);
    return delegate.findElements(by);
  }

  @Override
  public WebElement findElement(By by) {
    LOG.log(Level.FINE, "Direct findElement usage detected: {0}", by);
    return delegate.findElement(by);
  }

  @Override
  public String getPageSource() {
    return delegate.getPageSource();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void quit() {
    delegate.quit();
  }

  @Override
  public Set<String> getWindowHandles() {
    return delegate.getWindowHandles();
  }

  @Override
  public String getWindowHandle() {
    return delegate.getWindowHandle();
  }

  @Override
  public TargetLocator switchTo() {
    return delegate.switchTo();
  }

  @Override
  public Navigation navigate() {
    return delegate.navigate();
  }

  @Override
  public Options manage() {
    return delegate.manage();
  }

  @Override
  public Object executeScript(String script, Object... args) {
    if (delegate instanceof JavascriptExecutor executor) {
      return executor.executeScript(script, args);
    }
    throw new UnsupportedOperationException("Delegate does not support JavascriptExecutor");
  }

  @Override
  public Object executeAsyncScript(String script, Object... args) {
    if (delegate instanceof JavascriptExecutor executor) {
      return executor.executeAsyncScript(script, args);
    }
    throw new UnsupportedOperationException("Delegate does not support JavascriptExecutor");
  }

  public Logs logs() {
    if (delegate instanceof org.openqa.selenium.HasLogs hasLogs) {
      return hasLogs.getLogs();
    }
    throw new UnsupportedOperationException("Delegate does not support logs");
  }
}
