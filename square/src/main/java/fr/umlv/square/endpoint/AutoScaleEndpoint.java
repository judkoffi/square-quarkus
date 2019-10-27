package fr.umlv.square.endpoint;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;

import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auto-scale")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AutoScaleEndpoint {

  @POST
  @Path("/update")
  /**
   * Road to update the number of instances of an app
   * @param request the JSON request
   * @return a Response in JSON
   */
  public Response update(Map<String, Integer> request) {
    var jsonBuilder = JsonbBuilder.create();
    var map = request.entrySet().stream()
        .collect(toMap(x -> x.getKey(), x -> "need to start " + x.getValue() + " instance(s)"));
    return Response.ok().entity(jsonBuilder.toJson(map)).build();
  }

  @GET
  @Path("/status")
  /**
   * Road to know what are the actions that the auto scale has to do
   * @return a Response in JSON
   */
  public Response status() {
    var jsonBuilder = JsonbBuilder.create();
    var hashmap = new HashMap<String, String>();
    hashmap.put("todomvc:8082", "no action");
    hashmap.put("demo:8083", "need to stop 1 instance(s)");
    return Response.ok().entity(jsonBuilder.toJson(hashmap)).build();
  }

  @GET
  @Path("/stop")
  /**
   * Road to stop auto scale
   * @return a Response JSON which give the number of instance handled by auto scale
   */
  public Response stop() {
    var jsonBuilder = JsonbBuilder.create();
    var hashmap = new HashMap<String, Integer>();
    hashmap.put("todomvc:8082", 2);
    hashmap.put("demo:8083", 1);
    return Response.ok().entity(jsonBuilder.toJson(hashmap)).build();
  }

}
