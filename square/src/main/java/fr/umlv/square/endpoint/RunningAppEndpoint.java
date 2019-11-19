package fr.umlv.square.endpoint;

import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import fr.umlv.square.model.request.DeployInstanceRequest;
import fr.umlv.square.model.request.StopInstanceRequest;
import fr.umlv.square.service.DockerService;
import fr.umlv.square.util.SquareHttpStatusCode;

@Path("/app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningAppEndpoint {

  private final Validator validator;
  private final DockerService dockerService;

  @Inject
  public RunningAppEndpoint(DockerService dockerService, Validator validator) {
    this.dockerService = dockerService;
    this.validator = validator;
  }

  @POST
  @Path("/deploy")
  /**
   * Endpoint to start an app
   * 
   * @param request : a JSON which defines the name of the app and its port number
   * @return : a Response in JSON which give information about the app deployed
   */
  public Response deploy(DeployInstanceRequest request) {
    var violations = validator.validate(request);

    if (!violations.isEmpty()) {
      return Response
        .status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE)
        .entity("Invalid post body")
        .build();
    }

    var deployResponse = dockerService.runContainer(request.getAppName(), request.getPort());

    var entityBody = deployResponse.isPresent() ? deployResponse.get().toJson() : "{}";

    return Response.status(SquareHttpStatusCode.CREATED_STATUS_CODE).entity(entityBody).build();
  }

  @GET
  @Path("/list")
  /**
   * Endpoint to list all the instances of the docker container
   * 
   * @return : a Response in JSON with all the informations of the app listed
   */
  public Response list() {
    var runningApps = dockerService.getRunnningList();

    var list = runningApps.isEmpty() ? new ArrayList<String>()
        : runningApps.get().stream().map((mapper) -> mapper.toJson()).collect(Collectors.toList());
    return Response.ok().entity(list.toString()).build();
  }

  @POST
  @Path("/stop")
  /**
   * Endpoint to stop an app
   * 
   * @param request : a JSON which give the id of the app to stop
   * @return : a Response in JSON which give the information of the app at the moment of its stop
   */
  public Response stop(StopInstanceRequest request) {
    var violations = validator.validate(request);

    if (!violations.isEmpty()) {
      return Response
        .status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE)
        .entity("Invalid post body")
        .build();
    }

    var optional = dockerService.stopApp(request.getId());
    var result = optional.isEmpty() ? "{}" : optional.get().toJson();

    return Response.status(SquareHttpStatusCode.CREATED_STATUS_CODE).entity(result).build();
  }

}
