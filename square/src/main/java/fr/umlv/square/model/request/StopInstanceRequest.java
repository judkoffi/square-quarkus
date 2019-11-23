package fr.umlv.square.model.request;

import java.util.Objects;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Model request use to map an stop instance post request as a java object
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

  /**
   * Allow to get the id of an instance top stop
   * @return : int : the id of an instance to stop
   */
  public int getId() {
    return id;
  }

  /**
   * Allow to set the if of a docker instance top stop
   * @param id : int : the id of an instance top stop
   */
  public void setId(int id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "stop request :" + id;
  }
}
