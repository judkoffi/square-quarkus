package fr.umlv.square.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class LogEntity extends PanacheEntity {

  @Column(name = "squareId")
  private int squareId;

  @Column(name = "message")
  private String message;

  @Column(name = "date")
  private String date;

  @Column(name = "level")
  private String level;

  public LogEntity() {}

  public LogEntity(int squareId, String date, String level, String message) {
    this.squareId = squareId;
    this.date = date;
    this.level = level;
    this.message = message;
  }

  public int getId() {
    return squareId;
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

  public void setId(int id) {
    this.squareId = id;
  }

}
