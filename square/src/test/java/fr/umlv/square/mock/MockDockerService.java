package fr.umlv.square.mock;

import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import fr.umlv.square.model.response.DeployResponse;
import fr.umlv.square.model.response.RunningInstanceInfo;
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

  @Override
  public Optional<List<RunningInstanceInfo>> getRunnningList() {
    var instance1 = new RunningInstanceInfo(1, "sortapp", 8080, 9000, "sortapp-1", "4m50s");
    var instance2 = new RunningInstanceInfo(15, "hellapi", 8080, 1000, "hellapi-15", "8m50s");
    var instance3 = new RunningInstanceInfo(238, "yep", 8080, 5010, "yep-238", "17m50s");
    var list = List.of(instance1, instance2, instance3);
    return Optional.of(list);
  }

  @Override
  public Optional<RunningInstanceInfo> stopApp(int key) {
    var runningInstance = new RunningInstanceInfo(10, "fruitapi", 8080, 1000, "fruitapi-1", "2m30");
    return Optional.of(runningInstance);
  }

}
