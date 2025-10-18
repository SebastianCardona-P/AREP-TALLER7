package co.edu.escuelaing.streams.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuration class for MongoDB
 */
@Configuration
@EnableMongoRepositories(basePackages = "co.edu.escuelaing.streams.repository")
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB configuration is handled by application.yml
    // This class enables auditing and repository scanning
}
