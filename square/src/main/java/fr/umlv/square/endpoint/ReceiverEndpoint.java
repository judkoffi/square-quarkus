package fr.umlv.square.endpoint;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import fr.umlv.square.model.request.ClientLogRequest;
import fr.umlv.square.service.DockerService;
import fr.umlv.square.service.LogService;

@Path("/container-log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiverEndpoint {

  @Inject
  LogService logService;

  @Inject
  DockerService dockerService;

  /**
   * Endpoint to receive log from square-client lib store insde each docker instance
   * 
   * @param request: an {@ClientLogRequest} which contain request from an docker instance
   */
  @POST
  @Path("/send-log/")
  public void kawaii(ClientLogRequest request) {
    var squareId = dockerService.findIdFromContainerId(request.getContainer());
    var logs = request.getLogs();

    System.out.println(logs + "for square id " + squareId);

    if (logs.isEmpty())
      return;

  }

}
