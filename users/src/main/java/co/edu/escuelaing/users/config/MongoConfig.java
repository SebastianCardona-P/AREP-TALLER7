package co.edu.escuelaing.users.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuration class for MongoDB
 */
@Configuration
@EnableMongoRepositories(basePackages = "co.edu.escuelaing.users.repository")
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB configuration is handled by application.yml
    // This class enables auditing and repository scanning
}
