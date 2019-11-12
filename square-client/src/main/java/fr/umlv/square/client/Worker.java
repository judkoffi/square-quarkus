package fr.umlv.square.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import fr.umlv.square.model.LogModel;

public class Worker {
  private final static String OUTPUT_FILE = "outputLogfile.log";
  private final static String ERREUR_FILE = "errorLogfile.log";
  private final SquareClient squareClient;

  private int outputReadingIndex;
  private int erreurReadingIndex;

  public Worker() {
    this.outputReadingIndex = 0;
    this.erreurReadingIndex = 0;
    this.squareClient = new SquareClient(ClientConfig.fromEnv());
  }

  public boolean startApp() {
    var runApp = "java -jar app.jar";
    var processBuilder = new ProcessBuilder().inheritIO(); // launch processBuilder in same console that runApp command
    var outputLogFile = new File(OUTPUT_FILE);  
    var erreurLogFile = new File(ERREUR_FILE);
    processBuilder.redirectOutput(outputLogFile);   // set the standard output destination of the processBuilder to the file
    processBuilder.redirectError(erreurLogFile);    // set the standard error destination of the processBuilder to the file

    try {
      var process = processBuilder.command("sh", "-c", runApp).start();
      return process.isAlive();
    } catch (IOException e1) {
      return false;
    }
  }

  private String parseListLog(List<String> logs) {
    return logs
      .stream()
      .map((mapper) -> LogParser.parseLine(mapper).split(System.getProperty("line.separator"))) // apply the regex to parse the log 
      .map((mapper) -> new LogModel(mapper[2], mapper[0], mapper[1]).toString()) // create LogModel which defines a log
      .collect(Collectors.joining(" <> ")); // each log separate by <>
  }


  public void doWork() throws IOException {
    var list = Files.lines(Path.of(OUTPUT_FILE)).collect(Collectors.toList());  // list of Stream
    var newMessageList = list.subList(outputReadingIndex, list.size()); // read from the new part added to the file
    System.out.println("newMessageList");
    System.out.println(newMessageList);
    var msg = parseListLog(newMessageList);
    System.out.println("msg");
    System.out.println(msg);
    outputReadingIndex = list.size();   // to know the actual end of the file
    new Thread(() -> squareClient.sendInfoLog(msg)).start();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new AssertionError();
    }
  }
}
