package org.jboss.arquillian.scalatest

import org.jboss.arquillian.container.test.spi.TestRunnerExt
import org.jboss.arquillian.test.spi.TestResult
import org.scalatest.Suite
import org.scalatest.Reporter
import org.scalatest.Args
import org.scalatest.events.Event
import org.scalatest.events.TestFailed
import org.scalatest.events.RecordableEvent
import org.scalatest.events.Ordinal
import org.scalatest.events.Location
import org.scalatest.events.Formatter
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor

class ScalaTestRunner extends TestRunnerExt {
  override def execute(testClass: Class[_], methodName: String): TestResult = throw new UnsupportedOperationException
  override def execute(testClass: Class[_], methodName: String, testName: String): TestResult = {
    if (classOf[Suite].isAssignableFrom(testClass)) {
      var lastFailedOption: Option[TestFailed] = None
      val reporter = new Reporter {
        def apply(event: Event) = event match {
          case f : TestFailed => lastFailedOption = Some(f)
          case _ =>
        }
      }
      
      val test = testClass.asSubclass(classOf[Suite]).newInstance()
      
      val testRunnerAdaptor = TestRunnerAdaptorBuilder.build()
      
      testRunnerAdaptor.beforeSuite()
      testRunnerAdaptor.beforeClass(test.getClass(), LifecycleMethodExecutor.NO_OP)
      testRunnerAdaptor.before(test, test.getClass().getMethod("run", classOf[Option[String]], classOf[Args]), LifecycleMethodExecutor.NO_OP)
    
      //val testResult = testRunnerAdaptor.test(new ScalaTestMethodExecutor(test, "A Set when empty should have size 0"))
      val status = test.run(Some(testName), Args(reporter))
  
      testRunnerAdaptor.after(test, test.getClass().getMethod("run", classOf[Option[String]], classOf[Args]), LifecycleMethodExecutor.NO_OP)
      testRunnerAdaptor.afterClass(test.getClass(), LifecycleMethodExecutor.NO_OP)
      testRunnerAdaptor.afterSuite()
      
      testRunnerAdaptor.shutdown();
  
      if (status.succeeds) {
        new TestResult(TestResult.Status.PASSED)
      } else {
        new TestResult(TestResult.Status.FAILED, lastFailedOption.flatMap(_.throwable).getOrElse(null))
      }
    } else {
      new TestResult(TestResult.Status.SKIPPED, new Exception("testClass must be a sublclass of org.scalatest.Suite"))
    }
  }

}
