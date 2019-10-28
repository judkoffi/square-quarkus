package fr.umlv.square.model.response;

public class DeployResponse extends AbstractResponse {

  public DeployResponse(int id, String appName, int port, int servicePort,
      String dockerInstance) {
    super(id, appName, port, servicePort, dockerInstance);
  }
}
