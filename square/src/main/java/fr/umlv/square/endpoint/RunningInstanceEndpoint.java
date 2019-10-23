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

import fr.umlv.square.model.request.DeployInstanceRequest;
import fr.umlv.square.model.request.StopInstanceRequest;
import fr.umlv.square.model.response.DeployResponse;
import fr.umlv.square.model.response.RunningInstanceInfo;

@Path("/app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RunningInstanceEndpoint {

	@POST
	@Path("/deploy")
	public Response deploy(DeployInstanceRequest request) {

		System.out.println(request);
		String servicePort = "servicePort";
		String dockerInstance = "docerInstance";

		DeployResponse response = new DeployResponse(new Random().nextInt(), request.getAppName(), request.getPort(), servicePort,
				dockerInstance);

		return Response.ok().entity(response.toJson()).build();
	}

	@GET
	@Path("/list")
	public Response list() {
		String servicePort = "servicePort_";
		String dockerInstance = "docerInstance_";

		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < 5; i++) {
			list.add(new RunningInstanceInfo(new Random().nextInt(), "appName_" + i, 8000 + i, servicePort + i,
					dockerInstance + i, "1m50").toJson());
		}

		return Response.ok().entity(list.toString()).build();
	}

	@POST
	@Path("/stop")
	public Response stop(StopInstanceRequest request) {
		String servicePort = "servicePort_";
		String dockerInstance = "docerInstance_";

		RunningInstanceInfo result = new RunningInstanceInfo(request.getId(), "appName_", 8000, servicePort, dockerInstance, "1m50");

		return Response.ok().entity(result.toJson()).build();
	}

}
