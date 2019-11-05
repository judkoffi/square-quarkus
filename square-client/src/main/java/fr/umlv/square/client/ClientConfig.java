package fr.umlv.square.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

class ClientConfig {
  final String squareHost;
  final int squarePort;

  private ClientConfig(String host, String port) {
    this.squareHost = Objects.requireNonNull(host);
    this.squarePort = Integer.parseInt(Objects.requireNonNull(port));
  }

  public static ClientConfig fromFile(String path) throws IOException {
    var input = new FileInputStream(path);
    var properties = new Properties();
    properties.load(input);
    var host = properties.getProperty("square.host");
    var port = properties.getProperty("square.port");
    return new ClientConfig(host, port);
  }

  public static ClientConfig fromEnv() {
    var envVariable = new ProcessBuilder().environment();
    var host = envVariable.get("SQUARE_HOST");
    var port = envVariable.get("SQUARE_PORT");
    return new ClientConfig(host, port);
  }


  @Override
  public String toString() {
    return "HOST=" + squareHost + "; PORT=" + squarePort;
  }
}
