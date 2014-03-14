package com.example

import scala.collection.immutable.Set
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.scalatest.WordSpec
import javax.inject.Inject
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.PropSpec

//class ScalatestExampleTest extends WordSpec {
class ScalatestExampleTest extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  
  @Inject
  var restService: RestService = _
  
  property("property1") {
    forAll("name") { name: String =>
      restService.sayHello(name) should equal ("Hello, " + name)
    }
  } 

//  "A Set" when {
//    "empty" should {
//      "have size 0" in {
//        assert(Set.empty.size == 0)
//      }
//
//      "produce NoSuchElementException when head is invoked" in {
//        intercept[NoSuchElementException] {
//          Set.empty.head
//        }
//      }
//    }
//  }
}

object ScalatestExampleTest {
  @Deployment
  def createTestArchive() = {
    ShrinkWrap.create(classOf[WebArchive], "test.war")
      .addClasses(classOf[RestService])
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
  }
}
