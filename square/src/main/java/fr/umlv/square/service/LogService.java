package fr.umlv.square.service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.orm.LogRepository;

@ApplicationScoped
public class LogService {

  @Inject
  LogRepository databaseRepository;

  public LogService() {}

  public List<LogEntity> getAllLogs() {
    return databaseRepository.listAll();
  }

  @Transactional
  public void saveLogs(List<LogEntity> logs) {
    databaseRepository.persist(logs.stream());
  }

  public List<LogEntity> findLogs(Predicate<LogEntity> predicate) {
    return databaseRepository.listAll().stream().filter(predicate).collect(Collectors.toList());
  }

}
