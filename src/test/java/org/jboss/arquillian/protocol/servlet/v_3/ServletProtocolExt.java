package org.jboss.arquillian.protocol.servlet.v_3;

import java.util.Collection;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet.ServletProtocolConfiguration;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutorExt;

public class ServletProtocolExt extends
    org.jboss.arquillian.protocol.servlet.v_3.ServletProtocol {
  @Override
  protected String getProtcolName() {
    return super.getProtcolName() + " Ext";
  }

  @Override
  public ServletMethodExecutor getExecutor(
      ServletProtocolConfiguration protocolConfiguration,
      ProtocolMetaData metaData, CommandCallback callback) {
    Collection<HTTPContext> contexts = metaData.getContexts(HTTPContext.class);
    if (contexts.size() == 0) {
      throw new IllegalArgumentException("No " + HTTPContext.class.getName()
          + " found in " + ProtocolMetaData.class.getName() + ". "
          + "Servlet protocol can not be used");
    }
    return new ServletMethodExecutorExt(protocolConfiguration, contexts, callback);
  }
  
  @Override
  public DeploymentPackager getPackager()
  {
     return new ServletProtocolDeploymentPackagerExt();
  }

}
