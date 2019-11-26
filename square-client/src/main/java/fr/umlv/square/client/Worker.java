package fr.umlv.square.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Worker class is a class use to manage application start, log reading and log sending to Square
 * API for persistence
 */
public class Worker {
  /*
   * File use to store all of output from runned app
   */
  private static final String OUTPUT_FILE = "outputLogfile.log";
  private final SquareClient squareClient;
  private boolean appIsAlive; // Use a boolean as default true to check if app is alive


  /*
   * Index use to store offset of log read by previous thread
   */
  private int readingIndex;

  private final Object lock = new Object();

  public Worker() {
    synchronized (lock) {
      this.readingIndex = 0;
      this.squareClient = new SquareClient(ClientConfig.fromEnv());
      this.appIsAlive = true;
    }
  }

  /**
   * Start the app app.jar in the container
   * 
   * @return true if the app has started correctly and false otherwise
   */
  public boolean startApp() {
    synchronized (lock) {
      var runApp = "java -jar app.jar";
      /*
       * launch processBuilder in same console that runApp command
       */
      var processBuilder = new ProcessBuilder().inheritIO();
      var outputLogFile = new File(OUTPUT_FILE);

      /*
       * set the standard output destination of the processBuilder to the log file
       */
      processBuilder.redirectOutput(outputLogFile);
      /*
       * set the standard error destination of the processBuilder to the log file
       */
      processBuilder.redirectError(outputLogFile);

      try {
        var process = processBuilder.command("sh", "-c", runApp).start();
        return process.isAlive();
      } catch (IOException e1) {
        return false;
      }
    }
  }

  /**
   * Sends logs of the application running to the endpoint API of square
   * 
   * @throws IOException
   */
  public void doWork() throws IOException {
    synchronized (lock) {
      if (!appIsAlive) {
        squareClient.sendAppStatus(appIsAlive);
        return;
      }
      /*
       * Read all lines of output's file using Stream API into a list of String and skip previous
       * read lines
       */
      try (var fileStream = Files.lines(Path.of(OUTPUT_FILE))) {

        var list = fileStream
          .skip(readingIndex)
          .map(LogParser::parseLine)
          .filter(p -> !p.getMessage().isBlank() && !p.getMessage().isEmpty())
          .collect(Collectors.toList());

        if (list.isEmpty())
          return;

        /*
         * to know the actual end of the file. If no new lines -> keep older index value else update
         * index
         */
        if (squareClient.sendInfoLog(list)) {
          readingIndex = readingIndex + list.size();
          System.out.println("send, index" + readingIndex);
        } else {
          System.out.println("No send, index" + readingIndex);
        }
      }
    }
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

  public void checkAppAlive() {
    synchronized (lock) {
      var processBuilder = new ProcessBuilder();
      try {
        var processPS = processBuilder.command("sh", "-c", "ps -e | grep app.jar").start();
        if (processPS.waitFor() == 0) {
          var stdout = processPS.getInputStream();
          var output = getOutputOfCommand(stdout);
          System.out.println(output);
          if (!output.contains("java -jar app.jar")) {
            appIsAlive = false;
          }
        }
      } catch (IOException | InterruptedException e) {
        return;
      }
    }
  }
}
