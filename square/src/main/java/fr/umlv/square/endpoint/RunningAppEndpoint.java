package fr.umlv.square.endpoint;

import java.util.ArrayList;
import java.util.Random;
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
import fr.umlv.square.model.response.RunningInstanceInfo;
import fr.umlv.square.service.DockerService;

@Path("/app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningAppEndpoint {

  @Inject
  Validator validator;

  @Inject
  DockerService dockerService;

  @POST
  @Path("/deploy")
  /**
   * Road to start an app
   * 
   * @param request : a JSON which defines the name of the app and its port number
   * @return : a Response in JSON which give information about the app deployed
   */
  public Response deploy(DeployInstanceRequest request) {
    var violations = validator.validate(request);

    if (!violations.isEmpty()) {
      return Response.status(400).entity("Invalid post body").build();
    }

    var deployResponse = dockerService.runContainer(request.getAppName(), request.getPort());

    var entityBody = deployResponse.isPresent() ? deployResponse.get().toJson() : "";

    System.out.println(deployResponse);

    return Response.ok().entity(entityBody).build();
  }

  @GET
  @Path("/list")
  /**
   * Road to list all the instances of the docker container
   * 
   * @return : a Response in JSON with all the informations of the app listed
   */
  public Response list() {

    var list = new ArrayList<String>();

    for (int i = 0; i < 5; i++) {
      list
        .add(new RunningInstanceInfo(new Random().nextInt(), "appName_" + i, 8000 + i, 8000 + i,
            "appName_" + i, "1m50").toJson());
    }

    return Response.ok().entity(list.toString()).build();
  }

  @POST
  @Path("/stop")
  /**
   * Road to stop an app
   * 
   * @param request : a JSON which give the id of the app to stop
   * @return : a Response in JSON which give the information of the app at the moment of its stop
   */
  public Response stop(StopInstanceRequest request) {
    var violations = validator.validate(request);

    if (!violations.isEmpty()) {
      return Response.status(400).entity("Invalid post body").build();
    }

    var result =
        new RunningInstanceInfo(request.getId(), "appName_", 8000, 8000, "appName_", "1m50");

    return Response.ok().entity(result.toJson()).build();
  }

}
