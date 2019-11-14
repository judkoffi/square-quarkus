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
  private String container;
  @NotNull
  @NotBlank
  private List<ReceivedLogModel> logs;

  public ClientLogRequest() {}

  public List<ReceivedLogModel> getLogs() {
    return logs;
  }

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  public void setLogs(List<ReceivedLogModel> logs) {
    this.logs = logs;
  }

  @Override
  public String toString() {
    return "log request :" + logs + " from: " + container;
  }
}
