package fr.umlv.square.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InstanceBackUpService {
  private static String BACKUP_PATH = "../square.backup";

  private final DockerService dockerService;

  @Inject
  public InstanceBackUpService(DockerService dockerService) {
    this.dockerService = dockerService;
  }

  public void saveRunningInstance() {
    var map = dockerService.getRunningInstanceMap();
    var properties = new Properties();
    properties.putAll(map);

    try {
      properties.store(new FileOutputStream(BACKUP_PATH), null);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public void readSavedInstance() {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(BACKUP_PATH));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    var backUpMap = new HashMap<Object, Object>();

    for (String key : properties.stringPropertyNames()) {
      backUpMap.put(key, properties.get(key));
    }

    System.out.println(backUpMap);
  }
}
