package fr.umlv.square.client;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.junit.jupiter.api.Test;
import fr.umlv.square.client.model.LogModel;

public class SquareClientTest {
  @Test
  public void testSquareCLient() {
    var client = new SquareClient(ClientConfig.defaultConfig());
    assertThrows(NullPointerException.class, () -> client.sendInfoLog(null));
  }

  @Test
  public void testBuildJson() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    var e1 = new LogModel("toto", "2019-12-02 00:57:06,373", "INFO");
    var e2 = new LogModel("tata", "2019-12-02 00:57:06,373", "INFO");
    var e3 = new LogModel("titi", "2019-12-02 00:57:06,373", "INFO");
    var list = List.of(e1, e2, e3);

    var expected = "{\"dockerInstance\":\"-1\", \"logs\":" + list + "}";
    var method = SquareClient.class.getDeclaredMethod("buildJson", List.class);
    method.setAccessible(true);

    var value = method.invoke(new SquareClient(ClientConfig.defaultConfig()), list);
    assertEquals(expected, value);
  }

  @Test
  public void testBuildNPEJson() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    var method = SquareClient.class.getDeclaredMethod("buildJson", List.class);
    method.setAccessible(true);

    assertThrows(NullPointerException.class,
        () -> method.invoke(new SquareClient(ClientConfig.defaultConfig()), List.of(null)));
  }

  @Test
  public void testStatusJson() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    var expected = "{\"dockerInstance\":\"-1\", \"status\":false}";
    var method = SquareClient.class.getDeclaredMethod("buildStatusJson", boolean.class);
    method.setAccessible(true);

    var value = method.invoke(new SquareClient(ClientConfig.defaultConfig()), false);
    assertEquals(expected, value);
  }


  @Test
  public void testStatusJson2() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    var expected = "{\"dockerInstance\":\"-1\", \"status\":true}";
    var method = SquareClient.class.getDeclaredMethod("buildStatusJson", boolean.class);
    method.setAccessible(true);

    var value = method.invoke(new SquareClient(ClientConfig.defaultConfig()), true);
    assertEquals(expected, value);
  }


  @Test
  public void testSendRequest() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    var method = SquareClient.class.getDeclaredMethod("sendRequest", String.class, String.class);
    method.setAccessible(true);

    var value = (boolean) method
      .invoke(new SquareClient(ClientConfig.defaultConfig()), "http://127.0.0.1", "8070");
    assertFalse(value);
  }

  @Test
  public void testSendNPERequest() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    var method = SquareClient.class.getDeclaredMethod("sendRequest", String.class, String.class);
    method.setAccessible(true);

    assertAll(() ->
    {
      assertThrows(IllegalArgumentException.class,
          () -> method.invoke(new SquareClient(ClientConfig.defaultConfig()), "a", 1));
    }, () ->
    {
      assertThrows(InvocationTargetException.class,
          () -> method.invoke(new SquareClient(ClientConfig.defaultConfig()), "", ""));

    });
  }

}
