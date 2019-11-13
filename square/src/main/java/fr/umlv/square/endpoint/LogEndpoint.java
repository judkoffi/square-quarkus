package fr.umlv.square.endpoint;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.umlv.square.model.response.LogTimeResponse;
import fr.umlv.square.util.SquareHttpStatusCode;

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogEndpoint {

  private final static ArrayList<LogTimeResponse> data;

  static {
    data = fillArrayList();
  }

  private static enum FilterType {
    ID, APPLICATION, DOCKER, UNKNOWN
  };

  @GET
  @Path("/{time}")
  /**
   * Endpoint who gives logs since a time given as argument
   * 
   * @param time : the last minutes for which the logs are wanted
   * @return a Response in JSON
   */
  public Response list(@PathParam("time") String time) {
    if (time == null || !isNumeric(time))
      return Response
        .status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE)
        .entity("Invalid time value")
        .build();

    var result = logsFiltedByTime(time).map(e -> e.toJson()).collect(Collectors.toList());
    return Response.ok().entity(result.toString()).build();
  }

  @GET
  @Path("/{time}/{filter}")
  /**
   * Endpoint who gives logs since a time given as argument
   * 
   * @param time : the last minutes for which the logs are wanted
   * @param filter : a filter by id, app name or an instance name
   * @return a Response in JSON
   */
  public Response listFilter(@PathParam("time") String time, @PathParam("filter") String filter) {
    if (time == null || filter == null || !isNumeric(time))
      return Response
        .status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE)
        .entity("Invalid time value")
        .build();

    var result = logsFiltedByTime(time)
      .filter(getPredicate(filter))
      .map(e -> e.toJson())
      .collect(Collectors.toList());

    return Response.ok().entity(result.toString()).build();
  }

  /**
   * Fill an array of data to test the roads
   * 
   * @return an ArrayList of data
   */
  private static ArrayList<LogTimeResponse> fillArrayList() {
    var arraylist = new ArrayList<LogTimeResponse>(10);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 5; j++) {
        arraylist
          .add(new LogTimeResponse(j, "app:" + i, 80 + i, 80 + i, "docker_80-" + i, "log_" + i,
              new Timestamp(System.currentTimeMillis() - (i + j * 1000 * 60)).toString()));
      }
    }
    return arraylist;
  }

  /**
   * Convert minutes into milliseconds
   * 
   * @param minutes : the minutes to convert
   * @return the minutes given converted into milliseconds
   */
  private static long convertMinuteToMillisecond(long minutes) {
    return TimeUnit.MINUTES.toMillis(minutes);
  }

  /**
   * Filter the data ArrayList with only the logs that are before the number of minutes given as
   * argument
   * 
   * @param timestamp : the last minutes for which the logs are wanted
   * @return a Stram of the logs which are in the "timestamp" last minutes
   */
  private static Stream<LogTimeResponse> logsFiltedByTime(String timestamp) {
    var timeTarget = convertMinuteToMillisecond(Long.parseLong(timestamp));
    var filterTimestamp = new Timestamp(System.currentTimeMillis() - timeTarget);
    return data
      .stream()
      .filter((log) -> log.getTimestamp().compareTo(filterTimestamp.toString()) > 0);

  }

  /**
   * Give the predicate which allow to filter logs by a String
   * 
   * @param filter : the String used to filter logs
   * @return a Predicate corresponding to the type of filter wanted
   */
  private static Predicate<LogTimeResponse> getPredicate(String filter) {
    var filterType = findFilterType(filter);
    switch (filterType) {
      case ID:
        return (e) -> e.getId() == Integer.parseInt(filter);
      case APPLICATION:
        return (e) -> e.getAppName().equals(filter);
      case DOCKER:
        return (e) -> e.getDockerInstance().equals(filter);
      default:
        return (e) -> false;
    }
  }

  /**
   * Allow to know if a String is a numeric. Uses to define if the user want to filter logs by an id
   * 
   * @param filter : a String given by the user to filter the logs
   * @return true if the filter is numeric or false otherwise
   */
  private static boolean isNumeric(String filter) {
    return filter.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
  }

  /**
   * Find the type of filter to do corresponding to the filter given as argument
   * 
   * @param filter : the filter given by the user
   * @return the type of filter corresponding to the filter given as argument
   */
  private static FilterType findFilterType(String filter) {
    if (filter.contains(":"))
      return FilterType.APPLICATION;
    else if (filter.contains("-"))
      return FilterType.DOCKER;
    else if (isNumeric(filter))
      return FilterType.ID;
    else
      return FilterType.UNKNOWN;
  }
}
