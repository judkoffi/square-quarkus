package fr.umlv.square.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Main {
  private static int readIndex;

  private final static String OUTPUT_FILE = "outputLogfile.log";
  private final static String ERREUR_FILE = "errorLogfile.log";


  public static void main(String[] args) {
    var config = ClientConfig.fromEnv();
    var squareClient = new SquareClient(config);

    var runApp = "java -jar app.jar";
    var processBuilder = new ProcessBuilder().inheritIO(); // launch processBuilder in same console
                                                           // that runApp command
    var outputLogFile = new File(OUTPUT_FILE);
    var erreurLogFile = new File(ERREUR_FILE);
    processBuilder.redirectOutput(outputLogFile);
    processBuilder.redirectError(erreurLogFile);

    Process process = null;
    try {
      process = processBuilder.command("sh", "-c", runApp).start();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    var i = 0;
    while (i < 50) {
      try {
        var list = Files.lines(Path.of(OUTPUT_FILE)).collect(Collectors.toList());
        var newMessageList = list.subList(readIndex, list.size());
        var msg = newMessageList.toString();
        readIndex = list.size();
        new Thread(() -> squareClient.sendLog(msg, LogType.INFO)).start();
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
