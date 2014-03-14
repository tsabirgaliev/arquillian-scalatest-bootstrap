package org.jboss.arquillian.scalatest.container;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.scalatest.ScalaTestRunner;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class ScalatestDeploymentAppender extends CachedAuxilliaryArchiveAppender {

  @Override
  protected Archive<?> buildArchive()
  {
     JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-scalatest.jar")
                       .addPackages(
                             true, 
                             "scala", 
                             "org.scalatest",
                             "org.scalacheck",
                             "org.scalautils",
                             ScalaTestRunner.class.getPackage().getName())
                       .addAsResource("org/scalatest/ScalaTestBundle.properties")
                       .addAsServiceProvider(
                             TestRunner.class, 
                             ScalaTestRunner.class);
     return archive;
  }

}
