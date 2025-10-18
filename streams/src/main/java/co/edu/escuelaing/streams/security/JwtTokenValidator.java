package co.edu.escuelaing.streams.security;

import co.edu.escuelaing.streams.config.CognitoProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.ParseException;

/**
 * JWT Token Validator for AWS Cognito tokens
 */
@Component
@Slf4j
public class JwtTokenValidator {
    
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    
    public JwtTokenValidator(CognitoProperties cognitoProperties) {
        try {
            this.jwtProcessor = new DefaultJWTProcessor<>();
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(cognitoProperties.getJwksUrl()));
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);
        } catch (Exception e) {
            log.error("Error initializing JWT processor: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize JWT processor", e);
        }
    }
    
    public JWTClaimsSet validateToken(String token) throws ParseException, BadJOSEException, JOSEException {
        return jwtProcessor.process(token, null);
    }
    
    public String getUsernameFromClaims(JWTClaimsSet claims) {
        try {
            String username = claims.getStringClaim("cognito:username");
            if (username == null) {
                username = claims.getStringClaim("username");
            }
            if (username == null) {
                username = claims.getSubject();
            }
            return username;
        } catch (ParseException e) {
            log.error("Error extracting username from claims: {}", e.getMessage());
            return claims.getSubject();
        }
    }
}
