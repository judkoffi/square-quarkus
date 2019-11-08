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
  private final Object lock = new Object();

  public SquareClient(ClientConfig clientConfig) {
    this.squareUrl = "http://" + clientConfig.squareHost + ":" + clientConfig.squarePort;
    this.client = HttpClient.newHttpClient();
    this.dockerId = clientConfig.dockerId;
  }

  private String buildJson(String message, LogType type) {
    synchronized (lock) {
      return "{\"container\":\"" + dockerId + "\", \"message\":\"" + message + "\", \"logtype\":\""
          + type + "\"}";
    }
  }

  public void sendLog(String message, LogType type) {
    synchronized (lock) {
      var uri = squareUrl + ENDPOINT;
      var body = buildJson(message, type);
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
}

