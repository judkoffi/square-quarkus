package fr.umlv.square.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) {
    var config = ClientConfig.fromEnv();
    var squareClient = new SquareClient(config);
    System.out.println(config);

    var runApp = "java -jar app.jar";
    var processBuilder = new ProcessBuilder().inheritIO(); // launch processBuilder in same console
                                                           // that runApp command

    var logFile = new File("logfile.log");
    processBuilder.redirectOutput(logFile);

    Process process = null;
    try {
      process = processBuilder.command("sh", "-c", runApp).start();
    } catch (IOException e1) {
      e1.printStackTrace();
    }


    var i = 0;
    while (i < 50) {
      try {
        var msg = Files.lines(Path.of("logfile.log")).collect(Collectors.toList()).toString();
        new Thread(() -> squareClient.sendLog(msg)).start();
        i++;
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new AssertionError();
        }
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }
}
