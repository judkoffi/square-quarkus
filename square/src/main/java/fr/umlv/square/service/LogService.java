package fr.umlv.square.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.orm.LogTable;

/**
 * Class use as interface between database repository to have access of date store in repository
 */
@ApplicationScoped
public class LogService {
  private final LogTable databaseRepository;

  @Inject
  public LogService(LogTable databaseRepository) {
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

  /**
   * Convert minutes into milliseconds
   * 
   * @param minutes : the minutes to convert
   * @return the minutes given converted into milliseconds
   */
  private static long convertMinuteToMillisecond(long minutes) {
    return TimeUnit.MINUTES.toMillis(minutes);
  }

  private static Timestamp convertStringToTimestamp(String strDate) {
    try {
      var formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      var date = formatter.parse(strDate);
      var timeStampDate = new Timestamp(date.getTime());
      return timeStampDate;
    } catch (ParseException e) {
      System.out.println("Exception :" + e);
      return null;
    }
  }

  public List<LogEntity> getLogsFiltedByTime(String timestamp) {
    var timeTarget = convertMinuteToMillisecond(Long.parseLong(timestamp));
    var timeWithoutOffset = (System.currentTimeMillis() - timeTarget)
        - TimeUnit.SECONDS.toMillis(ZonedDateTime.now().getOffset().getTotalSeconds());
    var filterTimestamp = "" + new Timestamp(timeWithoutOffset);
    return databaseRepository.find("date >= ?1", convertStringToTimestamp(filterTimestamp)).list();
  }

  public List<LogEntity> getLogsFiltedById(int id) {
    return databaseRepository.find("squareId", id).list();
  }

}
