package fr.umlv.square.model.response;

/**
 * Class use to represent a JSON response after an deploy post request
 */
public class DeployResponse extends AbstractResponse {
  public DeployResponse(int id, String appName, int port, int servicePort, String dockerInstance) {
    super(id, appName, port, servicePort, dockerInstance);
  }
}
