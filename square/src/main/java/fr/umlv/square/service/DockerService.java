package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import fr.umlv.square.model.response.DeployResponse;

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

  private int runImage(String imageName, int appPort, int externalPort) {
    var id = generateId();
    var uniqueName = imageName + "-" + id;

    var command =
        "docker run -p " + externalPort + ":" + appPort + " --name " + uniqueName + " " + imageName;

    try {
      var runProcess = processBuilder.command("bash", "-c", command).start().isAlive();
      return runProcess ? id : -1;
    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  private Optional<ImageInfo> getRunningImageInfo(String runningName, int runningId) {
    var psCommand = "docker ps";
    try {
      var process = processBuilder.command("bash", "-c", psCommand).start();

      try {
        var exitValue = process.waitFor();
        System.out.println("ps exit status: " + exitValue);

      } catch (InterruptedException e) {
      }

      var outputStream = process.getInputStream();
      var consoleOutput = getOutputOfCommand(outputStream);

      System.out.println(consoleOutput);
      if (!consoleOutput.contains(runningName))
        return Optional.empty();


      return buildImageInfoFromString(consoleOutput, runningName, runningId);
    } catch (IOException e) {
      return Optional.empty();
    }
  }


  private Optional<ImageInfo> buildImageInfoFromString(String consoleOutput, String name, int id) {
    var lines = consoleOutput.split("\n");
    var line = Arrays.stream(lines).skip(1).filter((p) -> p.contains(name)).findFirst().get();
    var tokens = line.split("\\s+");
    return Optional.of(new ImageInfo(name, tokens[0], tokens[13], id));
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
      var dockerImagePath = createDockerFile(appName, appPort);
      if (dockerImagePath == null)
        return Optional.empty();

      var isSuccededImageBuild = buildImage(dockerImagePath, appName);
      if (!isSuccededImageBuild)
        return Optional.empty();

      var externalPort = generatePort(appPort);
      var ranImageId = runImage(appName, appPort, externalPort);
      if (ranImageId == -1)
        return Optional.empty();

      try {
        Thread.sleep(3000); // TODO: Have a better implementation
      } catch (InterruptedException e) {
        throw new AssertionError();
      }

      var imageInfo = getRunningImageInfo(appName, ranImageId);
      if (imageInfo.isEmpty())
        return Optional.empty();

      var info = imageInfo.get();
      runningInstanceMap.put(info.squareId, info);

      System.out.println(info);
      var response =
          new DeployResponse(info.squareId, appName, appPort, externalPort, info.dockerInstance);
      return Optional.of(response);

    } catch (AssertionError e) {
      return Optional.empty();
    }
  }

  private static class ImageInfo {
    private final String name;
    private final String dockerId;
    private final int squareId;
    private final String dockerInstance;

    public ImageInfo(String name, String dockerId, String serviceDocker, int id) {
      this.name = Objects.requireNonNull(name);
      this.dockerId = Objects.requireNonNull(dockerId);
      this.dockerInstance = Objects.requireNonNull(serviceDocker);
      this.squareId = Objects.requireNonNull(id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, dockerId, squareId, dockerInstance);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ImageInfo))
        return false;

      ImageInfo imageInfo = (ImageInfo) obj;
      return imageInfo.squareId == squareId && imageInfo.name.equals(name)
          && imageInfo.dockerInstance.equals(dockerInstance) && imageInfo.dockerId.equals(dockerId);
    }

    @Override
    public String toString() {
      return "name: " + name + "; dockerID " + dockerId + "; squareId" + squareId
          + ": serviceDocker " + dockerInstance;
    }
  }
}
