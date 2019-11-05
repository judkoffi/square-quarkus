package fr.umlv.square.endpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import fr.umlv.square.model.request.ClientLogRequest;

@Path("/container-log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiverEndpoint {

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
    var processBuilder = new ProcessBuilder();
    processBuilder.directory(new File("../../logs/"));

    System.out.println("request " + request.toString());


    var outputStream =
        processBuilder.command("bash", "-c", request.getMessage()).start().getInputStream();

    var result = getOutput(outputStream);
    return result;
  }

}
