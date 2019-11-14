package fr.umlv.square.client;

import java.io.IOException;

/**
 * Main class, main entry for lib client
 */
public class Main {
  public static void main(String[] args) throws IOException {
    var worker = new Worker();
    if (!worker.startApp()) {
      System.err.println("App not start");
      //System.exit(-1);
    }
    // TODO use an infinite loop
    var i = 0;
 //   while (i < 50) {
      try {
        worker.doWork(); // launch thread
      } catch (IOException e) {
        e.printStackTrace();
      }
      i++;
   // }
  }
}
