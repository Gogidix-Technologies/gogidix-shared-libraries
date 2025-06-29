package com.exalt.ecosystem.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for authentication and authorization.
 * Handles JWT token creation, validation, and extraction of user information.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final long refreshTokenExpirationMs;
    
    /**
     * Constructor initializes JWT configuration.
     * 
     * @param jwtSecret The JWT secret key
     * @param jwtExpirationMs Token expiration in milliseconds
     * @param refreshTokenExpirationMs Refresh token expiration in milliseconds
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret:default-secret-key-change-in-production}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }
    
    /**
     * Generates JWT token from authentication.
     * 
     * @param authentication Spring Security authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUserDetails(userPrincipal, jwtExpirationMs);
    }
    
    /**
     * Generates JWT token from user details.
     * 
     * @param userDetails User details object
     * @return JWT token string
     */
    public String generateToken(UserDetailsImpl userDetails) {
        return generateTokenFromUserDetails(userDetails, jwtExpirationMs);
    }
    
    /**
     * Generates refresh token from user details.
     * 
     * @param userDetails User details object
     * @return Refresh token string
     */
    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return generateTokenFromUserDetails(userDetails, refreshTokenExpirationMs);
    }
    
    /**
     * Internal method to generate token from user details.
     * 
     * @param userDetails User details
     * @param expirationMs Expiration time in milliseconds
     * @return JWT token string
     */
    private String generateTokenFromUserDetails(UserDetailsImpl userDetails, long expirationMs) {
        Date expiryDate = new Date(System.currentTimeMillis() + expirationMs);
        
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        return Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .claim("username", userDetails.getUsername())
                .claim("email", userDetails.getEmail())
                .claim("authorities", authorities)
                .claim("userType", userDetails.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Gets user ID from JWT token.
     * 
     * @param token JWT token
     * @return User ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * Gets username from JWT token.
     * 
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("username", String.class);
    }
    
    /**
     * Gets email from JWT token.
     * 
     * @param token JWT token
     * @return Email address
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("email", String.class);
    }
    
    /**
     * Gets user authorities from JWT token.
     * 
     * @param token JWT token
     * @return List of authorities
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("authorities", List.class);
    }
    
    /**
     * Gets user type from JWT token.
     * 
     * @param token JWT token
     * @return User type
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("userType", String.class);
    }
    
    /**
     * Gets expiration date from JWT token.
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }
    
    /**
     * Validates JWT token.
     * 
     * @param token JWT token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
    
    /**
     * Checks if JWT token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Extracts token from Authorization header.
     * 
     * @param authHeader Authorization header value
     * @return JWT token string or null if invalid
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * Gets remaining time until token expiration.
     * 
     * @param token JWT token
     * @return Remaining time in milliseconds
     */
    public long getTimeUntilExpiration(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("Error calculating time until expiration: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Checks if token needs refresh (expires within threshold).
     * 
     * @param token JWT token
     * @param refreshThresholdMs Threshold in milliseconds
     * @return true if token needs refresh
     */
    public boolean needsRefresh(String token, long refreshThresholdMs) {
        long timeUntilExpiration = getTimeUntilExpiration(token);
        return timeUntilExpiration < refreshThresholdMs;
    }
    
    /**
     * Creates new token with extended expiration.
     * 
     * @param oldToken Existing token
     * @return New token with extended expiration
     */
    public String refreshToken(String oldToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(oldToken)
                    .getBody();
            
            Date newExpiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);
            
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .setExpiration(newExpiryDate)
                    .signWith(secretKey, SignatureAlgorithm.HS512)
                    .compact();
                    
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            throw new SecurityException("Failed to refresh token", e);
        }
    }
}