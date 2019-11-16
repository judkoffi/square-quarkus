package fr.umlv.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifecycleBean {
  private static final Logger LOGGER = LoggerFactory.getLogger("ListenerBean");

  void onStart(@Observes StartupEvent ev) {
    LOGGER.info("The application is starting...; PID: " + ProcessHandle.current().pid());
  }

  void onStop(@Observes ShutdownEvent ev) {
    LOGGER.info("The application is stopping...");
  }

}
