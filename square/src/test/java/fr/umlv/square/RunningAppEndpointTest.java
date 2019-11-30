package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

@QuarkusTest
public class RunningAppEndpointTest {

  @NotNull
  private TypeRef<ArrayList<TestRunningInstance>> getDeployResponseTypeRef() {
    return new TypeRef<ArrayList<TestRunningInstance>>() {
      // Kept empty on purpose
    };
  }

  private static class TestDeployResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id")
    int id;
    @JsonProperty("port")
    int port;
    @JsonProperty("app")
    String appName;
    @JsonProperty("service-port")
    int servicePort;
    @JsonProperty("docker-instance")
    String dockerInstance;
  }

  private static class TestRunningInstance extends TestDeployResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("elapsed-time")
    String elapsedTime;

    @Override
    public String toString() {
      return "app: " + appName;
    }
  }

  @Test
  @Order(1)
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
  @Order(2)
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
  @Order(3)
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
  @Order(4)
  public void testDeployEndpoint() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"fruitapi:8080\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(201)
      .assertThat()
      .body(not("{}"));

    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"helloapp:8080\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(201)
      .assertThat()
      .body(not("{}"));
  }

  @Test
  @Order(5)
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
  @Order(6)
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
  @Order(7)
  public void testBadAPPDeployEndpoint() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"toto:8080\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(201)
      .assertThat()
      .body(is("{}"));
  }

  @Test
  @Order(8)
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
  @Order(9)
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
  @Order(10)
  public void testListEndpoint() throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .extract()
      .as(getDeployResponseTypeRef());

    System.out.println(result);

    var tmp = result.stream().filter((p) -> p.appName.equals("fruitapi")).findFirst().get();

    assertAll(() ->
    {
      assertEquals(2, result.size());
    }, () ->
    {
      assertEquals("fruitapi", tmp.appName);
      assertEquals(8080, tmp.port);
      assertEquals("fruitapi-" + tmp.id, tmp.dockerInstance);
    });
  }

  @Test
  @Order(11)
  public void testStopEndpointWithUnknowId() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"id\": 2}")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(422)
      .body(is("{\"message\":\"Unknow instance id\"}"));
  }

  @Test
  @Order(12)
  public void testStopEndpoint() {
    var result = given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \"helloapp:8080\"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(201)
      .assertThat()
      .extract()
      .as(TestDeployResponse.class);

    var tmp = given()
      .contentType(ContentType.JSON)
      .body("{\"id\": " + result.id + " }")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(201)
      .body(not("{}"))
      .extract()
      .as(TestRunningInstance.class);

    assertAll(() ->
    {
      assertEquals("helloapp", tmp.appName);
      assertEquals(8080, tmp.port);
      assertEquals("helloapp-" + tmp.id, tmp.dockerInstance);
      assertEquals(tmp.id, result.id);
    });
  }

  @AfterAll
  public static void cleanInstance() throws InterruptedException, IOException {
    var process = new ProcessBuilder("bash", "-c", "docker kill $(docker ps -aq)");
    process.start().waitFor();
  }
}
