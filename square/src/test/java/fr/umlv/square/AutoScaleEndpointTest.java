package fr.umlv.square;

public class AutoScaleEndpointTest {
	
	@Test
	public void testAutoScaleUpdateEndpoint() {
		var expected = "{\"todomvc:8082\": \"need to start 1 instance(s)\", \"demo:8083\": \"need to stop 2 instance(s)\"}";

		given().contentType(ContentType.JSON).body("{\"todomvc:8082\": 1, \"demo:8083\" : 2 }").when().post("/auto-scale/update").then()
				.statusCode(200).assertThat().body(is(expected));

	}
}
