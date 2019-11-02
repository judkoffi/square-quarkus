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
  public Optional<DeployResponse> runContainer(String appName, int port, int defaultPort) {

    var response = new DeployResponse(1, appName, port, 10000, appName + "_" + port);

    return Optional.of(response);
  }

}
