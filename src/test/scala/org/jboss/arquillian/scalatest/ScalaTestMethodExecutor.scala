package org.jboss.arquillian.scalatest

import org.scalatest.Suite
import org.jboss.arquillian.test.spi.TestMethodExecutorExt
import org.scalatest.Args

class ScalaTestMethodExecutor(val test: Suite, val testName: String) extends TestMethodExecutorExt {
    def getMethod() = classOf[Suite].getMethod("run", classOf[Option[String]], classOf[Args])
    def getInstance() = test
    def invoke(parameters: Object*) = {
      test.run(
          parameters(0).asInstanceOf[Option[String]], 
          parameters(1).asInstanceOf[Args]
      )
      ()
    }
    
    def getTestName() = testName

}