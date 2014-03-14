package org.jboss.arquillian.test.spi;

import org.jboss.arquillian.test.spi.TestMethodExecutor;

/**
 * For use with test frameworks, where test case is 
 * reporesented by a String value, rather than a
 * test class method
 * 
 * @author <a href="mailto:tair.sabirgaliev@gmail.com">Tair Sabirgaliev</a>
 *
 */
public interface TestMethodExecutorExt extends TestMethodExecutor {
  /**
   * 
   * @return the name of a testCase
   */
  public String getTestName();
}
