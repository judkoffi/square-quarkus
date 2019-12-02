package fr.umlv.square;

import java.io.Serializable;
import java.util.ArrayList;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.restassured.common.mapper.TypeRef;

public class ModelClassesForTests {

  @NotNull
  static TypeRef<ArrayList<TestRunningInstance>> getDeployResponseTypeRef() {
    return new TypeRef<ArrayList<TestRunningInstance>>() {
      // Kept empty on purpose
    };
  }

  @NotNull
  static TypeRef<ArrayList<TestLogModel>> getLogsResponseTypeRef() {
    return new TypeRef<ArrayList<TestLogModel>>() {
      // Kept empty on purpose
    };
  }

  static class TestDeployResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id")
    int id;
    @JsonProperty("port")
    int port;
    @JsonProperty("app")
    String appName;
    @JsonProperty("service-port")
    int servicePort;
    @JsonProperty("docker-instance")
    String dockerInstance;
  }

  static class TestRunningInstance extends TestDeployResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("elapsed-time")
    String elapsedTime;

    @Override
    public String toString() {
      return "app: " + appName;
    }
  }

  static class TestLogModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("id")
    int id;
    @JsonProperty("port")
    int port;
    @JsonProperty("service-port")
    int servicePort;
    @JsonProperty("docker-instance")
    String dockerInstance;
    @JsonProperty("app")
    String app;
    @JsonProperty("appName")
    String appName;
    @JsonProperty("message")
    String message;
    @JsonProperty("timestamp")
    String timestamp;
  }

}
