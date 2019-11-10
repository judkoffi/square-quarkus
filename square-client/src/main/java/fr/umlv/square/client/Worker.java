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
    var processBuilder = new ProcessBuilder().inheritIO(); // launch processBuilder in same console
                                                           // that runApp command
    var outputLogFile = new File(OUTPUT_FILE);
    var erreurLogFile = new File(ERREUR_FILE);
    processBuilder.redirectOutput(outputLogFile);
    processBuilder.redirectError(erreurLogFile);

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
      .map((mapper) -> LogParser.parseLine(mapper).split(System.getProperty("line.separator")))
      .map((mapper) -> new LogModel(mapper[2], mapper[0], mapper[1]).toString())
      .collect(Collectors.joining(" <> "));
  }


  public void doWork() throws IOException {
    var list = Files.lines(Path.of(OUTPUT_FILE)).collect(Collectors.toList());
    var newMessageList = list.subList(outputReadingIndex, list.size());
    var msg = parseListLog(newMessageList);
    outputReadingIndex = list.size();
    new Thread(() -> squareClient.sendInfoLog(msg)).start();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new AssertionError();
    }
  }
}
