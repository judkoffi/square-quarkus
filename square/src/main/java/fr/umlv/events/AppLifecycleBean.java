package fr.umlv.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.umlv.square.service.InstanceBackUpService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * Class used to execute method at the start-up and the stop of square
 *
 */
@ApplicationScoped // describe the whole square application entity
public class AppLifecycleBean {
  private final InstanceBackUpService backUpService;
  private static final Logger LOGGER = LoggerFactory.getLogger(AppLifecycleBean.class);

  @Inject
  public AppLifecycleBean(InstanceBackUpService backUpService) {
    this.backUpService = backUpService;
  }

  /**
   * This method is executed when the app starts
   * @param ev : the event when the app starts
   */
  void onStart(@Observes StartupEvent ev) {
    LOGGER.info("The application is starting... and PID: {}", ProcessHandle.current().pid());
    backUpService.readSavedInstance();
  }

  void onStop(@Observes ShutdownEvent ev) {

  }

}
