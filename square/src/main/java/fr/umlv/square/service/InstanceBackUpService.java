package fr.umlv.square.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import fr.umlv.square.util.ProcessBuilderHelper;

@ApplicationScoped
public class InstanceBackUpService {
  private final DockerService dockerService;

  @Inject
  public InstanceBackUpService(DockerService dockerService) {
    this.dockerService = dockerService;
  }

  public void readSavedInstance() {
    var helper = new ProcessBuilderHelper();
    var cmd =
        "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";
    var output = helper.execOutputCommand(cmd);

    ProcessBuilderHelper
      .parseDockerPs(output, (p) -> true)
      .stream()
      .forEach((imageInfo) -> dockerService.putInstance(imageInfo));
  }
}
