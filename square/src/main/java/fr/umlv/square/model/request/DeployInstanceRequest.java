package fr.umlv.square.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Model request use to map an deploy post request as a java object
 */
public class DeployInstanceRequest {
  @NotNull
  @NotBlank
  @Pattern(regexp = "[^\\:]+:[0-9]+") // app must match with regexp, ex: appname:port
  private String app;

  public DeployInstanceRequest() {}

  public String getApp() {
    return app;
  }

  public void setApp(String app) {  // because app is private
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
