package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;


import fr.umlv.square.model.response.DeployResponse;
import fr.umlv.square.model.response.RunningInstanceInfo;

@ApplicationScoped // One DockerService instance for the whole application
public class DockerService {
  private final static String DOCKERFILE_TEMPLATE; // DOckerfile use as template for all images
  private final static String DOCKERFILES_DIRECTORY;
  private final static String APPS_DIRECTORY;
  private final static int MIN_PORT_NUMBER;
  private final static int MAX_PORT_NUMBER;
  private final ProcessBuilder processBuilder;
  private final HashMap<Integer, ImageInfo> runningInstanceMap;

  static {
    DOCKERFILE_TEMPLATE = "FROM hirokimatsumoto/alpine-openjdk-11\n" + "WORKDIR /app\n"
        + "COPY {{1}}.jar /app/app.jar\n" + "RUN chmod 775 /app\n" + "EXPOSE {{2}}\n"
        + "CMD java -jar app.jar";
    DOCKERFILES_DIRECTORY = "docker-images/";
    APPS_DIRECTORY = "apps/";
    MIN_PORT_NUMBER = 2000;
    MAX_PORT_NUMBER = 65535;
  }

  public DockerService() {
    // default pwd = square/target
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
    var imageFile = DOCKERFILE_TEMPLATE.replace("{{1}}", appName).replace("{{2}}", "" + port);
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
    var builImageCommand = "docker build -f " + imagePath + " -t " + appName + " " + APPS_DIRECTORY;
    try {
      var exitValue = processBuilder.command("bash", "-c", builImageCommand).start().waitFor();
      return (exitValue == 0);
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
  }

  private int generateId() {
    var id = 1 + new Random().nextInt(Integer.MAX_VALUE % 500);
    if (!runningInstanceMap.containsKey(id))
      return id;
    return generateId();
  }

  private int generatePort(int... notAvailablePort) {
    var port = MIN_PORT_NUMBER + new Random().nextInt(MAX_PORT_NUMBER);

    for (var i = 0; i < notAvailablePort.length; i++) {
      if (port == notAvailablePort[i])
        return generatePort(notAvailablePort);
    }

    var isUsedPort = runningInstanceMap
      .entrySet()
      .stream()
      .anyMatch((entry) -> entry.getValue().squareId == port);

    return !isUsedPort ? port : generatePort(notAvailablePort);
  }

  private int runImage(String imageName, int appPort, int servicePort) {
    var id = generateId();
    var uniqueName = imageName + "-" + id;

    var cmd =
        "docker run -p " + servicePort + ":" + appPort + " --name " + uniqueName + " " + imageName;

    try {
      var runProcess = processBuilder.command("bash", "-c", cmd).start().isAlive();
      return runProcess ? id : -1;
    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  private Optional<ImageInfo> getRunningImageInfo(String runningName, int runningId,
      int servicePort) {
    //var cmd = "docker ps";
	  var cmd = "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();

      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();
      var consoleOutput = getOutputOfCommand(outputStream);
      System.out.println(consoleOutput);
      if (exitValue != 0 || !consoleOutput.contains(runningName)) {
        return Optional.empty();
      }
      var info = parseDockerPs(consoleOutput, (p) -> p.contains(runningName)).get(0);
      return Optional.of(info);
    } catch (IOException | InterruptedException e) {
      return Optional.empty();
    }
  }

  private boolean generateAndBuildDockerFile(String appName, int appPort) {
    var dockerImagePath = createDockerFile(appName, appPort);
    return dockerImagePath == null ? false : buildImage(dockerImagePath, appName);
  }

  // TODO: Implement better check of already builded image
  private boolean isAlreadyImage(String appName) {
    var cmd = "docker images " + appName;

    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();
      var consoleOutput = getOutputOfCommand(outputStream);
      return exitValue == 0 && consoleOutput.split(System.getProperty("line.separator")).length > 2;
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
  }

  private Optional<ImageInfo> runBuiledImage(String appName, int appPort) {
    var externalPort = generatePort(appPort);
    var ranImageId = runImage(appName, appPort, externalPort);
    if (ranImageId == -1)
      return Optional.empty();
    try {
      Thread.sleep(3000); // TODO: Have a better implementation
    } catch (InterruptedException e) {
      throw new AssertionError();
    }
    return getRunningImageInfo(appName, ranImageId, externalPort);
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
        System.out.println("already image");
        var makeDockerfile = generateAndBuildDockerFile(appName, appPort);
        if (!makeDockerfile)
          return Optional.empty();
      }

      var imageInfo = runBuiledImage(appName, appPort);
      if (imageInfo.isEmpty())
        return Optional.empty();

      var info = imageInfo.get();
      runningInstanceMap.put(info.squareId, info);

      return Optional
        .of(new DeployResponse(info.squareId, appName, appPort, info.servicePort,
            info.dockerInstance));

    } catch (AssertionError e) {
      return Optional.empty();
    }
  }

