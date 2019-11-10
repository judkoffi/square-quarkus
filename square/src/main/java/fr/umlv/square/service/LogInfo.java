package fr.umlv.square.service;

public class LogInfo {
  private final String message;
  private final String containerId;
  private final int squareId;
  private final String date;

  public LogInfo(String message, String containerId, String date, int squareId) {
    this.message = message;
    this.containerId = containerId;
    this.date = date;
    this.squareId = squareId;
  }

  public String getContainerId() {
    return containerId;
  }

  public int getSquareId() {
    return squareId;
  }

  public String getDate() {
    return date;
  }

  public String getMessage() {
    return message;
  }
}
