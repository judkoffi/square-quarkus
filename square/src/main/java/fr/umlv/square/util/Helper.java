package fr.umlv.square.util;

public class Helper {
  /**
   * Allow to know if a String is a numeric. Uses to define if the user want to filter logs by an id
   * 
   * @param filter : a String given by the user to filter the logs
   * @return true if the filter is numeric or false otherwise
   */
  public static boolean isNumeric(String filter) {
    return filter.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
  }

}
