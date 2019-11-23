package fr.umlv.square.model.response;

import java.util.Objects;

/**
 * Class used to represent a JSON response after a log request
 */

public class LogTimeResponse extends AbstractResponse {
  private final String message;
  private final String timestamp;

  public LogTimeResponse(int id, String appName, int port, int servicePort, String dockerInstance,
      String message, String timestamp) {
    super(id, appName, port, servicePort, dockerInstance);

    this.message = Objects.requireNonNull(message);
    this.timestamp = Objects.requireNonNull(timestamp);
  }

  public String getTimestamp() {
    return timestamp;
  }

  @Override
  /**
   * build a JSON string using all fields
   */
  String buildJson() {
    return super.buildJson() + ", \"message\":\"" + message + "\"" + ", \"timestamp\":\""
        + timestamp + "\"";
  }
}
