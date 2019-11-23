package fr.umlv.square.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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

/**
 * This class is used as an interface between the application square and the Docker
 */

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

  /*
   * Method to compare content of an dockerfile if it exist with new request dockerfile content
   */
  private static boolean isNewImage(Path path, String content) {
    var file = path.toFile();
    if (!file.exists()) {
      return true;
    } else {
      try (var fileContent = Files.lines(path)) {
        var oldContent = fileContent.collect(Collectors.joining(System.lineSeparator())).toString();
        return !oldContent.equals(content);
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
  }

  /*
   * Build dockerfile content for an specified app
   */
  private String buildDockerFileContent(String appName, int appPort) {
    return DOCKERFILE_TEMPLATE
      .replace("{{1}}", appName)
      .replace("{{2}}", "" + appPort)
      .replace("{{3}}", squareHost)
      .replace("{{4}}", squarePort)
      .concat("CMD java -jar client.jar");
  }

  // This methods generate and create docker file of an application with the name : appName and which runs on the pot : appPort
  private boolean generateAndBuildDockerFile(String appName, int appPort) {
    var dockerFilePath = DOCKERFILES_DIRECTORY + "Dockerfile." + appName;
    var dockerFileContent = buildDockerFileContent(appName, appPort);

    if (isNewImage(
        Path.of(processHelper.getRootPah() + DOCKERFILES_DIRECTORY + "Dockerfile." + appName),
        dockerFileContent)) {
      var createDockerfileCommand = "echo \"" + dockerFileContent + "\" > " + dockerFilePath;
      var createdFileRes = processHelper.execWaitForCommand(createDockerfileCommand);
      return createdFileRes && buildImage(dockerFilePath, appName);
    }

    return true;
  }

  // This methods execute the command docker build to build an image
  private boolean buildImage(String imagePath, String appName) {
    var builImageCommand = "docker build -f " + imagePath + " -t " + appName + " ./";
    return processHelper.execWaitForCommand(builImageCommand);
  }

  // This method randomly generate an id which will be attached at the image name of the application to be unique
  private int generateId() {
    var id = 1 + new Random().nextInt(Integer.MAX_VALUE);
    return (!runningInstanceMap.containsKey(id)) ? id : generateId();
  }

  // This method randomly generate a port on which the application will run into the docker
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
  // This method execute the command docker run to run an image into a docker
  private int runImage(String imageName, int appPort, int servicePort) {
    var id = generateId() % 500;
    var uniqueName = imageName + "-" + id;
    var cmd = "docker run --rm -d -p " + servicePort + ":" + appPort + " --name " + uniqueName
        + " --hostname=" + uniqueName + " " + imageName;

    var output = processHelper.execOutputCommand(cmd).split(System.lineSeparator())[0];
    var checkRuningCmd = "docker inspect -f '{{.State.Running}}' " + output;
    var status = processHelper.execOutputCommand(checkRuningCmd).split(System.lineSeparator())[0];
    return status.equals("true") ? id : -1;
  }

  // This method return an ImageInfo which is the result of the docker run command
  private Optional<ImageInfo> runBuildedImage(String appName, int appPort) {
    var servicePort = generateRandomPort();
    var runnedId = runImage(appName, appPort, servicePort);

    if (runnedId == -1)
      return Optional.empty();

    var dockerInstance = appName + "-" + runnedId;
    return Optional
      .of(new ImageInfo(appName, System.currentTimeMillis(), appPort, servicePort, dockerInstance,
          runnedId));
  }

  // This method return a String which represents the time elapsed from the begin of the application start
  private String buildElapsedTime(long diff) {
    var calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(diff);
    var hoursToMinutes = calendar.get(Calendar.HOUR) * HOUR_MINUTE_VALUE;
    var minutes = hoursToMinutes + calendar.get(Calendar.MINUTE);
    return minutes + "m" + calendar.get(Calendar.SECOND) + "s";
  }

  /**
   * Allow to get the ImageInfo corresponding to the name of the docker instance
   * @param dockerInstance : String the name of the docker instance
   * @return an ImageInfo corresponding to the name of the docker instance
   */
  public ImageInfo findImageInfoByDockerInstance(String dockerInstance) {
    var imageInfo = runningInstanceMap
      .entrySet()
      .stream()
      .filter((p) -> p.getValue().getDockerInstance().equals(dockerInstance))
      .findFirst();
    return imageInfo.isEmpty() ? null : imageInfo.get().getValue();
  }

  /**
   * Allow to fill the map of running instance information
   * @param instance : ImageInfo which is the object to fill the map of running instance information
   */
  void putInstance(ImageInfo instance) {
    runningInstanceMap.put(instance.getSquareId(), instance);
  }

  /**
   * Allow to get the map of running instance information
   * @return : HashMap<Integer, ImageInfo> : the map which associate an Integer (the id of the instance) to an ImageInfo
   */
  HashMap<Integer, ImageInfo> getRunningInstanceMap() {
    return runningInstanceMap;
  }

  /**
   * Execute all the processus to run a container
   * @param appName: name of application to be deploy in docker container
   * @param port: port of application redirected form docker container to current system
   * @param defaultPort: port of application to be deploy in docker container
   * @return {@link Optional} {@link DeployResponse}: information of ran container
   */
  public Optional<DeployResponse> runContainer(String appName, int appPort) {
    try {
      var makeDockerfile = generateAndBuildDockerFile(appName, appPort);
      if (!makeDockerfile)
        return Optional.empty();

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

  /**
   * Return the list of RunningInstanceInfo which are contained in the docker by using the docker ps command
   * @return a List of RunningInstanceInfo
   */
  public Optional<List<RunningInstanceInfo>> getRunnningList() {
    // format the docker ps command to get more information about the container
    var cmd =
        "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";

    var cmdResult = processHelper.execOutputCommand(cmd);
    if (cmdResult.isBlank() || cmdResult.isEmpty()) {
      return Optional.empty();
    }

    var list = ProcessBuilderHelper
      .parseDockerPs(cmdResult, (p) -> true)
      .stream()
      .map((mapper) -> new RunningInstanceInfo(mapper.getSquareId(), mapper.getImageName(), mapper.getAppPort(), mapper.getServicePort(), mapper.getDockerInstance(), buildElapsedTime(mapper.getCreated())))//
      .collect(Collectors.toList());

    return Optional.of(list);
  }

  /**
   * Return the RunningInstanceInfo of the application the user want to stop
   * @param key : int which is the id of the instance the user want to stop
   * @return : RunningInstanceInfo which the application the user want to stop
   */
  public Optional<RunningInstanceInfo> stopApp(int key) {
    var runningInstance = runningInstanceMap.get(key);
    var cmd = "docker kill " + runningInstance.getDockerInstance();
    var cmdResult = processHelper.execWaitForCommand(cmd);
    if (!cmdResult)
      return Optional.empty();

    runningInstanceMap.remove(key);
    var diff = System.currentTimeMillis() - runningInstance.getCreated();

    return Optional
      .of(new RunningInstanceInfo(runningInstance.getSquareId(), runningInstance.getImageName(),
          runningInstance.getAppPort(), runningInstance.getServicePort(),
          runningInstance.getDockerInstance(), buildElapsedTime(diff)));
  }
}
