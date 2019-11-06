package fr.umlv.square.client;

public class Main {
  public static void main(String[] args) {
    var config = ClientConfig.fromEnv();
    var squareClient = new SquareClient(config);
    System.out.println(config);

    var i = 0;
    while (i < 50) {
      var msg = "hello " + i;
      new Thread(() -> squareClient.sendLog(msg)).start();
      i++;
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new AssertionError();
      }
    }
  }
}
