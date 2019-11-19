package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessBuilderHelper {
  private final ProcessBuilder processBuilder;

  public ProcessBuilderHelper() {
    this.processBuilder = new ProcessBuilder().directory(new File("../../"));
  }

  /* Helper method use to display output after run command on terminal */
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

  public boolean execWaitForCommand(String cmd) {
    try {
      var exitValue = processBuilder.command("bash", "-c", cmd).start().waitFor();
      return (exitValue == 0);
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  public long execAliveCommand(String cmd) {
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      return process.isAlive() ? process.pid() : -1;
    } catch (IOException e) {
      return -1;
    }
  }

  public String execOutputCommand(String cmd) {
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();
      return (exitValue == 0) ? getOutputOfCommand(outputStream) : null;
    } catch (IOException | InterruptedException e) {
      return null;
    }
  }
}
