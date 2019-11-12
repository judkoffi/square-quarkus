package fr.umlv.square.service;

import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogService {
  private final HashMap<Integer, LogInfo> logsMap;

  public LogService() {
    this.logsMap = new HashMap<Integer, LogInfo>();
  }

  public void saveLog(List<LogInfo> parsedLogs) {
    parsedLogs.stream().forEach((log) -> logsMap.put(log.getSquareId(), log));
  }

}
