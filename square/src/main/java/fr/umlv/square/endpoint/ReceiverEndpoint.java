package fr.umlv.square.endpoint;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import fr.umlv.square.model.request.ClientLogRequest;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.service.DockerService;
import fr.umlv.square.service.LogService;

/**
 * This class defines all the endpoints which begin with "/container-log"
 */

@Path("/container-log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiverEndpoint {

  private final LogService logService;
  private final DockerService dockerService;

  @Inject
  public ReceiverEndpoint(DockerService dockerService, LogService logService) {
    this.dockerService = dockerService;
    this.logService = logService;
  }

  // Convert a date as a String into a date as a Timestamp
  private static Timestamp convertStringToTimestamp(String strDate) {
    try {
      var formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
      var date = formatter.parse(strDate);
      var timeStampDate = new Timestamp(date.getTime());
      return timeStampDate;
    } catch (ParseException e) {
      System.out.println("Exception :" + e);
      return null;
    }
  }

  /**
   * Endpoint to receive log from square-client lib store inside each docker instance
   * @param request: an {@ClientLogRequest} which contain request from an docker instance
   */
  @POST
  @Path("/send-log/")
  public Response processReceivedLog(ClientLogRequest request) {
    var originInstance = request.getDockerInstance();
    var imageInfo = dockerService.findImageInfoByDockerInstance(originInstance);
    var logs = request.getLogs();

    var entities = logs
      .stream()//
      .map((log) -> new LogEntity(imageInfo.getSquareId(), convertStringToTimestamp(log.getDate()), log.getLevel(), log.getMessage(), request.getDockerInstance(), imageInfo.getImageName(), imageInfo.getAppPort(), imageInfo.getServicePort()))//
      .collect(Collectors.toList());

    logService.saveLogs(entities);
    return Response.ok().build();
  }

  /**
   * Endpoint to receive all the logs of the app
   * @param request: an {@ClientLogRequest} which contain request from an docker instance : useless here
   * @return a List of logs
   */
  @GET
  @Path("/logs/")
  public List<LogEntity> list(ClientLogRequest request) {
    return logService.getAllLogs();
  }

}
