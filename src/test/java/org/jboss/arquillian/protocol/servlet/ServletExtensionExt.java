package org.jboss.arquillian.protocol.servlet;

import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ServletExtensionExt implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder builder) {
    builder.service(Protocol.class, org.jboss.arquillian.protocol.servlet.v_3.ServletProtocolExt.class);
  }

}
