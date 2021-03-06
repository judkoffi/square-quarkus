package fr.umlv.square.service;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import fr.umlv.square.orm.LogEntity;
import fr.umlv.square.orm.LogTable;
import fr.umlv.square.util.Helper;

/**
 * Class use as an interface between database repository and square application to have access of
 * date store in repository
 */

@ApplicationScoped
public class LogService {
  private final LogTable databaseRepository;

  @Inject
  public LogService(LogTable databaseRepository) {
    this.databaseRepository = databaseRepository;
  }

  @Transactional // this methods write into database
  public void saveLogs(List<LogEntity> entities) {
    databaseRepository.persist(entities.stream());
  }

  /**
   * Allow to get all the logs stored into the database
   * 
   * @return : List of LogEntity which are all the logs stored into the database
   */
  public List<LogEntity> getAllLogs() {
    return databaseRepository.listAll();
  }

  /**
   * Allow to get logs of the database which have been send from timestamp minutes
   * 
   * @param timestamp : String : the number of minutes the user want the logs
   * @return List of LogEntity filtered
   */
  public List<LogEntity> getLogsFilteredByTime(String timestamp) {
    var timeTarget = convertMinuteToMillisecond(Long.parseLong(timestamp));
    var timeWithoutOffset = (System.currentTimeMillis() - timeTarget)
        - (TimeUnit.SECONDS.toMillis(ZonedDateTime.now().getOffset().getTotalSeconds()));
    var filterTimestamp = "" + new Timestamp(timeWithoutOffset);
    return databaseRepository
      .find("date >= ?1",
          Helper.convertStringToTimestamp(filterTimestamp, "yyyy-MM-dd HH:mm:ss.SSS"))
      .list();
  }

  /**
   * Allow to get lofs of the database which the application have the id given
   * 
   * @param id : int : the id of the application the user want the logs
   * @return List of LogEntity filtered
   */
  public List<LogEntity> getLogsFilteredById(int id) {
    return databaseRepository.find("squareId", id).list();
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
}
