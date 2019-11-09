package fr.umlv.square.service;

public class LogInfo {
  private final String message;
  private final String containerId;
  private final String date;

  public LogInfo(String message, String containerId, String date) {
    this.message = message;
    this.containerId = containerId;
    this.date = date;
  }

  public String getContainerId() {
    return containerId;
  }

}
