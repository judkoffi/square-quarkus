package fr.umlv.square.client;

import java.io.IOException;

public class Main {
  private static final String PROPERTIES_PATH = "client.properties";

  public static void main(String[] args) {

    try {
      var config = ClientConfig.fromFile(PROPERTIES_PATH);
      var squareClient = new SquareClient(config);
      squareClient.sendLog();
    } catch (IOException e) {
      System.exit(-1);
    } catch (InterruptedException e) {
      // TODO
    }

  }
}