  private void someFunction(String appName, int appPort, int servicePort) {
    CompletableFuture<Optional<DeployResponse>> combinedFuture = CompletableFuture
      .supplyAsync(() -> runImage(appName, appPort, servicePort))
      .thenCombine(CompletableFuture.supplyAsync(() -> ""), (squareId, a) -> (squareId == -1) ? Optional.empty() : getRunningImageInfo(appName, squareId, servicePort)).thenCombine(CompletableFuture.supplyAsync(() -> ""), (optional, a) ->
      {
        if (optional.isEmpty())
          return Optional.empty();

        var info = (ImageInfo) optional.get();
        runningInstanceMap.put(info.squareId, info);

        System.out.println(info);
        var response =
            new DeployResponse(info.squareId, appName, appPort, servicePort, info.dockerInstance);

        return Optional.of(response);
      });
    // return combinedFuture.get();
  }

  private List<ImageInfo> parseDockerPs(String psOutput, Predicate<String> predicate) {
    var regex = "([A-Z\\s]+?)($|\\s{2,})";
    var lines = psOutput.trim().split("\n");
    // ((tokens[4]).split(":")[1]).split("->")[0]

    return Arrays
      .stream(lines)
      .skip(1)
      .filter(predicate)
      .map((elt) -> elt.split(regex))
      .map((tokens) ->
      {
        var id = Integer.parseInt(tokens[6].split("-")[1]);
        var ports = tokens[5].split(":");
        
        System.out.println("Ports " + Arrays.deepToString(ports));
        var servicePort = Integer.parseInt(ports[1].split("->")[0]);
        var appPort = Integer.parseInt(((ports[1].split("->")[0]).split("/"))[0]);
        
        var timestamp = (tokens[3].trim());        
        var time = timestamp.split(" ");
        var minutes = time[1].split(":")[1];
        var secondes = time[1].split(":")[2];
        var elapsedTime = minutes + "m" + secondes + "s";
        		
        // TODO BETTER IMPLEMENT + DO CALCULUS
        
        return new ImageInfo(tokens[0], tokens[1], tokens[2], elapsedTime.toString(), tokens[4], appPort,
            servicePort, tokens[6], id);
      })
      .collect(Collectors.toList());
  }


  public Optional<List<RunningInstanceInfo>> getRunnningList() {
	  System.out.println(runningInstanceMap);
    //var cmd = "docker ps";
	var cmd = "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";
    try {
      var process = processBuilder.command("bash", "-c", cmd).start();
      var outputStream = process.getInputStream();
      var exitValue = process.waitFor();

      if (exitValue != 0)
        return Optional.empty();

      var consoleOutput = getOutputOfCommand(outputStream);
      List<ImageInfo> infoList = parseDockerPs(consoleOutput, (__) -> true);
      return Optional
        .of(infoList.stream().map((mapper) -> new RunningInstanceInfo(mapper.squareId, mapper.imageName, mapper.appPort, mapper.servicePort, mapper.dockerInstance, mapper.created)).collect(Collectors.toList()));
    } catch (IOException | InterruptedException e) {
      return Optional.empty();
    }
  }
}
