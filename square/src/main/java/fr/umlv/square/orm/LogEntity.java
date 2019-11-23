package fr.umlv.square.orm;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * This class represent a model of logs tuples stored in database table LogTable
 */

@Entity
@Table(name = "LogTable")   
public class LogEntity extends PanacheEntity {

  @Column(name = "squareId")
  private int squareId;

  @Column(name = "message")
  @Type(type = "text")
  private String message;

  @Column(name = "date")
  private Timestamp date;

  @Column(name = "level")
  private String level;

  @Column(name = "dockerinstance")
  private String dockerInstance;

  @Column(name = "appname")
  private String appName;

  @Column(name = "port")
  private int port;

  @Column(name = "servicePort")
  private int servicePort;

  public LogEntity() {}

  public LogEntity(int squareId, Timestamp date, String level, String message,
      String dockerInstance, String appName, int port, int servicePort) {
    this.squareId = squareId;
    this.date = date;
    this.level = level;
    this.message = message;
    this.dockerInstance = dockerInstance;
    this.appName = appName;
    this.port = port;
    this.servicePort = servicePort;
  }

  public int getSquareId() {
    return squareId;
  }

  public String getMessage() {
    return message;
  }

  public Timestamp getDate() {
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

  public int getPort() {
    return port;
  }

  public int getServicePort() {
    return servicePort;
  }

  @Override
  public String toString() {
    return "LogEntity [squareId=" + squareId + ", message=" + message + ", date=" + date
        + ", level=" + level + ", dockerInstance=" + dockerInstance + ", appName=" + appName
        + ", port=" + port + ", servicePort=" + servicePort + "]";
  }

}
