package co.edu.escuelaing.users.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * AWS Cognito configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "aws.cognito")
@Data
public class CognitoProperties {
    
    private String region;
    private String userPoolId;
    private String clientId;
    private String clientSecret;
    private String jwksUrl;
}
