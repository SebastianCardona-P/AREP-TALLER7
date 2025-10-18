package co.edu.escuelaing.users.security;

import java.net.URL;
import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import co.edu.escuelaing.users.config.CognitoProperties;
import lombok.extern.slf4j.Slf4j;

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
    
    /**
     * Validate and parse JWT token
     * @param token JWT token
     * @return JWTClaimsSet if valid
     * @throws ParseException if token cannot be parsed
     * @throws BadJOSEException if token signature is invalid
     * @throws JOSEException if token processing fails
     */
    public JWTClaimsSet validateToken(String token) throws ParseException, BadJOSEException, JOSEException {
        return jwtProcessor.process(token, null);
    }
    
    /**
     * Extract username from token claims
     * @param claims JWT claims
     * @return username
     */
    public String getUsernameFromClaims(JWTClaimsSet claims) {
        try {
            // Cognito uses 'cognito:username' or 'username' claim
            String username = claims.getStringClaim("cognito:username");
            if (username == null) {
                username = claims.getStringClaim("username");
            }
            if (username == null) {
                username = claims.getSubject(); // fallback to 'sub' claim
            }
            return username;
        } catch (ParseException e) {
            log.error("Error extracting username from claims: {}", e.getMessage());
            return claims.getSubject(); // fallback to subject
        }
    }
    
    /**
     * Extract email from token claims
     * @param claims JWT claims
     * @return email
     */
    public String getEmailFromClaims(JWTClaimsSet claims) {
        try {
            return claims.getStringClaim("email");
        } catch (ParseException e) {
            log.error("Error extracting email from claims: {}", e.getMessage());
            return null;
        }
    }
}
