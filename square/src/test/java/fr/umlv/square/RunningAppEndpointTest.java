package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import javax.json.bind.JsonbBuilder;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RunningAppEndpointTest {

	@Test
	public void testDeployEndpoint() {
		var expected = "{\"id\": \"1\", \"app\": \"totoapp\", \"port\": \"9000\", \"service-docker\":\"servicePort\", \"docker-instance\":\"docerInstance\"}";
		given().body("{\"app\": \"totoapp:9000\"}").when().post("/app/deploy").then().statusCode(200).assertThat()
				.body(is(expected));

	}

	@Test
	public void testEmptyPostBodyDeployEndpoint() {
		var jsonBuilder = JsonbBuilder.create();
		var body = jsonBuilder.toJson("{}");

		given().body(body).when().post("/app/deploy").then().statusCode(400).body(is("Invalid post body"));

	}
}
