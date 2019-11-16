package fr.umlv.square.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import fr.umlv.square.model.LogModel;

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

  public Worker() {
    this.readingIndex = 0;
    this.squareClient = new SquareClient(ClientConfig.fromEnv());
  }

  /**
   * Start the app app.jar in the container
   * 
   * @return true if the app has started correctly and false otherwise
   */
  public boolean startApp() {
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

  private List<LogModel> rawLinesToLogModels(List<String> logs) {
    /*
     * Split lines separated by line separator (\n -> linux, \r -> windows) to extract information
     * for each line
     */
    var logsLines = logs.toString().split(System.getProperty("line.separator"));
    return Arrays
      .stream(logsLines)
      .map((mapper) -> LogParser.parseLine(mapper))
      .collect(Collectors.toList());
  }


  public void doWork() throws IOException {
    /*
     * Read all lines of output's file using Stream API into a list of String and skip previous
     * readed lines
     */
    var list = Files.lines(Path.of(OUTPUT_FILE)).skip(readingIndex).collect(Collectors.toList());
    // List of new raw log read from file before extract information
    var logsModels = rawLinesToLogModels(list);
    readingIndex = list.size(); // to know the actual end of the file
    // TODO : if log failed -> retry

    new Thread(() -> squareClient.sendInfoLog(logsModels)).start();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new AssertionError();
    }
  }
}
