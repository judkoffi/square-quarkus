package fr.umlv.square.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ClientLogRequest {
  @NotNull
  @NotBlank
  private String container;
  @NotNull
  @NotBlank
  private String message;

  public ClientLogRequest() {}

  public String getMessage() {
    return message;
  }

  public String getContainer() {
    return container;
  }

  public void setContainer(String container) {
    this.container = container;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "log request :" + message + " from: " + container;
  }
}
