package fr.umlv.square.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import fr.umlv.square.model.LogModel;

/**
 * Class use to store information a Square API like host, port and endpoint to send logs
 */
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

  /**
   * 
   * @param logsModels: list of logs to to send
   * @return {@String} with represent json body to to send to Square API
   */
  private String buildJson(List<LogModel> logsModels) {
    synchronized (lock) {
      return "{\"container\":\"" + dockerId + "\", \"logs\":" + logsModels + "}";
    }
  }

  /**
   * Method use to send a log message using @{HttpClient} for request
   * 
   * @param: logsModels: a @{LogModel} which represent list log to be use for post request
   * @return: void
   */
  public void sendInfoLog(List<LogModel> logsModels) {
    synchronized (lock) {
      var uri = squareUrl + ENDPOINT;
      var body = buildJson(logsModels);
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

