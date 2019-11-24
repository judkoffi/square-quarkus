package fr.umlv.square.util;

/**
 * Helper class use to store HTTP Status Code used inside Square API
 */

public class SquareHttpStatusCode {
  
  // This class cannot be instantiated
  private SquareHttpStatusCode() {
    throw new IllegalStateException("Utility class");
  }
  
  public static final int CREATED_STATUS_CODE = 201;
  public static final int BAD_REQUEST_STATUS_CODE = 400;
}
