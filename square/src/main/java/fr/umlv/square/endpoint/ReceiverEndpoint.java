package fr.umlv.square.endpoint;

import java.io.IOException;
import java.util.Arrays;
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
import fr.umlv.square.service.DockerService;
import fr.umlv.square.service.LogInfo;
import fr.umlv.square.service.LogService;
import fr.umlv.square.util.SquareDelimiter;

@Path("/container-log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiverEndpoint {

  @Inject
  LogService logService;

  @Inject
  DockerService dockerService;

  @POST
  @Path("/send-log/")
  public void kawaii(ClientLogRequest request) throws IOException {
    var squareId = dockerService.findIdFromContainerId(request.getContainer());
    var logs = request.getMessage();

    if (logs.equals("[]"))
      return;

    var array = List.of(logs);

    var parsedLogs =
        array.stream().map((mapper) -> mapper.split(SquareDelimiter.LOG_MODEL_REQUEST_DELIMETER))
          // .map((mapper) -> new LogInfo(mapper[2], request.getContainer(), mapper[0], squareId))
          .collect(Collectors.toList());

    System.out.println(Arrays.deepToString(parsedLogs.get(0)));

    // logService.saveLog(parsedLogs);

  }


  @GET
  @Path("/list")
  public Response list() {
    return Response.ok().entity(logService.getLogs()).build();
  }

}
