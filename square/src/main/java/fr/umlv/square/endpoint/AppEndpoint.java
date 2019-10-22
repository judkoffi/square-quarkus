package fr.umlv.square.endpoint;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import fr.umlv.square.model.request.RequestDeploy;

@Path("/app")
public class AppEndpoint {

	@POST
	@Path("/deploy")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deploy(RequestDeploy request) {
		
        /*Jsonb jsonb = JsonbBuilder.create();
        
        RequestDeploy p = jsonb.fromJson(request, RequestDeploy.class);
        
		System.out.println("request " + p);*/
		
		System.out.println(request);

		return Response.ok().entity("hello toto").build();
	}
}
