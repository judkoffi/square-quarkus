package fr.umlv.square.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import fr.umlv.square.model.service.ImageInfo;
import fr.umlv.square.util.ProcessBuilderHelper;

@ApplicationScoped
public class InstanceBackUpService {
  private static String BACKUP_PATH = "../square.backup";

  private final DockerService dockerService;

  @Inject
  public InstanceBackUpService(DockerService dockerService) {
    this.dockerService = dockerService;
  }

  public void saveRunningInstance() {
    var helper = new ProcessBuilderHelper();
    var cmd =
        "docker ps --format 'table {{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}'";

    var output = helper.execOutputCommand(cmd);
    var content = ProcessBuilderHelper
      .parseDockerPs(output, (p) -> true)
      .stream()
      .map((mapper) -> mapper.toBackUpString())
      .collect(Collectors.joining(System.lineSeparator()));

    try (var writer = new BufferedWriter(new FileWriter(BACKUP_PATH))) {
      writer.write(content);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static ImageInfo backUpLineToImageInfo(String line) {
    var tokens = line.split(";");
    return new ImageInfo(tokens[0], Long.parseLong(tokens[1]), Integer.parseInt(tokens[2]),
        Integer.parseInt(tokens[3]), tokens[4], Integer.parseInt(tokens[5]));
  }

  public void readSavedInstance() {
    try (var lines = Files.lines(Path.of(BACKUP_PATH))) {
      lines
        .map((line) -> backUpLineToImageInfo(line))
        .forEach((imageInfo) -> dockerService.putInstance(imageInfo));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
