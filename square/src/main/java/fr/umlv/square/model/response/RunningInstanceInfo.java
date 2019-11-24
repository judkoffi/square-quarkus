package fr.umlv.square.model.response;

import java.util.Objects;

/**
 * Class used to represent a JSON response an /app request
 */

public class RunningInstanceInfo extends AbstractResponse {
  private final String elapsedTime;

  public RunningInstanceInfo(int id, String appName, int port, int servicePort,
      String dockerInstance, String elapsedTime) {
    super(id, appName, port, servicePort, dockerInstance);
    this.elapsedTime = Objects.requireNonNull(elapsedTime);
  }

  /**
   * Used to build a JSON string using all fields
   */
  @Override
  String buildJson() {
    return super.buildJson() + ", \"elapsed-time\":\"" + elapsedTime + "\"";
  }

}
