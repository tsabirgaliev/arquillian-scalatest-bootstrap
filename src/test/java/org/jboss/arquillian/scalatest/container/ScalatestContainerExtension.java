package org.jboss.arquillian.scalatest.container;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ScalatestContainerExtension implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder builder)
  {
    builder.service(AuxiliaryArchiveAppender.class, ScalatestDeploymentAppender.class);
  }


}
