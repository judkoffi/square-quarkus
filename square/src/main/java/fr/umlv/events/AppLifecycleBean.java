package fr.umlv.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.umlv.square.service.InstanceBackUpService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifecycleBean {
  private final InstanceBackUpService backUpService;
  private static final Logger LOGGER = LoggerFactory.getLogger(AppLifecycleBean.class);

  @Inject
  public AppLifecycleBean(InstanceBackUpService backUpService) {
    this.backUpService = backUpService;
  }

  void onStart(@Observes StartupEvent ev) {
    LOGGER.info("The application is starting... and PID: " + ProcessHandle.current().pid());
    backUpService.readSavedInstance();
  }

  void onStop(@Observes ShutdownEvent ev) {
    LOGGER.info("Create docker instance backup ....");
    backUpService.saveRunningInstance();
  }

}
