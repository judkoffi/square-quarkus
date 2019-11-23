package fr.umlv.square.model.service;

/**
 * Model class to map LogModel object send by square client app
 */

public class ReceivedLogModel {
  private String message;
  private String date;
  private String level;

  public ReceivedLogModel() {}

  public ReceivedLogModel(String message, String date, String level) {
    this.message = message;
    this.date = date;
    this.level = level;
  }

  public String getMessage() {
    return message;
  }

  public String getDate() {
    return date;
  }

  public String getLevel() {
    return level;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  @Override
  public String toString() {
    return "{" + "\"message\":\"" + message + "\", \"date\":\"" + date + "\", \"level\": \"" + level
        + "\" }";
  }
}
