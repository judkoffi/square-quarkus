package fr.umlv.square.endpoint;

import java.time.LocalDateTime;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 
 * Endpoint use to know if API is alive or not
 *
 */
@Path("/healthcheck")
@Produces(MediaType.APPLICATION_JSON)
public class HealthCheckEndpoint {

  @GET
  public Response health() {
    var body = "{\"status\": \"Yop yop\", \"date\":\"" + LocalDateTime.now() + "\"}";
    return Response.ok().entity(body).build();
  }

}
