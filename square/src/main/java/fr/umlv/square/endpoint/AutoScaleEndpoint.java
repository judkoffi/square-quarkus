package fr.umlv.square.endpoint;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Map.Entry;

import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auto-scale")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AutoScaleEndpoint {

	@POST
	@Path("/update")
	public Response update(Map<String, String> request) {
		var jsonBuilder = JsonbBuilder.create();
		var map = request.entrySet().stream().collect(toMap(Entry::getKey, Entry::getValue));
		return Response.ok().entity(jsonBuilder.toJson(map)).build();
	}

}
