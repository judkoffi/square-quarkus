package fr.umlv.square.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.hibernate.annotations.Type;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class LogEntity extends PanacheEntity {
  @Column(name = "squareId")
  private int squareId;

  @Column(name = "message")
  @Type(type = "text")
  private String message;

  @Column(name = "date")
  private String date;

  @Column(name = "level")
  private String level;

  @Column(name = "dockerinstance")
  private String dockerInstance;

  @Column(name = "appname")
  private String appName;

  public LogEntity() {}

  public LogEntity(int squareId, String date, String level, String message, String dockerInstance,
      String appName) {
    this.squareId = squareId;
    this.date = date;
    this.level = level;
    this.message = message;
    this.dockerInstance = dockerInstance;
    this.appName = appName;
  }

  public int getSquareId() {
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

  public String getAppName() {
    return appName;
  }

  public String getDockerInstance() {
    return dockerInstance;
  }

}
