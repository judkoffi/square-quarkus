package fr.umlv.square.model.response;

import java.util.Objects;

public class RunningInstanceInfo extends AbstractResponse {

  private final String elapsedTime;

  public RunningInstanceInfo(int id, String appName, int port, String servicePort,
      String dockerInstance, String elapsedTime) {
    super(id, appName, port, servicePort, dockerInstance);
    this.elapsedTime = Objects.requireNonNull(elapsedTime);
  }

  @Override
  String buildJson() {
    return super.buildJson() + ", \"elapsed-time\":\"" + elapsedTime + "\"";
  }

}
