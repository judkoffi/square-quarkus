package fr.umlv.square.client.main;

import java.io.IOException;
import fr.umlv.square.client.Worker;

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

    // TODO: Interrupted thred if app is killed
    while (!Thread.interrupted()) {
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
    }
  }
}
