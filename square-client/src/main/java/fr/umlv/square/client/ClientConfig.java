package fr.umlv.square.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Helper class use to read some information like Square host and port from env variables and
 * container id
 */
class ClientConfig {
  final String squareHost;
  final int squarePort;
  final String dockerId;

  private ClientConfig(String host, String port, String dockerId) {
    this.squareHost = Objects.requireNonNull(host);
    this.squarePort = Integer.parseInt(Objects.requireNonNull(port));
    this.dockerId = Objects.requireNonNull(dockerId);
  }

  /**
   * Helper method use to read output of command from STDOUT
   * 
   * @param outputStream: process STDOUT stream
   * @return {@String} which represent display in output console
   * @throws IOException
   */
  private static String getOutputOfCommand(InputStream outputStream) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(outputStream));
    var builder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    return builder.toString();
  }

  /**
   * Method use to read square's host and port from env variable.
   * 
   * @return {@link ClientConfig} contain Square API address, port and current docker id use to
   *         identified origin of log message
   */
  public static ClientConfig fromEnv() {
    var processBuilder = new ProcessBuilder();
    var envVariable = processBuilder.environment();
    String hostname = null;
    try {
      var process = processBuilder.command("sh", "-c", "hostname").start();
      var exitStatus = process.waitFor();
      if (exitStatus != 0) {
        return ClientConfig.defaultConfig();
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

  public static ClientConfig defaultConfig() {
    return new ClientConfig("0.0.0.0", "5050", "-1");
  }


  @Override
  public String toString() {
    return "HOST=" + squareHost + "; PORT=" + squarePort + "; DOCKERID=" + dockerId;
  }
}
