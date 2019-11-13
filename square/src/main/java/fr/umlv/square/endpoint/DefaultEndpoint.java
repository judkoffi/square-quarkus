package fr.umlv.square.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Default endpoint
 *
 */
@Path("/")
public class DefaultEndpoint {

  @GET
  public Response root() {
    return Response.ok().build();
  }

}
