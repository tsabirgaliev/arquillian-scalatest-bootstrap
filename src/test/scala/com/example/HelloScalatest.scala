package com.example

import org.scalatest._
import org.scalatest.events._
import org.jboss.arquillian.test.spi._
import org.jboss.arquillian.scalatest._

object HelloScalatest extends App {
  val test: Suite = new ScalatestExampleTest

  println(test.testNames)
  
  val testName = "property1"
  val testRunnerAdaptor = TestRunnerAdaptorBuilder.build()
  
  try {
    val methodExecutor = new ScalaTestMethodExecutor(test, testName)

    testRunnerAdaptor.beforeSuite()
    testRunnerAdaptor.beforeClass(test.getClass(), LifecycleMethodExecutor.NO_OP)
    testRunnerAdaptor.before(test, methodExecutor.getMethod, LifecycleMethodExecutor.NO_OP)
  
    val testResult = testRunnerAdaptor.test(methodExecutor)
    println(testResult)
    println(testResult.getThrowable())

    testRunnerAdaptor.after(test, methodExecutor.getMethod, LifecycleMethodExecutor.NO_OP)
    testRunnerAdaptor.afterClass(test.getClass(), LifecycleMethodExecutor.NO_OP)
    testRunnerAdaptor.afterSuite()
  } catch {
    case e: Throwable => e.printStackTrace()
  } finally {
    testRunnerAdaptor.shutdown()
  }

}
