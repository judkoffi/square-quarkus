package fr.umlv.square.client;

import java.io.File;
import java.io.IOException;
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
  private final static String OUTPUT_FILE = "outputLogfile.log";
  private final SquareClient squareClient;

  /*
   * Index use to store offset of log read by prvious thread
   */
  private int readingIndex;

  private final Object lock = new Object();

  public Worker() {
    synchronized (lock) {
      this.readingIndex = 0;
      this.squareClient = new SquareClient(ClientConfig.fromEnv());
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
   * @throws IOException
   */
  public void doWork() throws IOException {
    synchronized (lock) {
      /*
       * Read all lines of output's file using Stream API into a list of String and skip previous
       * readed lines
       */
      try (var fileStream = Files.lines(Path.of(OUTPUT_FILE))) {

        var list = fileStream
          .skip(readingIndex)
          .map((mapper) -> LogParser.parseLine(mapper))
          .filter((p) -> !p.getMessage().isBlank() && !p.getMessage().isEmpty())
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
}
