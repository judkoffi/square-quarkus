package fr.umlv.square.endpoint;

import java.util.ArrayList;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.umlv.square.model.request.DeployRequest;
import fr.umlv.square.model.response.DeployResponse;

@Path("/app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppEndpoint {

	@POST
	@Path("/deploy")
	public Response deploy(DeployRequest request) {

		System.out.println(request);
		var servicePort = "servicePort";
		var docerInstance = "docerInstance";

		var response = new DeployResponse(new Random().nextInt(), request.getAppName(), request.getPort(), servicePort,
				docerInstance);

		return Response.ok().entity(response).build();
	}

	@GET
	@Path("/list")
	public Response list() {
		var servicePort = "servicePort";
		var docerInstance = "docerInstance";

		var list = new ArrayList<DeployResponse>();

		for (var i = 0; i < 5; i++) {
			list.add(new DeployResponse(new Random().nextInt(), "appName" + i, 8000 + i, servicePort + i,
					docerInstance + i));
		}

		return Response.ok().entity(list).build();
	}

}
