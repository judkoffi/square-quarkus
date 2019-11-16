package fr.umlv.square.model.request;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

  public List<ReceivedLogModel> getLogs() {
    return logs;
  }

  public String getDockerInstance() {
    return dockerInstance;
  }

  public void setDockerInstance(String dockerInstance) {
    this.dockerInstance = dockerInstance;
  }

  public void setLogs(List<ReceivedLogModel> logs) {
    this.logs = logs;
  }

  @Override
  public String toString() {
    return "log request :" + logs + " from: " + dockerInstance;
  }
}
