package co.edu.escuelaing.users.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.nimbusds.jwt.JWTClaimsSet;

import co.edu.escuelaing.users.config.CognitoProperties;
import co.edu.escuelaing.users.dto.AuthRequest;
import co.edu.escuelaing.users.dto.AuthResponse;
import co.edu.escuelaing.users.dto.RegisterRequest;
import co.edu.escuelaing.users.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import co.edu.escuelaing.users.dto.TokenValidationResponse;
import co.edu.escuelaing.users.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for AWS Cognito authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CognitoAuthService {
    
    private final CognitoProperties cognitoProperties;
    private final JwtTokenValidator jwtTokenValidator;
    
    /**
     * Get Cognito client configured for the region
     */
    private CognitoIdentityProviderClient getCognitoClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(cognitoProperties.getRegion()))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
    }
    
    /**
     * Authenticate user with Cognito using InitiateAuth API
     * @param authRequest authentication request
     * @return authentication response with tokens
     */
    public AuthResponse authenticate(AuthRequest authRequest) {
        try (CognitoIdentityProviderClient cognitoClient = getCognitoClient()) {
            
            // Calculate SECRET_HASH
            String secretHash = calculateSecretHash(authRequest.getUsername());
            
            // Prepare auth parameters
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", authRequest.getUsername());
            authParameters.put("PASSWORD", authRequest.getPassword());
            authParameters.put("SECRET_HASH", secretHash);
            
            // Build InitiateAuth request
            InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(cognitoProperties.getClientId())
                    .authParameters(authParameters)
                    .build();
            
            log.info("Calling Cognito InitiateAuth for user: {}", authRequest.getUsername());
            
            // Call Cognito
            InitiateAuthResponse response = cognitoClient.initiateAuth(initiateAuthRequest);
            
            log.info("Cognito response received");
            
            // Check if there's a challenge (e.g., NEW_PASSWORD_REQUIRED)
            if (response.challengeName() != null) {
                log.warn("Authentication challenge required: {}", response.challengeName());
                throw new RuntimeException("Authentication challenge required: " + response.challengeName() + 
                        ". Please complete the challenge (e.g., change temporary password) using the AWS Console or Hosted UI.");
            }
            
            // Get authentication result
            AuthenticationResultType authResult = response.authenticationResult();
            
            if (authResult != null && authResult.idToken() != null) {
                log.info("Authentication successful, extracting tokens...");
                
                // Parse ID token to get user info
                String idToken = authResult.idToken();
                JWTClaimsSet claims = jwtTokenValidator.validateToken(idToken);
                
                return AuthResponse.builder()
                        .accessToken(authResult.accessToken())
                        .idToken(idToken)
                        .refreshToken(authResult.refreshToken())
                        .expiresIn(authResult.expiresIn())
                        .tokenType(authResult.tokenType())
                        .username(jwtTokenValidator.getUsernameFromClaims(claims))
                        .email(jwtTokenValidator.getEmailFromClaims(claims))
                        .build();
            }
            
            throw new RuntimeException("Authentication failed: No authentication result");
            
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Register new user in Cognito
     * @param registerRequest registration request
     * @return success message
     */
    public String register(RegisterRequest registerRequest) {
        try (CognitoIdentityProviderClient cognitoClient = getCognitoClient()) {
            
            // Calculate SECRET_HASH
            String secretHash = calculateSecretHash(registerRequest.getUsername());
            
            // Build user attributes
            AttributeType emailAttr = AttributeType.builder()
                    .name("email")
                    .value(registerRequest.getEmail())
                    .build();
            
            AttributeType nameAttr = AttributeType.builder()
                    .name("name")
                    .value(registerRequest.getFullName())
                    .build();
            
            // Build SignUp request
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(cognitoProperties.getClientId())
                    .username(registerRequest.getUsername())
                    .password(registerRequest.getPassword())
                    .secretHash(secretHash)
                    .userAttributes(emailAttr, nameAttr)
                    .build();
            
            log.info("Registering user: {}", registerRequest.getUsername());
            
            cognitoClient.signUp(signUpRequest);
            
            log.info("User registered successfully: {}", registerRequest.getUsername());
            return "User registered successfully. Please check your email to verify your account.";
            
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate JWT token
     * @param token JWT token
     * @return validation response
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            JWTClaimsSet claims = jwtTokenValidator.validateToken(token);
            
            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(jwtTokenValidator.getUsernameFromClaims(claims))
                    .email(jwtTokenValidator.getEmailFromClaims(claims))
                    .sub(claims.getSubject())
                    .message("Token is valid")
                    .build();
                    
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Calculate SECRET_HASH for Cognito authentication
     * @param username username
     * @return SECRET_HASH
     */
    private String calculateSecretHash(String username) {
        try {
            String message = username + cognitoProperties.getClientId();
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    cognitoProperties.getClientSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating SECRET_HASH", e);
        }
    }
}
