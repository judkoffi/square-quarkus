package fr.umlv.square.endpoint;

import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.umlv.square.model.request.RequestDeploy;
import fr.umlv.square.model.response.ResponseDeploy;

@Path("/app")
public class AppEndpoint {

	@POST
	@Path("/deploy")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deploy(RequestDeploy request) {
		ResponseDeploy response = new ResponseDeploy();
		response.setId(new Random().nextInt());
		response.setApp(request.getAppName());
		return Response.ok().entity(response).build();
	}
}
