package fr.umlv.square.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class SquareClient {
  private final String squareHost = "http://192.168.43.184";
  private final int squarePort = 5050;

  public SquareClient() {

  }

  public void sendLog() throws IOException, InterruptedException {
    var uri = URI.create(squareHost + ":" + squarePort + "/kawai/send-log");

    var body = "{\"HoneyBee\": \"COUCOU\"}";

    var client = HttpClient.newHttpClient();
    var request = HttpRequest
      .newBuilder()
      .uri(uri)
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    var response = client.send(request, BodyHandlers.ofString());
    System.out.println(response.statusCode());
    System.out.println(response.body());
  }

}
