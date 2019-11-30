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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class help to manage auto scaling work
 */
@ApplicationScoped
public class AutoScaleService {
  private static final int SCALING_DELAY = 20000;
  private final ScheduledExecutorService executor;
  private final ConcurrentHashMap<String, ScalingCounter> scaleMap;
  private final DockerService dockerService;
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoScaleService.class);

  @Inject
  public AutoScaleService(DockerService dockerService) {
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.scaleMap = new ConcurrentHashMap<>();
    this.dockerService = dockerService;
  }

  /**
   * Method which check each SCALING_DELAY if scaling contract is assured
   */
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

  /**
   * Start auto scaling
   */
  public void start() {
    LOGGER.info("start auto scale");
    executor.scheduleWithFixedDelay(this::scaleWork, 1000, SCALING_DELAY, TimeUnit.MILLISECONDS);
  }

  /**
   * Stop auto scaling
   */
  public void stop() {
    LOGGER.info("stop auto scale");
    executor.shutdownNow();
  }

  /**
   * Increment current running instance counter
   * 
   * @param appName: app which need to increment's instance counter
   */
  public void incInstanceCounter(String appName) {
    scaleMap.compute(appName, (k, v) ->
    {
      if (v == null)
        return new ScalingCounter(1, 1);
      v.runningInstanceCounter++;
      return v;
    });
  }

  /**
   * Decrement current running instance counter
   * 
   * @param appName: app which need to decrement's instance counter
   */
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

  /**
   * Get info about instances which need to start or stop
   * 
   * @return: Map which associate for each app, scaling information
   */
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

  /**
   * Get info about scaling configuration
   * 
   * @return: Map which associate for each app, scaling contract
   */
  public Map<String, Integer> getScalingConfig() {
    var map = new HashMap<String, Integer>();
    scaleMap.forEach((key, value) -> map.put(key, value.scalingInstanceCounter));
    return map;
  }

  /**
   * Update scaling contract
   * 
   * @param request: new scaling configuration
   */
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

  /**
   * Intern class use to store current running instance counter and for this instance scaling
   * configuration
   */
  private static class ScalingCounter {
    private int runningInstanceCounter;
    private int scalingInstanceCounter;

    public ScalingCounter(int runningInstanceCounter, int scalingInstanceCounter) {
      this.runningInstanceCounter = runningInstanceCounter;
      this.scalingInstanceCounter = scalingInstanceCounter;
    }
  }
}
