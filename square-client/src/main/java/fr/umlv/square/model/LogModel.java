package fr.umlv.square.model;

public class LogModel {
  private final String message;
  private final String date;
  private final String level;

  public LogModel(String message, String date, String level) {
    this.message = message;
    this.date = date;
    this.level = level;
  }

  @Override
  public String toString() {
    return date + " ;; " + level + " ;; " + message;
  }
}
