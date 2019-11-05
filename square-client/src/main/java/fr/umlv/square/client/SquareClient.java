package fr.umlv.square.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.StringJoiner;

public class SquareClient {

  private final String squarUrl;

  public SquareClient(ClientConfig clientConfig) {
    this.squarUrl = "http://" + clientConfig.squareHost + ":" + clientConfig.squarePort;
  }

  public void sendLog() throws IOException, InterruptedException {
    var uri = squarUrl + "/kawai/send-log";
    while (true) {
      new Thread(() ->
      {
        var i = 0;
        var map = new HashMap<String, String>();
        var body = "COUCOU " + i;
        map.put("message", body);
        var client = HttpClient.newHttpClient();

        var builder = new StringJoiner(",", "{", "}");

        map
          .entrySet()
          .forEach((elt) -> builder.add("\"" + elt.getKey() + "\":\"" + elt.getValue() + "\""));

        System.out.println(builder.toString());
        var request = HttpRequest
          .newBuilder()
          .uri(URI.create(uri))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(builder.toString()))
          .build();
        i++;

        try {
          var response = client.send(request, BodyHandlers.ofString());
          System.out.println(response.statusCode());
          System.out.println(response.body());
        } catch (IOException | InterruptedException e1) {
          throw new AssertionError();
        }
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new AssertionError();

        }
      }).start();
    }
  }
}
