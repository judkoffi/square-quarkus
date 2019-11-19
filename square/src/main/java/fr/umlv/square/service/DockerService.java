package fr.umlv.square.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import fr.umlv.square.model.response.DeployResponse;
import fr.umlv.square.model.response.RunningInstanceInfo;

@ApplicationScoped // One DockerService instance for the whole application
public class DockerService {
  private final static String DOCKERFILE_TEMPLATE; // Dockerfile use as template for all images
  private final static String DOCKERFILES_DIRECTORY = "docker-images/";
  private final static int HOUR_MINUTE_VALUE = 60;
  private final HashMap<Integer, ImageInfo> runningInstanceMap;
  private final String squareHost;
  private final String squarePort;
  private final ProcessBuilderHelper processHelper;

  static {
    DOCKERFILE_TEMPLATE = "FROM hirokimatsumoto/alpine-openjdk-11\n" + "WORKDIR /app\n"
        + "COPY apps/{{1}}.jar /app/app.jar\n"
        + "COPY lib-client/square-client.jar /app/client.jar\n" + "ENV SQUARE_HOST={{3}}\n"
        + "ENV SQUARE_PORT={{4}}\n" + "RUN chmod 775 /app\n" + "EXPOSE {{2}}\n";
  }

  /*
   * @ConfigProperty(name = "quarkus.http.host") read this api host from application.properties
   */
  public DockerService(@ConfigProperty(name = "quarkus.http.host") String squareHost,
      @ConfigProperty(name = "quarkus.http.port") String squarePort) {
    this.squareHost = squareHost;
    this.squarePort = squarePort;
    this.runningInstanceMap = new HashMap<Integer, ImageInfo>();
    this.processHelper = new ProcessBuilderHelper();
  }

  private String createDockerFile(String appName, int port) {
    var imageFile = DOCKERFILE_TEMPLATE
      .replace("{{1}}", appName)
      .replace("{{2}}", "" + port)
      .replace("{{3}}", squareHost)
      .replace("{{4}}", squarePort)
      .concat("CMD java -jar client.jar");

    var imagePath = DOCKERFILES_DIRECTORY + "Dockerfile." + appName;
    var createDockerfileCommand = "echo \"" + imageFile + "\" > " + imagePath;
    return processHelper.execWaitForCommand(createDockerfileCommand) ? imagePath : null;
  }

  private boolean buildImage(String imagePath, String appName) {
    var builImageCommand = "docker build -f " + imagePath + " -t " + appName + " ./";
    return processHelper.execWaitForCommand(builImageCommand);
  }

  private int generateId() {
    var id = 1 + new Random().nextInt(Integer.MAX_VALUE);
    return (!runningInstanceMap.containsKey(id)) ? id : generateId();
  }

