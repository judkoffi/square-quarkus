package fr.umlv.square.endpoint;

import java.util.ArrayList;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.umlv.square.model.response.LogsTimeResponse;

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogsEndpoint {
	
	@GET
	@Path("/{time}")
	public Response list(@PathParam("time") String time) {
		String servicePort = "servicePort_";
		String dockerInstance = "docerInstance_";

		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < 3; i++) {
			list.add(new LogsTimeResponse(new Random().nextInt(), "appName_" + i, 8000 + i, servicePort + i,
					dockerInstance + i, "ceci est un message de log", time).toJson());
		}

		return Response.ok().entity(list.toString()).build();
	}

}
