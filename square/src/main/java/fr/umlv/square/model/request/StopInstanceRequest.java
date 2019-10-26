package fr.umlv.square.model.request;

import java.util.Objects;
import javax.validation.constraints.Digits;

public class StopInstanceRequest {

  @Digits(fraction = 0, integer = Integer.MAX_VALUE)
  private int id;

  public StopInstanceRequest() {}

  public StopInstanceRequest(int id) {
    this.id = Objects.requireNonNull(id);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "stop request :" + id;
  }
}
