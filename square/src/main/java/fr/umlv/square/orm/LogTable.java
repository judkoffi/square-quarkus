package fr.umlv.square.orm;

import javax.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * Class which represent database table, it use to get all tuple, or filtered tuples
 */
@ApplicationScoped
public class LogTable implements PanacheRepository<LogEntity> {
  
}
