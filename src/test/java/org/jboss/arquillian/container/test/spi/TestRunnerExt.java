package org.jboss.arquillian.container.test.spi;

import org.jboss.arquillian.test.spi.TestResult;

public interface TestRunnerExt extends TestRunner {
  TestResult execute(Class<?> testClass, String methodName, String testName);
}
