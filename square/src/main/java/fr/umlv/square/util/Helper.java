package fr.umlv.square.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Helper {
  private Helper() {}

  /**
   * Allow to know if a String is a numeric. Uses to define if the user want to filter logs by an id
   * 
   * @param filter : a String given by the user to filter the logs
   * @return true if the filter is numeric or false otherwise
   */
  public static boolean isNumeric(String filter) {
    return filter.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
  }

  /**
   * Convert a date as a String into a date as a Timestamp
   * 
   * @param strDate: date to be convert
   * @param format: strDate format
   * @return a {@TimeStamp} represent strDate
   */
  public static Timestamp convertStringToTimestamp(String strDate, String format) {
    try {
      var formatter = new SimpleDateFormat(format);
      var date = formatter.parse(strDate);
      return new Timestamp(date.getTime());
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }
}
