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

  /**
   * Allow to get the whole application name : appname:port
   * @return : String contains the whole application name : appname:port
   */
  public String getApp() {
    return app;
  }

  /**
   * Allow to set the application name
   * @param app : String : the application name to set
   */
  public void setApp(String app) {  // because app is private
    this.app = app;
  }

  /**
   * Allow to get only the application name
   * @return : String which contains only the application name
   */
  public String getAppName() {
    return app.split(":")[0];
  }

  /**
   * Allow to get the port of the application
   * @return : int which is the port of the application
   */
  public int getPort() {
    return Integer.parseInt(app.split(":")[1]);
  }

  @Override
  public String toString() {
    return "deploy request :" + app;
  }
}
