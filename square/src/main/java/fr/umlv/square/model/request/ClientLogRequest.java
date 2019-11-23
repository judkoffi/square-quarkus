package fr.umlv.square.model.request;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import fr.umlv.square.model.service.ReceivedLogModel;

/**
 * Class which represent an log request send by square-client from each docker instance
 */

public class ClientLogRequest {
  @NotNull
  @NotBlank
  private String dockerInstance;
  @NotNull
  @NotBlank
  private List<ReceivedLogModel> logs;

  public ClientLogRequest() {}

  /**
   * Allow to get all the log of a docker instance
   * @return the List of ReceivedLogModel
   */
  public List<ReceivedLogModel> getLogs() {
    return logs;
  }

  /**
   * Allow to get the name of the docker instance
   * @return a String which is the name of the docker instance
   */
  public String getDockerInstance() {
    return dockerInstance;
  }

  /**
   * Allow to define the name of the docker instance
   * @param dockerInstance : String : the name of the docker instance
   */
  public void setDockerInstance(String dockerInstance) {
    this.dockerInstance = dockerInstance;
  }

  /**
   * ALlow to set a List of ReceivedLogModel of a docker instance
   * @param logs : List<ReceivedLogModel : the list of ReceivedLogModel of a docker instance
   */
  public void setLogs(List<ReceivedLogModel> logs) {
    this.logs = logs;
  }

  @Override
  public String toString() {
    return "log request :" + logs + " from: " + dockerInstance;
  }
}
