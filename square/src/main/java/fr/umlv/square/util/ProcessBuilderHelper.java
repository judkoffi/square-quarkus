package fr.umlv.square.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import fr.umlv.square.model.service.ImageInfo;

/**
 * This class is used to get some information on a process builder execution 
 */

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

  // Method used to create an ImageInfo from the String output of the "ps" command  
  private static ImageInfo psLinetoImageInfo(String[] tokens) {
    var id = Integer.parseInt(tokens[6].split("-")[1]);
    var ports = tokens[5].split(":");
    var servicePort = Integer.parseInt(ports[1].split("->")[0]);
    var appPort = Integer.parseInt(((ports[1].split("->")[1]).split("/"))[0]);
    var timestampString = (tokens[3].trim());
    var dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");

    Date date = null;
    try {
      date = dateFormatter.parse(timestampString);
    } catch (ParseException e) {
      throw new AssertionError(e);
    }

    var diff = System.currentTimeMillis() - date.getTime();

    return new ImageInfo(tokens[1], diff, appPort, servicePort, tokens[6], id);
  }

  /**
   * Extract one line of the ps command
   * @param psOutput
   * @param predicate
   * @return a List of ImageInfo
   */
  public static List<ImageInfo> parseDockerPs(String psOutput, Predicate<String> predicate) {
    var regex = "([A-Z\\s]+?)($|\\s{2,})";
    var lines = psOutput.trim().split("\n");

    return Arrays
      .stream(lines)
      .skip(1)
      .filter(predicate)
      .map((elt) -> elt.split(regex))
      .map((tokens) -> psLinetoImageInfo(tokens))
      .collect(Collectors.toList());
  }

  /**
   * Allow to get a List of ImageInfo from the String output of the "ps" command
   * @param lines : String which come from the String output of the "ps" command
   * @return : List of ImageInfo
   */
  public List<ImageInfo> dockerPsToImageInfo(String lines) {
    return parseDockerPs(lines, (e) -> true);
  }

  /**
   * Allow to executed a command with a processBuilder which wait until the command has finished
   * @param cmd : String : the command to execute
   * @return boolean : true if the command ended well, false otherwise
   */
  public boolean execWaitForCommand(String cmd) {
    try {
      var exitValue = processBuilder.command("bash", "-c", cmd).start().waitFor();
      return (exitValue == 0);
    } catch (IOException | InterruptedException e) {
      return false;
    }
  }

  /**
   * Allow to get the output of a command executed
   * @param cmd : String : the command to execute
   * @return String : the output of the command
   */
  public String execOutputCommand(String cmd) {
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();
      return (exitValue == 0) ? getOutputOfCommand(outputStream) : "";
    } catch (IOException | InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Allow to get the path where the current processBuilder execute its commands
   * @return : String which is the path where the current processBuilder execute its commands
   */
  public String getRootPah() {
    return processBuilder.directory().getAbsolutePath() + '/';
  }
}
