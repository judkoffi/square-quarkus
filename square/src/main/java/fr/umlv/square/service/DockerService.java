package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
  private final ProcessBuilder processBuilder;
  private final HashMap<Integer, ImageInfo> runningInstanceMap;
  private final String squareHost;
  private final String squarePort;

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
    this.processBuilder = new ProcessBuilder().directory(new File("../../"));
    this.runningInstanceMap = new HashMap<Integer, ImageInfo>();
  }

  /* Helper method use to display output after run command on terminal */
  private static String getOutputOfCommand(InputStream outputStream) throws IOException {
    var reader = new BufferedReader(new InputStreamReader(outputStream));
    var builder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
      builder.append(System.getProperty("line.separator"));
    }
    return builder.toString();
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
    try {
      var exitValue =
          processBuilder.command("bash", "-c", createDockerfileCommand).start().waitFor();
      return (exitValue == 0) ? imagePath : null;
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
  }

  private boolean buildImage(String imagePath, String appName) {
    var builImageCommand = "docker build -f " + imagePath + " -t " + appName + " ./";
    try {
      var exitValue = processBuilder.command("bash", "-c", builImageCommand).start().waitFor();
      return (exitValue == 0);
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
  }

  private int generateId() {
    var id = 1 + new Random().nextInt(Integer.MAX_VALUE);
    if (!runningInstanceMap.containsKey(id))
      return id;
    return generateId();
  }

  public static int generateRandomPort() {
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

  private PairPidId runImage(String imageName, int appPort, int servicePort) {
    // TODO: Remove modulo
    var id = generateId() % 500;
    var uniqueName = imageName + "-" + id;

    var cmd = "docker run --rm -p " + servicePort + ":" + appPort + " --name " + uniqueName
        + " --hostname=" + uniqueName + " " + imageName;

    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var runProcess = process.isAlive();
      return runProcess ? new PairPidId(id, process.pid()) : null;
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private boolean generateAndBuildDockerFile(String appName, int appPort) {
    var dockerImagePath = createDockerFile(appName, appPort);
    return dockerImagePath == null ? false : buildImage(dockerImagePath, appName);
  }

  private boolean isAlreadyImage(String appName) {
    var cmd = "docker image inspect " + appName;
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();
      var consoleOutput = getOutputOfCommand(outputStream);
      return exitValue == 0 && !consoleOutput.contains("[]");
    } catch (IOException | InterruptedException e) {
      throw new AssertionError(e);
    }
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
      if (imageInfo.isEmpty()) {
        System.err.println("run empty");
        return Optional.empty();
      }

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

  /*
   * TODO: FIX Hours adding to minutes
   */
  private String buildElapsedTime(long diff) {
    var calendar = Calendar.getInstance();
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
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();

      if (exitValue != 0)
        return Optional.empty();

      var consoleOutput = getOutputOfCommand(outputStream);
      List<ImageInfo> infoList = parseDockerPs(consoleOutput, (p) -> true);

      return Optional
        .of(infoList.stream().map((mapper) -> new RunningInstanceInfo(mapper.squareId, mapper.imageName, mapper.appPort, mapper.servicePort, mapper.dockerInstance, mapper.created)).collect(Collectors.toList()));
    } catch (IOException | InterruptedException e) {
      return Optional.empty();
    }
  }

  /*
   * 
   * TODO: Fix elapsed-time field bug Actual "elapsed-time": "2019-11-18 05:40:41.459" but must be
   * XmYs Improve implement of buildElapsedTime to use this method to compute elasped time
   */
  public Optional<RunningInstanceInfo> stopApp(int key) {
    var runningInstance = runningInstanceMap.get(key);
    var cmd = "docker kill " + runningInstance.dockerInstance;

    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();

      if (exitValue != 0)
        return Optional.empty();

      var consoleOutput = getOutputOfCommand(outputStream);
      System.out.println(consoleOutput);

      runningInstanceMap.remove(key);

      return Optional
        .of(new RunningInstanceInfo(runningInstance.squareId, runningInstance.imageName,
            runningInstance.appPort, runningInstance.servicePort, runningInstance.dockerInstance,
            runningInstance.created));
    } catch (IOException | InterruptedException e) {
      return Optional.empty();
    }
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

}
