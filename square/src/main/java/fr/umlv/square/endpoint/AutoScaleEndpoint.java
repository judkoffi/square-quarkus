package fr.umlv.square.endpoint;

import java.util.Map;
import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import fr.umlv.square.service.AutoScaleService;
import fr.umlv.square.util.Helper;
import fr.umlv.square.util.SquareHttpStatusCode;

/**
 * This class defines all the endpoints which begin with "/auto-scale"
 */

@Path("/auto-scale")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AutoScaleEndpoint {

  private final AutoScaleService autoScaleService;

  @Inject
  public AutoScaleEndpoint(AutoScaleService autoScaleService) {
    this.autoScaleService = autoScaleService;
  }

  private static boolean validRequest(Map<String, Integer> request) {
    return request.entrySet().stream().allMatch(p ->
    {
      var v = p.getKey();
      return (p.getValue() >= 0) && !v.isBlank() && !v.isEmpty() && v.contains(":")
          && Helper.isNumeric(v.split(":")[1]);
    });
  }

  @POST
  @Path("/update")
  /**
   * Endpoint to update the number of instances of an app
   * 
   * @param request the JSON request
   * @return a Response in JSON
   */
  /*
   * use a map because we don't know the name of the key
   */
  public Response update(Map<String, Integer> request) {
    if (request.isEmpty())
      return Response.status(SquareHttpStatusCode.CREATED_STATUS_CODE).entity("{}").build();

    if (!validRequest(request))
      return Response.status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE).build();


    if (!autoScaleService.isStarted()) {
      autoScaleService.start();
    }

    autoScaleService.updateScalingConfig(request);
    var body = autoScaleService.getScalingStatus();
    try (var jsonBuilder = JsonbBuilder.create()) {
      return Response
        .status(SquareHttpStatusCode.CREATED_STATUS_CODE)
        .entity(jsonBuilder.toJson(body))
        .build();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @GET
  @Path("/status")
  /**
   * Endpoint to know what are the actions that the auto scale has to do
   * 
   * @return a Response in JSON
   */
  public Response status() {
    var body = autoScaleService.getScalingStatus();
    try (var jsonBuilder = JsonbBuilder.create()) {
      return Response.ok().entity(jsonBuilder.toJson(body)).build();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @GET
  @Path("/stop")
  /**
   * Endpoint to stop auto scale
   * 
   * @return a Response JSON which give the number of instance handled by auto scale
   */
  public Response stop() {
    autoScaleService.stop();
    var body = autoScaleService.getScalingConfig();
    try (var jsonBuilder = JsonbBuilder.create()) {
      return Response.ok().entity(jsonBuilder.toJson(body)).build();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
