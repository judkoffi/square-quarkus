package fr.umlv.square.model.request;

import java.util.Objects;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Model request use to map an sport instance post request as a java object
 */
public class StopInstanceRequest {
  @NotNull
  @Positive
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
