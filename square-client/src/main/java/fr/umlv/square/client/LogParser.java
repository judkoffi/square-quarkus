package fr.umlv.square.client;

import java.util.regex.Pattern;
import fr.umlv.square.model.LogModel;

/**
 * 
 * Helper class use to parse log from console and get log information (date, level, message)
 *
 */
public class LogParser {
  /*
   * Regex use to extract date of log
   */
  private final static String TIMESTAMP_REGEX =
      "(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})";
  /*
   * Regex use to extract level of log
   */
  private final static String LEVEL_REGEX = "(?<level>INFO|SEVERE|WARN|TRACE|DEBUG|FATAL)";

  /*
   * Regex use to extract message of log
   */
  private final static String TEXT_REGEX = "(?<text>.*)";

  /*
   * Regex which combine date, level and message regex to extract line of log
   */
  private static Pattern linePattern =
      Pattern.compile(TIMESTAMP_REGEX + " " + LEVEL_REGEX + "\\s+" + TEXT_REGEX, Pattern.DOTALL);

  /**
   * Method use to recognise each regex and create an string which contain log to send to square
   * 
   * @param lineToParse: raw line of log from log file
   * @return a {@LogModel} which contains extract information from log line read from log's file
   */
  public static LogModel parseLine(String lineToParse) {
    var matcher = linePattern.matcher(lineToParse);
    String date = "";
    String level = "";
    String message = "";
    while (matcher.find()) {
      date = matcher.group("timestamp");
      level = matcher.group("level");
      message = matcher.group("text");
    }
    return new LogModel(message, date, level);
  }
}
