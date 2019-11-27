package fr.umlv.square.mock;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import fr.umlv.square.service.AutoScaleService;
import io.quarkus.test.Mock;

@Mock
@ApplicationScoped
public class MockAutoScaleService extends AutoScaleService {

  public MockAutoScaleService(MockDockerService dockerService) {
    super(dockerService);
  }

  @Override
  public void decInstanceCounter(String appName) {}

  @Override
  public void incInstanceCounter(String appName) {}

  @Override
  public Map<String, Integer> getScalingConfig() {
    return new HashMap<String, Integer>();
  }

  @Override
  public Map<String, String> getScalingStatus() {
    return new HashMap<String, String>();
  }

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void updateScalingConfig(Map<String, Integer> request) {}



}
