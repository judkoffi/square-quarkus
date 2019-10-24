package fr.umlv.square.endpoint;

import javax.json.bind.annotation.JsonbProperty;
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

	private class Entry {
		@JsonbProperty
		private String key;
		@JsonbProperty()
		private int value;
		
		public Entry() {
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public Entry(String key, int value) {
			super();
			this.key = key;
			this.value = value;
		}

	

	}

	@POST
	@Path("/update")
	public Response update(Entry request) {

		System.out.println("request " + request);

		/*
		 * Jsonb jsonb = JsonbBuilder.create(); String result = jsonb.from
		 * System.out.println(result);
		 */

		return Response.ok().entity("").build();
	}

}
