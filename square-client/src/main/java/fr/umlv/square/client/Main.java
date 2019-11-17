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
      /*
       * kill main app in docker instance will provoke instance kill and we don't need to use a
       * specicial handle to process if some fatal kill app
       */
      System.exit(-1);
    }
    // TODO use an infinite loop
    var i = 0;
    while (i < 50) {
      new Thread(() ->
      {
        try {
          worker.doWork();
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }).start();
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        return;
      }
      i++;
    }
  }
}
