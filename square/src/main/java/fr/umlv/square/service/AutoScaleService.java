package fr.umlv.square.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AutoScaleService {

  private static final int SCALING_DELAY = 20000;
  private final ScheduledExecutorService executor;
  private final ConcurrentHashMap<String, ScalingCounter> scaleMap;
  private final DockerService dockerService;

  @Inject
  public AutoScaleService(DockerService dockerService) {
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.scaleMap = new ConcurrentHashMap<>();
    this.dockerService = dockerService;
  }

  private void scaleWork() {
    scaleMap.forEach((key, value) ->
    {
      /*
       * ex: runCount = 1, scaleCount = 3 -> 1 - 3 = -2 --> need to start 2 instances ex: runCount =
       * 3, scaleCount = 2 -> 3 - 2 = 1 --> need to stop 1 instance
       */
      var diff = value.runningInstanceCounter - value.scalingInstanceCounter;
      var tokens = key.split(":");
      var appName = tokens[0];
      var appPort = Integer.parseInt(tokens[1]);

      if (diff < 0) {
        var i = Math.abs(diff);
        IntStream.range(0, i).forEach(action -> dockerService.runContainer(appName, appPort));
      } else if (diff > 0) {
        for (var i = 0; i < diff; i++) {
          var idToStop = dockerService.findFirstInstanceByAppNamePort(appName, appPort);
          if (idToStop != -1) {
            dockerService.stopApp(idToStop);
          }
        }
      }
    });
  }

  public void start() {
    System.out.println("start auto scale");
    executor.scheduleWithFixedDelay(this::scaleWork, 1000, SCALING_DELAY, TimeUnit.MILLISECONDS);
  }

  public void stop() {
    System.out.println("stop auto scale");
    executor.shutdownNow();
  }

  public void incInstanceCounter(String appName) {
    scaleMap.compute(appName, (k, v) ->
    {
      if (v == null)
        return new ScalingCounter(1, 1);
      v.runningInstanceCounter++;
      return v;
    });
  }

  public void decInstanceCounter(String appName) {
    scaleMap.compute(appName, (k, v) ->
    {
      if (v == null)
        return new ScalingCounter(0, 0);
      var tmp = v.runningInstanceCounter--;
      if (tmp < 0) {
        v.runningInstanceCounter = 0;
      }
      return v;
    });
  }

  public Map<String, String> getScalingStatus() {
    var map = new HashMap<String, String>();
    scaleMap.forEach((key, value) ->
    {
      var diff = value.runningInstanceCounter - value.scalingInstanceCounter;
      if (diff < 0) {
        var i = Math.abs(diff);
        map.put(key, "need to start " + i + " instance(s)");
      } else if (diff > 0) {
        var i = diff;
        map.put(key, "need to stop " + i + " instance(s)");
      } else {
        map.put(key, "need nothing");
      }
    });
    return map;
  }

  public Map<String, Integer> getScalingConfig() {
    var map = new HashMap<String, Integer>();
    scaleMap.forEach((key, value) -> map.put(key, value.scalingInstanceCounter));
    return map;
  }

  public void updateScalingConfig(Map<String, Integer> request) {
    request.forEach((keyRequest, newScaleCounter) ->
    {
      scaleMap.compute(keyRequest, (key, value) ->
      {
        if (value == null)
          return new ScalingCounter(0, newScaleCounter);
        value.scalingInstanceCounter = newScaleCounter;
        return value;
      });
    });
  }

  private static class ScalingCounter {
    private int runningInstanceCounter;
    private int scalingInstanceCounter;

    public ScalingCounter(int runningInstanceCounter, int scalingInstanceCounter) {
      this.runningInstanceCounter = runningInstanceCounter;
      this.scalingInstanceCounter = scalingInstanceCounter;
    }
  }
}
