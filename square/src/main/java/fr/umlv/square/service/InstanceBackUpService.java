package fr.umlv.square.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import fr.umlv.square.util.ProcessBuilderHelper;

/**
 * This class is used to maintain a consistency between the current running docker and the hashmap
 * of the application which contains all of the docker running
 */

@ApplicationScoped
public class InstanceBackUpService {
  private final DockerService dockerService;
  private final String processBuilderPath;

  @Inject
  public InstanceBackUpService(DockerService dockerService,
      @ConfigProperty(name = "process.builder.path") String processBuilderPath) {
    this.dockerService = dockerService;
    this.processBuilderPath = processBuilderPath;
  }

  /**
   * Read the docker ps command and stored all of the instances found into the hashmap of docker
   * instances
   */
  public void readSavedInstance() {
    var helper = new ProcessBuilderHelper(processBuilderPath);
    var cmd =
        "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";
    var output = helper.execOutputCommand(cmd);

    // stored the instance into the hashmap
    ProcessBuilderHelper
      .parseDockerPs(output, p -> true, true)
      .stream()
      .forEach(dockerService::putInstance);
  }
}
