package fr.umlv.square.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class DeployInstanceRequest {

  @NotBlank
  @Pattern(regexp = "[^\\:]+:[0-9]+")
  private String app;

  public DeployInstanceRequest() {}

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public String getAppName() {
    return app.split(":")[0];
  }

  public int getPort() {
    return Integer.parseInt(app.split(":")[1]);
  }

  @Override
  public String toString() {
    return "deploy request :" + app;
  }
}
