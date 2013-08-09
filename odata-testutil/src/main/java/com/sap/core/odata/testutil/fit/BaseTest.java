package com.sap.core.odata.testutil.fit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Provides basic support for JUnit tests<br>
 * - log & tracing
 * 
 * @author SAP AG
 */
public abstract class BaseTest {

  static {
    DOMConfigurator.configureAndWatch("/log4j.xml");
  }

  protected final Logger log = Logger.getLogger(this.getClass());

  private final Map<Class<?>, Level> disabledLoggings = new HashMap<Class<?>, Level>();

  /**
   * trace each junit error
   */
  @Rule
  public TestRule watch = new TestWatcher() {

    @Override
    protected void failed(final Throwable e, final Description description) {
      super.failed(e, description);
      log.info("starting " + description.getDisplayName());
      log.error(description.getDisplayName(), e);
    }

    @Override
    protected void starting(final Description description) {
      super.starting(description);
    }

    @Override
    protected void finished(final Description description) {
      super.finished(description);
      reEnableLogging();
    }

    @Override
    protected void succeeded(final Description description) {
      super.succeeded(description);
    }

  };

  /**
   * Disable logging.
   * <br /> 
   * Disabled logging will be automatically re-enabled after test execution (see {@link #reEnableLogging()}).
   */
  protected void disableLogging() {
    disableLogging(FitErrorCallback.class);
  }

  /**
   * Disable logging for over handed classes.
   * Disabled logging will be automatically re-enabled after test execution (see {@link #reEnableLogging()}).
   * 
   * @param classes
   */
  protected void disableLogging(final Class<?>... classes) {
    for (final Class<?> clazz : classes) {
      final org.apache.log4j.Logger l = org.apache.log4j.Logger.getLogger(clazz);
      if (l != null) {
        disabledLoggings.put(clazz, l.getEffectiveLevel());
        l.setLevel(Level.OFF);
      }
    }
  }

  protected void reEnableLogging() {
    for (final Entry<Class<?>, Level> entry : disabledLoggings.entrySet()) {
      final org.apache.log4j.Logger l = org.apache.log4j.Logger.getLogger(entry.getKey());
      l.setLevel(entry.getValue());
    }
    disabledLoggings.clear();
  }
}
