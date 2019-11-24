package fr.umlv.square.model.response;


import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Class used to represent a JSON response after a log request
 */

public class LogTimeResponse extends AbstractResponse {
  private final String message;
  private final String timestamp;

  public LogTimeResponse(int id, String appName, int port, int servicePort, String dockerInstance,
      String message, String timestamp) {
    super(id, appName, port, servicePort, dockerInstance);

    this.message = Objects.requireNonNull(message);
    this.timestamp = Objects.requireNonNull(timestamp);
  }

  public String getTimestamp() {
    return timestamp;
  }
  
  // Return a string date with the desired format
  private String formatTimestamp(String timestamp) {
    Date date = new Date(Timestamp.valueOf(timestamp).getTime());
    SimpleDateFormat sdf;
    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    sdf.setTimeZone(TimeZone.getTimeZone("CET"));
    return sdf.format(date);
  }

  @Override
  /**
   * build a JSON string using all fields
   */
  String buildJson() {
    return super.buildJson() + ", \"message\":\"" + message + "\"" + ", \"timestamp\":\""
        + formatTimestamp(timestamp) + "\"";
  }
}
