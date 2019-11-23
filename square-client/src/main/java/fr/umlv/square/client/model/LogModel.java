package fr.umlv.square.client.model;

/**
 * This class represent model which describe an log object to send to Square API
 */
public class LogModel {
  private final String message;
  private final String date;
  private final String level;

  public LogModel(String message, String date, String level) {
    this.message = message;
    this.date = date;
    this.level = level;
  }

  /**
   * Get the message associated with the log
   * @return message contained in the log
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "{" + "\"message\":\"" + message + "\", \"date\":\"" + date + "\", \"level\": \"" + level
        + "\" }";
  }
}
