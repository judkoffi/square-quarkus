package fr.umlv.square.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import fr.umlv.square.model.response.DeployResponse;
import fr.umlv.square.model.response.RunningInstanceInfo;
import fr.umlv.square.model.service.ImageInfo;
import fr.umlv.square.util.ProcessBuilderHelper;

@ApplicationScoped // One DockerService instance for the whole application
public class DockerService {
  private final static String DOCKERFILE_TEMPLATE; // Dockerfile use as template for all images
  private final static String DOCKERFILES_DIRECTORY = "docker-images/";
  private final static int HOUR_MINUTE_VALUE = 60;
  private final HashMap<Integer, ImageInfo> runningInstanceMap;
  private final ProcessBuilderHelper processHelper;

  @ConfigProperty(name = "quarkus.http.host")
  String squareHost;
  @ConfigProperty(name = "quarkus.http.port")
  String squarePort;

  static {
    DOCKERFILE_TEMPLATE = "FROM hirokimatsumoto/alpine-openjdk-11\n" + "WORKDIR /app\n"
        + "COPY apps/{{1}}.jar /app/app.jar\n"
        + "COPY lib-client/square-client.jar /app/client.jar\n" + "ENV SQUARE_HOST={{3}}\n"
        + "ENV SQUARE_PORT={{4}}\n" + "RUN chmod 775 /app\n" + "EXPOSE {{2}}\n";
  }

  /*
   * @ConfigProperty(name = "quarkus.http.host") read this api host from application.properties
   */
  public DockerService() {
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

    var cmd = "docker run --rm -d -p " + servicePort + ":" + appPort + " --name " + uniqueName
        + " --hostname=" + uniqueName + " " + imageName;

    var pid = processHelper.execAliveCommand(cmd);
    return (pid != -1) ? new PairPidId(id, pid) : null;
  }

  private boolean generateAndBuildDockerFile(String appName, int appPort) {
    var dockerImagePath = createDockerFile(appName, appPort);
    return dockerImagePath == null ? false : buildImage(dockerImagePath, appName);
  }

  // TODO: Have better implementation, because it's not work
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
      .of(new ImageInfo(appName, System.currentTimeMillis(), appPort, servicePort, dockerInstance,
          ranImageId.id));
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
      // if (!isAlreadyImage(appName)) {
      System.err.println("not already image");
      var makeDockerfile = generateAndBuildDockerFile(appName, appPort);
      if (!makeDockerfile)
        return Optional.empty();
      // }

      var imageInfo = runBuildedImage(appName, appPort);
      if (imageInfo.isEmpty())
        return Optional.empty();

      var info = imageInfo.get();
      runningInstanceMap.put(info.getSquareId(), info);

      System.out.println(info);

      return Optional
        .of(new DeployResponse(info.getSquareId(), appName, appPort, info.getServicePort(),
            info.getDockerInstance()));

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

  public Optional<List<RunningInstanceInfo>> getRunnningList() {
    var cmd =
        "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";

    var cmdResult = processHelper.execOutputCommand(cmd);
    if (cmdResult == null) {
      return Optional.empty();
    }

    List<ImageInfo> imageInfos = ProcessBuilderHelper.parseDockerPs(cmdResult, (p) -> true);

    var list = imageInfos
      .stream()//
      .map((mapper) -> new RunningInstanceInfo(mapper.getSquareId(), mapper.getImageName(), mapper.getAppPort(), mapper.getServicePort(), mapper.getDockerInstance(), buildElapsedTime(mapper.getCreated())))//
      .collect(Collectors.toList());

    return Optional.of(list);
  }

  /*
   * TODO: add pid check
   */
  public Optional<RunningInstanceInfo> stopApp(int key) {
    var runningInstance = runningInstanceMap.get(key);
    var cmd = "docker kill " + runningInstance.getDockerInstance();

    var cmdResult = processHelper.execWaitForCommand(cmd);

    if (!cmdResult) {
      return Optional.empty();
    }

    runningInstanceMap.remove(key);
    var diff = System.currentTimeMillis() - runningInstance.getCreated();

    return Optional
      .of(new RunningInstanceInfo(runningInstance.getSquareId(), runningInstance.getImageName(),
          runningInstance.getAppPort(), runningInstance.getServicePort(),
          runningInstance.getDockerInstance(), buildElapsedTime(diff)));
  }

  public int findSquareIdFromContainerId(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().getDockerInstance().equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? -1 : imageInfo.get().getKey();
  }

  public String findDockerInstanceFromContainerId(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().getDockerInstance().equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? null : imageInfo.get().getValue().getDockerInstance();
  }

  public String findAppNameFromContainerId(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().getDockerInstance().equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? null : imageInfo.get().getValue().getImageName();
  }

  public ImageInfo findImageInfoByDockerInstance(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().getDockerInstance().equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? null : imageInfo.get().getValue();
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

  void putInstance(ImageInfo instance) {
    runningInstanceMap.put(instance.getSquareId(), instance);
  }

  HashMap<Integer, ImageInfo> getRunningInstanceMap() {
    return runningInstanceMap;
  }

}
