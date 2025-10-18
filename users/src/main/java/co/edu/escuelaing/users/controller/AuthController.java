package co.edu.escuelaing.users.controller;

import co.edu.escuelaing.users.dto.*;
import co.edu.escuelaing.users.service.CognitoAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations with AWS Cognito
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final CognitoAuthService cognitoAuthService;
    
    /**
     * Login endpoint
     * @param authRequest authentication request
     * @return authentication response with JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        log.info("POST /api/auth/login - Login attempt for user: {}", authRequest.getUsername());
        try {
            AuthResponse response = cognitoAuthService.authenticate(authRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", authRequest.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    /**
     * Register endpoint
     * @param registerRequest registration request
     * @return registration response
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        log.info("POST /api/auth/register - Registration attempt for user: {}", registerRequest.getUsername());
        try {
            String message = cognitoAuthService.register(registerRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Validate token endpoint
     * @param request token validation request
     * @return validation response
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        log.info("POST /api/auth/validate - Token validation");
        TokenValidationResponse response = cognitoAuthService.validateToken(request.getToken());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint (public)
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth");
        return ResponseEntity.ok(response);
    }
}
