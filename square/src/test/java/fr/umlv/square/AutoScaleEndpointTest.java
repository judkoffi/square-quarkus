package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutoScaleEndpointTest {

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
  @Order(0)
  public void clear() {
    clean();
  }

  @Test
  @Order(1)
  public void noactivatedscaling() {
    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/auto-scale/status")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is("{}"));
  }

  @Test
  @Order(2)
  public void updatetest() throws InterruptedException {
    var body = "  {\"helloapp:8080\": 2,\"fruitapi:8080\": 1}";
    var expected =
        "{\"helloapp:8080\":\"need to start 2 instance(s)\",\"fruitapi:8080\":\"need to start 1 instance(s)\"}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/auto-scale/update")
      .then()
      .statusCode(201)
      .assertThat()
      .body(is(expected));
  }

  @Test
  @Order(3)
  public void updatetest2() throws InterruptedException {
    var expected =
        "{\"helloapp:8080\":\"need to start 2 instance(s)\",\"fruitapi:8080\":\"need to start 1 instance(s)\"}";

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/auto-scale/status")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is(expected));
  }


  @Test
  @Order(4)
  public void checkAutoScaleIsApply() throws InterruptedException {
    TimeUnit.SECONDS.sleep(30); // sleep 30 to be assure that scale service do the job
    var expected = "{\"helloapp:8080\":\"need nothing\",\"fruitapi:8080\":\"need nothing\"}";
    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/auto-scale/status")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is(expected));
  }

  @Test
  @Order(5)
  public void checkRunningAppAfterAutoScale() throws InterruptedException {
    listWrapper();
  }

  @Test
  @Order(6)
  public void testStopAutoScaleupdate2() {
    var body = "  {\"helloapp:8080\": 0,\"fruitapi:8080\": 0}";
    var expected =
        "{\"helloapp:8080\":\"need to stop 2 instance(s)\",\"fruitapi:8080\":\"need to stop 1 instance(s)\"}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/auto-scale/update")
      .then()
      .statusCode(201)
      .assertThat()
      .body(is(expected));
  }

  @Test
  @Order(7)
  public void getRemoveScaling() throws InterruptedException {
    TimeUnit.SECONDS.sleep(30); // sleep 30 to be assure that scale service do the job
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .extract()
      .as(ModelClassesForTests.getDeployResponseTypeRef());

    assertEquals(0, result.size());
  }

  @Test
  @Order(8)
  public void testStopAutoScale() {
    var expected = "{\"helloapp:8080\":0,\"fruitapi:8080\":0}";
    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/auto-scale/stop")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is(expected));
  }

  @Test
  @Order(9)
  public void checkAutoScaleStatus() throws InterruptedException, IOException {
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

    var expected = "{\"helloapp:8080\":\"need nothing\",\"fruitapi:8080\":\"need nothing\"}";
    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/auto-scale/status")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is(expected));
  }


  @Test
  @Order(10)
  public void checkAutoScalerReUp() throws InterruptedException, IOException {
    var body = "{\"helloapp:8080\": 2,\"fruitapi:8080\": 1}";
    var expected =
        "{\"helloapp:8080\":\"need to start 1 instance(s)\",\"fruitapi:8080\":\"need nothing\"}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/auto-scale/update")
      .then()
      .statusCode(201)
      .assertThat()
      .body(is(expected));
  }

  @Test
  @Order(11)
  public void checkAutoScalerReUpList() throws InterruptedException, IOException {
    listWrapper();
  }

  public void listWrapper() throws InterruptedException {
    TimeUnit.SECONDS.sleep(30); // sleep 30 to be assure that scale service do the job
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .extract()
      .as(ModelClassesForTests.getDeployResponseTypeRef());

    var tmp = result.stream().filter((p) -> p.appName.equals("fruitapi")).findFirst().get();
    var helloApps =
        result.stream().filter((p) -> p.appName.equals("helloapp")).collect(Collectors.toList());

    assertAll(() ->
    {
      assertEquals(3, result.size());
    }, () ->
    {
      assertEquals("fruitapi", tmp.appName);
      assertEquals(8080, tmp.port);
      assertEquals("fruitapi-" + tmp.id, tmp.dockerInstance);
    }, () ->
    {
      var a = helloApps.get(0);
      var b = helloApps.get(1);
      assertEquals(2, helloApps.size());
      assertEquals("helloapp", a.appName);
      assertEquals("helloapp", b.appName);
      assertEquals(8080, a.port);
      assertEquals(8080, b.port);
      assertEquals("helloapp-" + a.id, a.dockerInstance);
      assertEquals(8080, tmp.port);
      assertEquals("helloapp-" + b.id, b.dockerInstance);
    });
  }

  @Test
  @Order(12)
  public void autoScaleBadRequest() {
    var body = "  {\"helloapp8080\": 2,\"fruitapi:8080\": 1}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/auto-scale/update")
      .then()
      .statusCode(400)
      .assertThat();
  }

  @Test
  @Order(13)
  public void autoScaleBadRequest2() {
    var body = "  {\"helloapp:aa&&\": 2,\"fruitapi:8080\": 1}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/auto-scale/update")
      .then()
      .statusCode(400)
      .assertThat();
  }

  @Test
  @Order(14)
  public void autoScaleBadRequest3() {
    var body = "  {\"helloapp:aa&\": 2\"fruitapi:8080\": 1}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/auto-scale/update")
      .then()
      .statusCode(400)
      .assertThat();
  }
}
