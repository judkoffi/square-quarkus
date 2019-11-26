package fr.umlv.square.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ClientStatusRequest {
  @NotNull
  @NotBlank
  private String dockerInstance;
  @NotNull
  @NotBlank
  private boolean status;

  public ClientStatusRequest() {}

  public String getDockerInstance() {
    return dockerInstance;
  }

  public boolean getStatus() {
    return status;
  }

  public void setDockerInstance(String dockerInstance) {
    this.dockerInstance = dockerInstance;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

}
