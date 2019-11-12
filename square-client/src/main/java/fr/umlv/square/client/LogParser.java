package fr.umlv.square.client;

import java.util.regex.Pattern;

public class LogParser {
  // regex to parse the log
  private final static String TIMESTAMP_REGEX =
      "(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})";
  private final static String LEVEL_REGEX = "(?<level>INFO|SEVERE|WARN|TRACE|DEBUG|FATAL)";
  private final static String TEXT_REGEX = "(?<text>.*)";

  private static Pattern linePattern =
      Pattern.compile(TIMESTAMP_REGEX + " " + LEVEL_REGEX + "\\s+" + TEXT_REGEX, Pattern.DOTALL);

  public static String parseLine(String lineToParse) {
    var stringBuilder = new StringBuilder();
    var matcher = linePattern.matcher(lineToParse);
    while (matcher.find()) {
      stringBuilder
        .append(matcher.group("timestamp"))
        .append(System.getProperty("line.separator"))
        .append(matcher.group("level"))
        .append(System.getProperty("line.separator"))
        .append(matcher.group("text"))
        .append(System.getProperty("line.separator"));
    }
    return stringBuilder.toString();
  }

}
