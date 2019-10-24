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

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogsEndpoint {
	@GET
	@Path("/{time}")
	public Response list(@PathParam("time") String time) {
		if (!isNumeric(time))
			return Response.status(400).entity("Invalid time value").build();

		return Response.ok().entity(logsFiltedByTime(time).map(e -> e.toJson()).collect(Collectors.toList()).toString())
				.build();
	}

	@GET
	@Path("/{time}/{filter}")
	public Response listFilter(@PathParam("time") String time, @PathParam("filter") String filter) {
		if (!isNumeric(time))
			return Response.status(400).entity("Invalid time value").build();

		return Response.ok().entity(logsFiltedByTime(time).filter(getPredicate(filter)).map(e -> e.toJson())
				.collect(Collectors.toList()).toString()).build();
	}

	private final static ArrayList<LogTimeResponse> data;
	static {
		data = fillArrayList();
	}

	private static enum FilterType {
		ID, APPLICATION, DOCKER, UNKNOWN
	};

	private static ArrayList<LogTimeResponse> fillArrayList() {
		var arraylist = new ArrayList<LogTimeResponse>(10);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				arraylist.add(new LogTimeResponse(j, "app:" + i, 80 + i, "80" + i, "docker_80-" + i, "log_" + i,
						new Timestamp(System.currentTimeMillis() - (i + j * 1000 * 60)).toString()));
			}
		}
		return arraylist;
	}

	private static long convertMinuteToMillisecond(long minutes) {
		return TimeUnit.MINUTES.toMillis(minutes);
	}

	private static Stream<LogTimeResponse> logsFiltedByTime(String timestamp) {
		var timeTarget = convertMinuteToMillisecond(Long.parseLong(timestamp));
		var filterTimestamp = new Timestamp(System.currentTimeMillis() - timeTarget);
		return data.stream().filter((elt) -> elt.getTimestamp().compareTo(filterTimestamp.toString()) > 0);

	}

	private static Predicate<LogTimeResponse> getPredicate(String filter) {
		var filterType = findFilterType(filter);
		switch (filterType)
		{
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

	public static boolean isNumeric(String filter) {
		return filter.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
	}

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
