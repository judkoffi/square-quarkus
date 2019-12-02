package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogEndpointTest {

  @ConfigProperty(name = "quarkus.http.host")
  String host;

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
  public void testGoodRequestTimelogsWithoutDeployApp() {
    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/logs/0")
      .then()
      .statusCode(200)
      .assertThat()
      .body(is("[]"));
  }

  @Test
  @Order(3)
  public void testBagRequestTimelogs() {
    given().contentType(ContentType.JSON).when().get("/logs/a").then().statusCode(400).assertThat();
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
  @Order(4)
  public void testGoodRequestTimelogs() throws InterruptedException {
    TimeUnit.SECONDS.sleep(20); // to be sure that logs are stored in DB
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/logs/10")
      .then()
      .statusCode(200)
      .assertThat()
      .extract()
      .as(ModelClassesForTests.getLogsResponseTypeRef());

    assertTrue(result.size() >= 3);
  }

  @Test
  @Order(5)
  public void testGoodRequestTimelogsWIthAppNameFilter() {
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/logs/10/helloapp:8080")
      .then()
      .statusCode(200)
      .assertThat()
      .extract()
      .as(ModelClassesForTests.getLogsResponseTypeRef());

    assertTrue(result.size() >= 3);
  }


  @Test
  @Order(6)
  public void testGoodRequestTimelogs2() throws InterruptedException, IOException {
    var list = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/app/list")
      .then()
      .statusCode(200)
      .extract()
      .as(ModelClassesForTests.getDeployResponseTypeRef());

    var tmp = list.stream().filter((p) -> p.appName.equals("helloapp")).findFirst().get();

    var uri = "http://" + host + ":" + tmp.servicePort + "/api/hello";
    var request = HttpRequest
      .newBuilder()
      .uri(URI.create(uri))
      .header("Content-Type", "application/json")
      .build();
    var client = HttpClient.newHttpClient();
    client.send(request, BodyHandlers.ofString());

    TimeUnit.SECONDS.sleep(30); // to be sure that logs are stored in DB
    var result = given()
      .contentType(ContentType.JSON)
      .when()
      .get("/logs/10")
      .then()
      .statusCode(200)
      .assertThat()
      .extract()
      .as(ModelClassesForTests.getLogsResponseTypeRef());

    assertTrue(result.size() >= 7);
  }

}
