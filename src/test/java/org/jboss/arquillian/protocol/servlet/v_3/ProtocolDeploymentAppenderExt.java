package org.jboss.arquillian.protocol.servlet.v_3;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.protocol.servlet.runner.ServletRemoteExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class ProtocolDeploymentAppenderExt implements AuxiliaryArchiveAppender{
  @Override
  public JavaArchive createAuxiliaryArchive()
  {
     // Load based on package to avoid ClassNotFoundException on HttpServlet when loading ServletTestRunner
     return ShrinkWrap.create(JavaArchive.class, "arquillian-protocol.jar")
                    .addPackage(ServletRemoteExtension.class.getPackage()) // servlet.runner
                    .addAsManifestResource(
                          "org/jboss/arquillian/protocol/servlet/v_3/web-fragment-ext.xml",
                          "web-fragment.xml")
                    .addAsServiceProvider(RemoteLoadableExtension.class, ServletRemoteExtension.class);
  }
}

