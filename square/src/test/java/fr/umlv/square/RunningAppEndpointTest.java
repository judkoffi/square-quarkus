package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class RunningAppEndpointTest {

	@Test
	public void testDeployEndpoint() {
		var expected = "{\"id\": 1, \"app\": \"totoapp\", \"port\": 5000, \"service-docker\": 10000, \"docker-instance\":\"totoapp-12\"}";

		given().contentType(ContentType.JSON).body("{\"app\": \"totoapp:5000\"}").when().post("/app/deploy").then()
				.statusCode(200).assertThat().body(is(expected));

	}

	@Test
	public void testEmptyPostBodyDeployEndpoint() {
		given().contentType(ContentType.JSON).body("{}").when().post("/app/deploy").then().statusCode(400)
				.body(is("Invalid post body"));

	}

	@Test
	public void testBadRequestDeployEndpoint() {
		given().contentType(ContentType.JSON).body("{\"app\": \"totoapp5000\"}").when().post("/app/deploy").then()
				.statusCode(400).body(is("Invalid post body"));

	}

	@Test
	public void testBadRequestDeployEndpoint2() {
		given().contentType(ContentType.JSON).body("{\"app\": \"totoappaasa\"}").when().post("/app/deploy").then()
				.statusCode(400).body(is("Invalid post body"));
	}

	@Test
	public void testBadRequestDeployEndpoint3() {
		given().contentType(ContentType.JSON).body("{\"qcscs\": \"totoappaasa\"}").when().post("/app/deploy").then()
				.statusCode(400).body(is("Invalid post body"));
	}

	@Test
	public void testBadRequestDeployEndpoint4() {
		given().contentType(ContentType.JSON).body("{\"dadad\": \"totoappaasa:4000\"}").when().post("/app/deploy")
				.then().statusCode(400).body(is("Invalid post body"));
	}
}
