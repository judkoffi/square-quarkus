package fr.umlv.square;

import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RunningAppEndpointTest {
	@Test
	public void testDeployEndpoint() {
		var expected = "{\"id\": \"1\", \"app\": \"totoapp\", \"port\": \"9000\", \"service-docker\":\"servicePort\", \"docker-instance\":\"docerInstance\"}";
		given().body("{\"app\": \"totoapp:9000\"}").when().post("/app/deploy").then().assertThat().body(equalTo(expected));

	}

	public void testEmptyPostBodyDeployEndpoint() {
		given().when().body("{}").post("/app/deploy").then().statusCode(200).body(is(""));
	}
}
