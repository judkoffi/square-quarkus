package fr.umlv.square.client;

import java.io.IOException;

public class Main {

  public static void main(String[] args) {
    var config = ClientConfig.fromEnv();
    var squareClient = new SquareClient(config);
    System.out.println(config);

    var i = 0;
    while (i < 50) {
      var msg = "hello" + i;
      new Thread(() ->
      {
        try {
          squareClient.sendLog(msg);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }).start();
      i++;
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
