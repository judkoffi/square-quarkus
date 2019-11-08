package fr.umlv.square.client;

import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    var executor = new A();
    if (!executor.startApp()) {
      System.err.println("App not start");
      System.exit(-1);
    }

    var i = 0;
    while (i < 50) {
      try {
        executor.doWork();
      } catch (IOException e) {
        e.printStackTrace();
      }
      i++;
    }
  }
}
