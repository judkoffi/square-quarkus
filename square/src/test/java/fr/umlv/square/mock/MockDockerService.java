package fr.umlv.square.mock;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import fr.umlv.square.model.response.DeployResponse;
import fr.umlv.square.service.DockerService;
import io.quarkus.test.Mock;

@Mock
@ApplicationScoped
public class MockDockerService extends DockerService {

  @Override
  public Optional<DeployResponse> runContainer(String appName, int appPort) {
    var response = new DeployResponse(1, appName, appPort, 10000, appName + "_" + appPort);
    return Optional.of(response);
  }

}
