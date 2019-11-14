package fr.umlv.square.orm;

import javax.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class LogRepository implements PanacheRepository<LogEntity> {

}
