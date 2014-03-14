package com.example;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/rest")
public class RestService {
  @Path("/greeter/{name}")
  public String sayHello(@PathParam("name") String name) {
    return "Hello, " + name;
  }
}
