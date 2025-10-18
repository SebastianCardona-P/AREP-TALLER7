package co.edu.escuelaing.streams.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AWS Cognito configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "aws.cognito")
@Data
public class CognitoProperties {
    
    private String region;
    private String userPoolId;
    private String jwksUrl;
}
