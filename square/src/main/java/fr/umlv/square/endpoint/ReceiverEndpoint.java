package fr.umlv.square.endpoint;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import fr.umlv.square.model.request.ClientLogRequest;
import fr.umlv.square.orm.LogEntity;
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
  public void processReceivedLog(ClientLogRequest request) {
    var originInstance = request.getDockerInstance();
    var squareId = dockerService.findSquareIdFromContainerId(originInstance);
    var appName = dockerService.findAppNameFromContainerId(originInstance);

    var logs = request.getLogs();
    if (logs.isEmpty())
      return;

    System.out.println("logs from: " + squareId);
    System.out.println("nblogs: " + logs.size());

    var entities = logs
      .stream()//
      .map((log) -> new LogEntity(squareId, log.getDate(), log.getLevel(), log.getMessage(), originInstance, appName)).collect(Collectors.toList());

    System.out.println(entities);

    // logService.saveLogs(entities);
  }

  @GET
  @Path("/logs/")
  public List<LogEntity> list(ClientLogRequest request) {
    return logService.getAllLogs();
  }

}
