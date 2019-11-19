package fr.umlv.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.umlv.square.service.ProcessBuilderHelper;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifecycleBean {
  private static final Logger LOGGER = LoggerFactory.getLogger("ListenerBean");

  void onStart(@Observes StartupEvent ev) {
    LOGGER.info("The application is starting...; PID: " + ProcessHandle.current().pid());
    var h = new ProcessBuilderHelper();
    System.out.println(h.execOutputCommand("docker ps"));
  }

  void onStop(@Observes ShutdownEvent ev) {
    LOGGER.info("The application is stopping...");
  }

}
