package fr.umlv.square.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class SquareClient {
  private final String ENDPOINT = "/container-log/send-log";
  private final String squareUrl;
  private final HttpClient client;
  private final String dockerId;

  public SquareClient(ClientConfig clientConfig) {
    this.squareUrl = "http://" + clientConfig.squareHost + ":" + clientConfig.squarePort;
    this.client = HttpClient.newHttpClient();
    this.dockerId = clientConfig.dockerId;
  }


  public void sendLog(String message) {
    var uri = squareUrl + ENDPOINT;
    var body = "{\"container\":\"" + dockerId + "\", \"message\":\"" + message + "\"}";

    System.out.println(body);

    var request = HttpRequest
      .newBuilder()
      .uri(URI.create(uri))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    try {
      var response = client.send(request, BodyHandlers.ofString());
      System.out.println(response.statusCode());
      System.out.println(response.body());
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
  }
}

