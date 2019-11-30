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
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.umlv.square.model.request.ClientLogRequest;
import fr.umlv.square.model.request.ClientStatusRequest;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.service.DockerService;
import fr.umlv.square.service.LogService;
import fr.umlv.square.util.Helper;

/**
 * This class defines all the endpoints which begin with "/container-log"
 */

@Path("/container-log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiverEndpoint {

  private final LogService logService;
  private final DockerService dockerService;
  private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverEndpoint.class);

  @Inject
  public ReceiverEndpoint(DockerService dockerService, LogService logService) {
    this.dockerService = dockerService;
    this.logService = logService;
  }

  /**
   * Endpoint to receive log from square-client lib store inside each docker instance
   * 
   * @param request: an {@ClientLogRequest} which contain request from an docker instance
   */
  @POST
  @Path("/send-log/")
  public Response processReceivedLog(ClientLogRequest request) {
    var originInstance = request.getDockerInstance();
    var imageInfo = dockerService.findImageInfoByDockerInstance(originInstance);
    var logs = request.getLogs();

    var entities = logs
      .stream()
      .map(log -> new LogEntity(imageInfo.getSquareId(),
          Helper.convertStringToTimestamp(log.getDate(), "yyyy-MM-dd HH:mm:ss,SSS"), log.getLevel(),
          log.getMessage(), request.getDockerInstance(), imageInfo.getImageName(),
          imageInfo.getAppPort(), imageInfo.getServicePort()))
      .collect(Collectors.toList());

    logService.saveLogs(entities);
    return Response.ok().build();
  }

  /**
   * Endpoint to receive status of app running inside docker from square-client lib
   * 
   * @param request: an {ClientStatusRequest} which contain request from an docker instance which
   *        notify if app was killed
   */
  @POST
  @Path("/status/")
  public Response processReceivedAppStatus(ClientStatusRequest request) {
    var instance = dockerService.findImageInfoByDockerInstance(request.getDockerInstance());
    LOGGER.info("instance {} is killed ", instance.getDockerInstance());
    dockerService.updateDockerInstanceStatus(instance, request.getStatus());
    var r = dockerService.stopApp(instance.getSquareId());
    if (r.isEmpty()) {
      LOGGER.info("Failed to kill instance");
    } else {
      LOGGER.info("success to kill instance");
    }
    return Response.ok().build();
  }

  /**
   * Endpoint to receive all the logs of the app
   * 
   * @param request: an {@ClientLogRequest} which contain request from an docker instance : useless
   *        here
   * @return a List of logs
   */
  @GET
  @Path("/logs/")
  public List<LogEntity> list(ClientLogRequest request) {
    return logService.getAllLogs();
  }

}
