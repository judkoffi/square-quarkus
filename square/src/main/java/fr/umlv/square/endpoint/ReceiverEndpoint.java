package fr.umlv.square.endpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import fr.umlv.square.model.request.ClientLogRequest;
import fr.umlv.square.service.LogInfo;
import fr.umlv.square.service.LogService;

@Path("/container-log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiverEndpoint {

  @Inject
  LogService logService;

  private String getOutput(InputStream outpuStream) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(outpuStream));
    var builder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
      builder.append(System.getProperty("line.separator"));
    }
    return builder.toString();
  }

  @POST
  @Path("/send-log/")
  public String kawaii(ClientLogRequest request) throws IOException {

    logService.a();

    var processBuilder = new ProcessBuilder();
    processBuilder.directory(new File("../../logs/"));

    System.out.println("request " + request);

    var logs = request.getMessage();

    List<LogInfo> list = logs
      .equals("[]") ? List.of()
          : Arrays.stream(logs.split(",")).map((mapper) -> new LogInfo(mapper, request.getContainer(), "-1")).collect(Collectors.toList());

    list.forEach((log) -> logService.saveLog(log));

    var outputStream = processBuilder
      .command("bash", "-c", "echo " + request.getMessage() + ">> log1.log")
      .start()
      .getInputStream();

    var result = getOutput(outputStream);
    return result;
  }

}
