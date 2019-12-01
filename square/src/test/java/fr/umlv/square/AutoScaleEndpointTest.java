package fr.umlv.square;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class AutoScaleEndpointTest {

  @BeforeEach
  public void clearInstance() throws InterruptedException, IOException {
    var process = new ProcessBuilder("bash", "-c", "docker kill $(docker ps -aq)");
    process.start().waitFor();
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

  @Order(3)
  public void clear() throws InterruptedException, IOException {
    clearInstance();
  }


}
