package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import fr.umlv.square.model.response.RunningInstanceInfo;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class RunningAppEndpointTest {

  @Test
  public void testDeployEndpoint() {
    var expected =
        "{\"id\": 1, \"app\": \"totoapp\", \"port\": 5000, \"service-docker\": 10000, \"docker-instance\":\"totoapp_5000\"}";

    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"totoapp:5000\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is(expected));

  }

  @Test
  public void testEmptyPostBodyDeployEndpoint() {
    given()
      .contentType(ContentType.JSON)
      .body("{}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));

  }

  @Test
  public void testBadRequestDeployEndpoint() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"totoapp5000\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));

  }

  @Test
  public void testBadRequestDeployEndpoint2() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"totoappaasa\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));
  }

  @Test
  public void testBadRequestDeployEndpoint3() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"qcscs\": \"totoappaasa\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));
  }

  @Test
  public void testBadRequestDeployEndpoint4() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"dadad\": \"totoappaasa:4000\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));
  }


  @Test
  public void testAppStopEndpoint() {
    var expected =
        "{\"id\": 2, \"app\": \"appName_\", \"port\": 8000, \"service-docker\": 8000, \"docker-instance\":\"appName_\", \"elapsed-time\":\"1m50\"}";
    given()
      .contentType(ContentType.JSON)
      .body("{\"id\": \"2\"}")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(200)
      .body(is(expected));
  }

  @Test
  public void testBadRequestStop() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"dadad\": \"totoappaasa\"}")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));
  }

  @Test
  public void testBadRequestStop2() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"isqx\": 22}")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));
  }


  @Test
  public void testListEndpoit() {
    var instance1 = new RunningInstanceInfo(1, "sortapp", 8080, 9000, "sortapp-1", "4m50s");
    var instance2 = new RunningInstanceInfo(15, "hellapi", 8080, 1000, "hellapi-15", "8m50s");
    var instance3 = new RunningInstanceInfo(238, "yep", 8080, 5010, "yep-238", "17m50s");
    var list = List.of(instance1, instance2, instance3);
    var result = list.stream().map((mapper) -> mapper.toJson()).collect(Collectors.toList());

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .body(is(result.toString()));
  }

}
