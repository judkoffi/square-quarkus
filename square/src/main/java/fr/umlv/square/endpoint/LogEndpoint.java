package fr.umlv.square.endpoint;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import fr.umlv.square.model.response.LogTimeResponse;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.service.LogService;
import fr.umlv.square.util.SquareHttpStatusCode;

/**
 * This class defines all the endpoints which begin with "/log"
 */

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogEndpoint {

  private final LogService logService;

  @Inject
  public LogEndpoint(LogService logService) {
    this.logService = logService;
  }

  // Describe on which element the user want to do filter the logs
  private static enum FilterType {
    ID, APPLICATION, DOCKER, UNKNOWN
  };

  /**
   * Endpoint who gives logs since a time given as argument
   * @param time : the last minutes for which the logs are wanted
   * @return a Response in JSON
   */
  @GET
  @Path("/{time}")
  public Response list(@PathParam("time") String time) {
    if (time == null || !isNumeric(time))
      return Response
        .status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE)
        .entity("Invalid time value")
        .build();


    // TODO: Improve filter by date method and use Date typeF
    System.out.println("---------------------------------------");
    var result = logService
      .getLogsFiltedByTime(time)
      .stream()
      .map((entity) -> new LogTimeResponse(entity.getSquareId(), entity.getAppName(), entity.getPort(), entity.getServicePort(), entity.getDockerInstance(), entity.getMessage(), entity.getDate().toString()))//
      .map(logTimeResponse -> logTimeResponse.toJson())
      .collect(Collectors.toList());
    return Response.ok().entity(result.toString()).build();
  }

  /**
   * Endpoint who gives logs since a time given as argument
   * @param time : the last minutes for which the logs are wanted
   * @param filter : a filter by id, app name or an instance name
   * @return a Response in JSON
   */
  @GET
  @Path("/{time}/{filter}")
  public Response listFilter(@PathParam("time") String time, @PathParam("filter") String filter) {
    if (time == null || filter == null || !isNumeric(time))
      return Response
        .status(SquareHttpStatusCode.BAD_REQUEST_STATUS_CODE)
        .entity("Invalid time value")
        .build();

    var result = logService
      .getLogsFiltedByTime(time)
      .stream()
      .filter(getPredicate(filter))
      .map((entity) -> new LogTimeResponse(entity.getSquareId(), entity.getAppName(), entity.getPort(), entity.getServicePort(), entity.getDockerInstance(), entity.getMessage(), entity.getDate().toString()))//
      .map(logTimeResponse -> logTimeResponse.toJson())
      .collect(Collectors.toList());

    return Response.ok().entity(result.toString()).build();
  }

  /**
   * Give the predicate which allow to filter logs by a String
   * @param filter : the String used to filter logs
   * @return a Predicate corresponding to the type of filter wanted
   */
  private Predicate<LogEntity> getPredicate(String filter) {
    var filterType = findFilterType(filter);
    switch (filterType) {
      case ID:
        return (e) -> e.getSquareId() == Integer.parseInt(filter);
      case APPLICATION:
        //args: todomvc:8082
        return (e) -> ("" + e.getAppName() + ":" + e.getPort()).equals(filter);
      case DOCKER:
        return (e) -> e.getDockerInstance().equals(filter);
      default:
        return (e) -> false;
    }
  }

  /**
   * Allow to know if a String is a numeric. Uses to define if the user want to filter logs by an id
   * @param filter : a String given by the user to filter the logs
   * @return true if the filter is numeric or false otherwise
   */
  private static boolean isNumeric(String filter) {
    return filter.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
  }

  /**
   * Find the type of filter to do corresponding to the filter given as argument
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
