package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.umlv.square.model.response.RunningInstanceInfo;

public class CheckDockerAlive {
  /* Helper method use to display output after run command on terminal */
  private static String getOutputOfCommand(InputStream outputStream) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(outputStream));
    var builder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
      builder.append(System.getProperty("line.separator"));
    }
    reader.close();
    return builder.toString();
  }

  public static boolean isDockerAlive(ProcessBuilder processBuilder, String appName) {
    try {
      var process = processBuilder.command("bash", "-c", "docker ps").start();
      var outputStream = process.getInputStream();
      var consoleOutput = getOutputOfCommand(outputStream);
      System.out.println(consoleOutput);
      System.out.println(appName + " is alive : " + consoleOutput.contains(appName));
      return consoleOutput.contains(appName);

    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  public static Map<String, Boolean> checkDocker(ProcessBuilder processBuilder,
      ArrayList<RunningInstanceInfo> list) {
    var stateApp = new HashMap<String, Boolean>();
    new Thread(() ->
    {
      // while(true) {
      list.stream().map(e ->
      {
        System.out.println("app : " + e.getAppName());
        System.out.println(isDockerAlive(processBuilder, e.getAppName()));
        return stateApp.put(e.getAppName(), isDockerAlive(processBuilder, e.getAppName()));
      });
      // }
    }).start();
    System.out.println(stateApp);
    return stateApp;
  }
}
