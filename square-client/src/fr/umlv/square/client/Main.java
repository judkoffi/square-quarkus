package fr.umlv.square.client;

import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    var squareClient = new SquareClient();
    squareClient.sendLog();
  }
}
