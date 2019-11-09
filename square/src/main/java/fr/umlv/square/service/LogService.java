package fr.umlv.square.service;

import java.util.HashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LogService {
  private final HashMap<Integer, LogInfo> logsMap;

  @Inject
  DockerService dockerService;


  public LogService() {
    this.logsMap = new HashMap<Integer, LogInfo>();
  }

  public void saveLog(LogInfo log) {
    System.out.println("finded id:" + dockerService.findIdFromContainerId(log.getContainerId()));
  }

public void a() {
dockerService.func();
}
}
