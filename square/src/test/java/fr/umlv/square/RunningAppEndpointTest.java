package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RunningAppEndpointTest {

  public static void clean() {
    var list = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .extract()
      .as(ModelClassesForTests.getDeployResponseTypeRef());
    list.forEach(action -> stopInstance(action.id));
  }

  private static void stopInstance(int id) {
    given()
      .contentType(ContentType.JSON)
      .body("{\"id\": " + id + " }")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(201);
  }

  @Test
  @Order(1)
  public void clear() {
    clean();
  }

  @Test
  @Order(2)
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
  @Order(3)
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
  @Order(4)
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
  @Order(5)
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
  @Order(6)
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
  @Order(7)
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
  @Order(8)
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
  @Order(9)
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
  @Order(10)
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
  @Order(11)
  public void testListEndpoint() throws InterruptedException {
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .extract()
      .as(ModelClassesForTests.getDeployResponseTypeRef());

    var tmp = result.stream().filter((p) -> p.appName.equals("fruitapi")).findFirst().get();
    var tmp2 = result.stream().filter((p) -> p.appName.equals("helloapp")).findFirst().get();

    assertAll(() ->
    {
      assertEquals(2, result.size());
    }, () ->
    {
      assertEquals("fruitapi", tmp.appName);
      assertEquals(8080, tmp.port);
      assertEquals("fruitapi-" + tmp.id, tmp.dockerInstance);
    }, () ->
    {
      assertEquals("helloapp", tmp2.appName);
      assertEquals(8080, tmp2.port);
      assertEquals("helloapp-" + tmp2.id, tmp2.dockerInstance);
    });
  }

  @Test
  @Order(12)
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
  @Order(13)
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
      .as(ModelClassesForTests.TestDeployResponse.class);

    var tmp = given()
      .contentType(ContentType.JSON)
      .body("{\"id\": " + result.id + " }")
      .when()
      .post("/app/stop")
      .then()
      .statusCode(201)
      .body(not("{}"))
      .extract()
      .as(ModelClassesForTests.TestRunningInstance.class);

    assertAll(() ->
    {
      assertEquals("helloapp", tmp.appName);
      assertEquals(8080, tmp.port);
      assertEquals("helloapp-" + tmp.id, tmp.dockerInstance);
      assertEquals(tmp.id, result.id);
    });
  }

  @Test
  @Order(14)
  public void testBadRequestDeployEndpoint5() {
    given()
      .contentType(ContentType.JSON)
      .body("{\"app\": \" \"}")
      .when()
      .post("/app/deploy")
      .then()
      .statusCode(400)
      .body(is("Invalid post body"));
  }
}
