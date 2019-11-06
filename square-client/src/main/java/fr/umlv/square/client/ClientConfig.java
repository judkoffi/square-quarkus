package fr.umlv.square.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Properties;

class ClientConfig {
  final String squareHost;
  final int squarePort;
  final String dockerId;

  private ClientConfig(String host, String port, String dockerId) {
    this.squareHost = Objects.requireNonNull(host);
    this.squarePort = Integer.parseInt(Objects.requireNonNull(port));
    this.dockerId = Objects.requireNonNull(dockerId);
  }

  private static String getOutputOfCommand(InputStream outputStream) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(outputStream));
    var builder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
      builder.append(System.getProperty("line.separator"));
    }
    return builder.toString();
  }

  public static ClientConfig fromFile(String path) throws IOException {
    var input = new FileInputStream(path);
    var properties = new Properties();
    properties.load(input);
    var host = properties.getProperty("square.host");
    var port = properties.getProperty("square.port");
    return new ClientConfig(host, port, "");
  }

  public static ClientConfig fromEnv() {
    var processBuilder = new ProcessBuilder();
    var envVariable = processBuilder.environment();
    String hostname = null;
    try {
      var process = processBuilder.command("sh", "-c", "hostname").start();
      var exitStatus = process.waitFor();
      if (exitStatus != 0) {
        return null;
      }
      var outputStream = process.getInputStream();
      hostname = getOutputOfCommand(outputStream).split("line.separator")[0];
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
    var host = envVariable.get("SQUARE_HOST");
    var port = envVariable.get("SQUARE_PORT");
    return new ClientConfig(host, port, hostname);
  }

  @Override
  public String toString() {
    return "HOST=" + squareHost + "; PORT=" + squarePort + "; DOCKERID=" + dockerId;
  }
}
