package org.jboss.arquillian.protocol.servlet;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Timer;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet.ServletProtocolConfiguration;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestMethodExecutorExt;

public class ServletMethodExecutorExt extends ServletMethodExecutor {
  public ServletMethodExecutorExt(ServletProtocolConfiguration config,
      Collection<HTTPContext> contexts, final CommandCallback callback) {
    super(config, contexts, callback);
  }

  @Override
  public TestResult invoke(TestMethodExecutor testMethodExecutor) {
    if (testMethodExecutor instanceof TestMethodExecutorExt) {
      return invoke((TestMethodExecutorExt) testMethodExecutor);
    } else {
      return super.invoke(testMethodExecutor);
    }
  }

  public TestResult invoke(TestMethodExecutorExt testMethodExecutor) {
    if (testMethodExecutor == null)
    {
       throw new IllegalArgumentException("TestMethodExecutor must be specified");
    }

    URI targetBaseURI = uriHandler.locateTestServlet(testMethodExecutor.getMethod());
    
    String testName = testMethodExecutor.getTestName();
    try {
      testName = URLEncoder.encode(testMethodExecutor.getTestName(), "ISO-8859-1");
    } catch (Throwable t) {
      throw new RuntimeException("Unable to url-encode testName", t);
    }

    Class<?> testClass = testMethodExecutor.getInstance().getClass();
    final String url = targetBaseURI.toASCIIString() + ARQUILLIAN_SERVLET_MAPPING
          + "?outputMode=serializedObject&className=" + testClass.getName() + "&methodName="
          + testMethodExecutor.getMethod().getName() + "&testName=" + testName;

    final String eventUrl = targetBaseURI.toASCIIString() + ARQUILLIAN_SERVLET_MAPPING
          + "?outputMode=serializedObject&className=" + testClass.getName() + "&methodName="
          + testMethodExecutor.getMethod().getName() + "&testName=" + testName + "&cmd=event";

    Timer eventTimer = null;
    try
    {
       eventTimer = createCommandServicePullTimer(eventUrl);
       return executeWithRetry(url, TestResult.class);
    }
    catch (Exception e)
    {
       throw new IllegalStateException("Error launching test " + testClass.getName() + "."
             + testMethodExecutor.getMethod() + "(" + testName + ")", e);
    }
    finally
    {
       if (eventTimer != null)
       {
          eventTimer.cancel();
       }
    }
  }
}
