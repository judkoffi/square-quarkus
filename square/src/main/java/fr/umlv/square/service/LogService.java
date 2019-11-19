package fr.umlv.square.service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.orm.LogRepository;

/**
 * Class use as interface between database repository to have access of date store in repository
 */
@ApplicationScoped
public class LogService {
  private final LogRepository databaseRepository;

  @Inject
  public LogService(LogRepository databaseRepository) {
    this.databaseRepository = databaseRepository;
  }

  public List<LogEntity> getAllLogs() {
    return databaseRepository.listAll();
  }

  @Transactional
  public void saveLogs(List<LogEntity> entities) {
    databaseRepository.persist(entities.stream());
  }

  public List<LogEntity> findLogs(Predicate<LogEntity> predicate) {
    return databaseRepository.listAll().stream().filter(predicate).collect(Collectors.toList());
  }

}
