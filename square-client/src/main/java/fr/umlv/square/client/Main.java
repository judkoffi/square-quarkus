package fr.umlv.square.client;

import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    var worker = new Worker();
    if (!worker.startApp()) {
      System.err.println("App not start");
      System.exit(-1);
    }
    var i = 0;
    while (i < 50) {
      try {
        worker.doWork();    // launch thread
      } catch (IOException e) {
        e.printStackTrace();
      }
      i++;
    }
  }
}