  private static int generateRandomPort() {
    ServerSocket s = null;
    try {
      s = new ServerSocket(0);
      return s.getLocalPort();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      assert s != null;
      try {
        s.close();
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
  }

  // TODO: Remove modulo
  private PairPidId runImage(String imageName, int appPort, int servicePort) {
    var id = generateId() % 500;
    var uniqueName = imageName + "-" + id;

    var cmd = "docker run --rm -p " + servicePort + ":" + appPort + " --name " + uniqueName
        + " --hostname=" + uniqueName + " " + imageName;

    var pid = processHelper.execAliveCommand(cmd);
    return (pid != -1) ? new PairPidId(id, pid) : null;
  }

  private boolean generateAndBuildDockerFile(String appName, int appPort) {
    var dockerImagePath = createDockerFile(appName, appPort);
    return dockerImagePath == null ? false : buildImage(dockerImagePath, appName);
  }

  private boolean isAlreadyImage(String appName) {
    var cmd = "docker image inspect " + appName;
    var cmdResult = processHelper.execOutputCommand(cmd);
    return cmdResult.contains("[]");
  }

  private Optional<ImageInfo> runBuildedImage(String appName, int appPort) {
    var servicePort = generateRandomPort();
    var ranImageId = runImage(appName, appPort, servicePort);

    if (ranImageId.id == -1 || ProcessHandle.of(ranImageId.pid).isEmpty())
      return Optional.empty();

    var dockerInstance = appName + "-" + ranImageId.id;
    return Optional
      .of(new ImageInfo(appName, new Timestamp(System.currentTimeMillis()).toString(), appPort,
          servicePort, dockerInstance, ranImageId.id));
  }

  /**
   * @param appName: name of application to be deploy in docker container
   * @param port: port of application redirected form docker container to current system
   * @param defaultPort: port of application to be deploy in docker container
   * 
   * @return {@link Optional} {@link DeployResponse}: information of ran container
   */
  public Optional<DeployResponse> runContainer(String appName, int appPort) {
    try {
      if (!isAlreadyImage(appName)) {
        System.err.println("not already image");
        var makeDockerfile = generateAndBuildDockerFile(appName, appPort);
        if (!makeDockerfile)
          return Optional.empty();
      }

      var imageInfo = runBuildedImage(appName, appPort);
      if (imageInfo.isEmpty())
        return Optional.empty();

      var info = imageInfo.get();
      runningInstanceMap.put(info.squareId, info);

      System.out.println(info);

      return Optional
        .of(new DeployResponse(info.squareId, appName, appPort, info.servicePort,
            info.dockerInstance));

    } catch (AssertionError e) {
      return Optional.empty();
    }
  }

  private String buildElapsedTime(long diff) {
    var calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(diff);
    var hoursToMinutes = calendar.get(Calendar.HOUR_OF_DAY) * HOUR_MINUTE_VALUE;
    var minutes = hoursToMinutes + calendar.get(Calendar.MINUTE);
    return minutes + "m" + calendar.get(Calendar.SECOND) + "s";
  }

  /**
   * Extract each token of the line
   * 
   * @param tokens
   * @return
   */
  private ImageInfo psLinetoImageInfo(String[] tokens) {
    var id = Integer.parseInt(tokens[6].split("-")[1]);
    var ports = tokens[5].split(":");
    var servicePort = Integer.parseInt(ports[1].split("->")[0]);
    var appPort = Integer.parseInt(((ports[1].split("->")[0]).split("/"))[0]);
    var timestampString = (tokens[3].trim());
    var dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");

    Date date = null;
    try {
      date = dateFormatter.parse(timestampString);
    } catch (ParseException e) {
      throw new AssertionError(e);
    }

    var diff = System.currentTimeMillis() - date.getTime();

    return new ImageInfo(tokens[1], buildElapsedTime(diff), appPort, servicePort, tokens[6], id);
  }

  /**
   * Extract one line of the ps command
   * 
   * @param psOutput
   * @param predicate
   * @return
   */
  private List<ImageInfo> parseDockerPs(String psOutput, Predicate<String> predicate) {
    var regex = "([A-Z\\s]+?)($|\\s{2,})";
    var lines = psOutput.trim().split("\n");

    return Arrays
      .stream(lines)
      .skip(1)
      .filter(predicate)
      .map((elt) -> elt.split(regex))
      .map((tokens) -> psLinetoImageInfo(tokens))
      .collect(Collectors.toList());
  }


  public Optional<List<RunningInstanceInfo>> getRunnningList() {
    var cmd =
        "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";

    var cmdResult = processHelper.execOutputCommand(cmd);
    if (cmdResult == null) {
      return Optional.empty();
    }

    List<ImageInfo> imageInfos = parseDockerPs(cmdResult, (p) -> true);

    var list =
        imageInfos.stream().map((mapper) -> new RunningInstanceInfo(mapper.squareId, mapper.imageName, mapper.appPort, mapper.servicePort, mapper.dockerInstance, mapper.created)).collect(Collectors.toList());

    return Optional.of(list);
  }

  /*
   * 
   * TODO: Fix elapsed-time field bug Actual "elapsed-time": "2019-11-18 05:40:41.459" but must be
   * XmYs Improve implement of buildElapsedTime to use this method to compute elasped time Possible
   * to add PID check to have an excellent proof
   */
  public Optional<RunningInstanceInfo> stopApp(int key) {
    var runningInstance = runningInstanceMap.get(key);
    var cmd = "docker kill " + runningInstance.dockerInstance;

    var cmdResult = processHelper.execWaitForCommand(cmd);

    if (!cmdResult) {
      return Optional.empty();
    }

    runningInstanceMap.remove(key);

    return Optional
      .of(new RunningInstanceInfo(runningInstance.squareId, runningInstance.imageName,
          runningInstance.appPort, runningInstance.servicePort, runningInstance.dockerInstance,
          runningInstance.created));
  }

  public int findSquareIdFromContainerId(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().dockerInstance.equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? -1 : imageInfo.get().getKey();
  }

  public String findDockerInstanceFromContainerId(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().dockerInstance.equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? null : imageInfo.get().getValue().dockerInstance;
  }

  public String findAppNameFromContainerId(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().dockerInstance.equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? null : imageInfo.get().getValue().imageName;
  }

  /**
   * Class use to return a pair of value (ID -> PID) PID is use to check if instance is isAlive
   */
  private static class PairPidId {
    private final int id;
    private final long pid;

    public PairPidId(int id, long pid) {
      this.id = Objects.requireNonNull(id);
      this.pid = Objects.requireNonNull(pid);
    }
  }

}
