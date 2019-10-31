package fr.umlv.square;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class AutoScaleEndpointTest {
	
	@Test
	public void testAutoScaleUpdateEndpoint() {
		var expected = "{\"demo:8083\":\"need to start 1 instance(s)\",\"todomvc:8082\":\"need to start 2 instance(s)\"}";

		given().contentType(ContentType.JSON).body("{\"todomvc:8082\": 2, \"demo:8083\" : 1 }")
		.when().post("/auto-scale/update").then()
				.statusCode(200).assertThat().body(is(expected));

	}
}
