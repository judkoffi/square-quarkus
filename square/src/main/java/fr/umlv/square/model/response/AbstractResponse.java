package fr.umlv.square.model.response;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Class use to represent as an java object a super class of all Square response. It use to
 * factoring the common fields of all Square API response model
 */
abstract class AbstractResponse {
  private final int id;
  private final int port;
  private final String appName;
  private final int servicePort;
  private final String dockerInstance;

  public AbstractResponse(int id, String appName, int port, int servicePort,
      String dockerInstance) {
    this.id = Objects.requireNonNull(id);
    this.appName = Objects.requireNonNull(appName);
    this.port = Objects.requireNonNull(port);
    this.servicePort = Objects.requireNonNull(servicePort);
    this.dockerInstance = Objects.requireNonNull(dockerInstance);
  }

  /**
   * Method use to build a JSON string using all fields
   * 
   * @return @{String} which represent json content of an @{AbstractResponse}
   */
  String buildJson() {
    StringJoiner joiner = new StringJoiner(", ");
    joiner.add("\"id\": " + id);
    joiner.add("\"app\": \"" + appName + "\"");
    joiner.add("\"port\": " + port);
    joiner.add("\"service-docker\": " + servicePort);
    joiner.add("\"docker-instance\":\"" + dockerInstance + "\"");
    return joiner.toString();
  }

  public int getId() {
    return id;
  }

  public String getAppName() {
    return appName;
  }

  public String getDockerInstance() {
    return dockerInstance;
  }


  /**
   * Method use to finalise build of JSON string using all fields
   * 
   * @return @{String} which represent json string of an @{AbstractResponse}
   */

  public String toJson() {
    return "{" + buildJson() + "}";
  }
}
