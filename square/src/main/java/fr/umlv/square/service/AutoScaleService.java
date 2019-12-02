package fr.umlv.square.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
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
  private final ConcurrentHashMap<String, ScalingInfo> scaleMap;
  private final DockerService dockerService;
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoScaleService.class);
  private boolean isStarted;

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
    if (!isStarted)
      return;

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
        LongStream.range(0, i).forEach(action -> dockerService.runContainer(appName, appPort));
      } else if (diff > 0) {
        LongStream.range(0, diff).forEach(action ->
        {
          var idToStop = dockerService.findFirstInstanceByAppNamePort(appName, appPort);
          if (idToStop != -1) {
            dockerService.stopApp(idToStop);
          }
        });
      }
    });
  }

  /**
   * Start auto scaling
   */
  public void startScaling() {
    LOGGER.info("restart start auto scale");
    if (!isStarted)
      isStarted = true;
  }

  /**
   * Stop auto scaling
   */
  public void stopScaling() {
    LOGGER.info("stop task scale");
    isStarted = false;
  }


  /**
   * Start auto scaling at start of square
   */
  public void start() {
    LOGGER.info("start auto scale");
    isStarted = true;
    executor.scheduleWithFixedDelay(this::scaleWork, 1000, SCALING_DELAY, TimeUnit.MILLISECONDS);
  }

  /**
   * Stop auto scaling at stop of square
   */
  public void stop() {
    LOGGER.info("stop auto scale");
    isStarted = false;
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
      if (v == null) {
        System.out.println("no containt " + appName);
        return null;
      }
      System.out.println("innc " + appName);
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
      if (v == null) {
        System.out.println("no containt " + appName);
        return null;
      }
      var tmp = v.runningInstanceCounter--;
      if (tmp < 0) {
        v.runningInstanceCounter = 0;
      }
      return v;
    });
  }

  /**
   * Get instances which are current running but don't have scaling config set
   */
  private Set<String> makeDiffBetweenScaleMapAndRunningMap() {
    var sources = new HashSet<String>(dockerService.getMapKeys()); // Make defencive copy
    var tmp = scaleMap.entrySet().stream().map(Entry::getKey).collect(Collectors.toSet());
    var targets = new HashSet<String>(tmp);
    sources.removeAll(targets);
    return sources;
  }

  /**
   * Get info about instances which need to start or stop
   * 
   * @return: Map which associate for each app, scaling information
   */
  public Map<String, String> getScalingStatus() {
    var map = new HashMap<String, String>();
    if (!isStarted) {
      dockerService.getMapKeys().forEach(key -> map.put(key, "need nothing"));
      return map;
    }

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
    makeDiffBetweenScaleMapAndRunningMap().forEach(key -> map.put(key, "need nothing"));
    return map;
  }

  /**
   * Get info about scaling configuration
   * 
   * @return: Map which associate for each app, scaling contract
   */
  public Map<String, Long> getScalingConfig() {
    var map = new HashMap<String, Long>();
    scaleMap.forEach((key, value) ->
    {
      map.put(key, value.scalingInstanceCounter);
    });
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
        if (value == null) {
          System.out.println("add new entry " + keyRequest);
          return new ScalingInfo(dockerService.getRunningCounterOfApp(keyRequest), newScaleCounter);
        }
        value.scalingInstanceCounter = newScaleCounter;
        return value;
      });
    });
    System.out.println("in method");
    System.out.println(scaleMap);
  }

  /**
   * Intern class use to store current running instance counter and for this instance scaling
   * configuration
   */
  private static class ScalingInfo {
    private long runningInstanceCounter;
    private long scalingInstanceCounter;

    public ScalingInfo(long runningInstanceCounter, long scalingInstanceCounter) {
      this.runningInstanceCounter = runningInstanceCounter;
      this.scalingInstanceCounter = scalingInstanceCounter;
    }
  }

}
