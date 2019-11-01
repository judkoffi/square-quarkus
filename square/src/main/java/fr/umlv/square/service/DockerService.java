package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped // One DockerService instance for the whole application
public class DockerService {
  private final static String DOCKERFILE_TEMPLATE; // DOckerfile use as template for all images
  private final static String DOCKERFILES_DIRECTORY;
  private final static String APPS_DIRECTORY;
  private final ProcessBuilder processBuilder;

  static {
    DOCKERFILE_TEMPLATE = "FROM hirokimatsumoto/alpine-openjdk-11\n" + "WORKDIR /app\n"
        + "COPY {{1}}.jar /app/app.jar\n" + "RUN chmod 775 /app\n" + "EXPOSE {{2}}\n"
        + "CMD java -jar app.jar";
    DOCKERFILES_DIRECTORY = "docker-images/";
    APPS_DIRECTORY = "apps/";
  }

  public DockerService() {
    // default pwd = square/target
    this.processBuilder = new ProcessBuilder().directory(new File("../../"));
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

  private boolean runImage(String imageName, int port, int defaultPort) {
    var customName = imageName + "_" + port;
    var runDockerCommand =
        "docker run -p " + port + ":" + defaultPort + " --name " + customName + " " + imageName;

    try {
      var timeout = 1000; // Time to wait to be sure that docker is running
      processBuilder.command("bash", "-c", runDockerCommand).start().waitFor(timeout,
          TimeUnit.MILLISECONDS);

      // Make docker ps and check if image is running
      var process = processBuilder.command("bash", "-c", "docker ps").start();

      var outputStream = process.getInputStream();
      var consoleOutput = getOutputOfCommand(outputStream);
      System.out.println(consoleOutput);
      return consoleOutput.contains(customName);
    } catch (IOException | InterruptedException e) {
      throw new AssertionError();
    }
  }

  /**
   * @param appName: name of application to be deploy in docker container
   * @param port: port of application redirected form docker container to current system
   * @param defaultPort: port of application to be deploy in docker container
   * 
   * @return true in container was set up or false if not
   */
  public boolean runContainer(String appName, int port, int defaultPort) {
    try {
      var dockerImagePath = createDockerFile(appName, port);
      if (dockerImagePath == null)
        return false;

      var isSuccededImageBuild = buildImage(dockerImagePath, appName);
      if (!isSuccededImageBuild)
        return false;

      var isImageRan = runImage(appName, port, defaultPort);
      if (!isImageRan)
        return false;

    } catch (AssertionError e) {
      return false;
    }
    return true;
  }
}
