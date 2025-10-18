package co.edu.escuelaing.users.security;

import co.edu.escuelaing.users.config.CognitoProperties;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * Validates JWT tokens from AWS Cognito and sets authentication in SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenValidator jwtTokenValidator;
    private final CognitoProperties cognitoProperties;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    // Validate token
                    JWTClaimsSet claims = jwtTokenValidator.validateToken(jwt);
                    
                    // Verify issuer
                    String issuer = claims.getIssuer();
                    String expectedIssuer = "https://cognito-idp." + cognitoProperties.getRegion() 
                            + ".amazonaws.com/" + cognitoProperties.getUserPoolId();
                    
                    if (!expectedIssuer.equals(issuer)) {
                        log.warn("Invalid token issuer: {}", issuer);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // Extract username
                    String username = jwtTokenValidator.getUsernameFromClaims(claims);
                    
                    if (username != null) {
                        // Create authentication
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication in SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("Authenticated user: {}", username);
                    }
                } catch (Exception e) {
                    log.error("JWT validation failed: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from Authorization header
     * @param request HTTP request
     * @return JWT token or null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
