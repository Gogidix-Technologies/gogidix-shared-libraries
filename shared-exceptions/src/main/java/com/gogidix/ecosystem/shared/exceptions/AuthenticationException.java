package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 * This exception should be used when user authentication cannot be verified,
 * such as invalid credentials, expired tokens, or missing authentication.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class AuthenticationException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "AUTHENTICATION_FAILED";
    
    /**
     * Constructs a new AuthenticationException with a message.
     * Uses default error code and HTTP 401 Unauthorized status.
     * 
     * @param message The error message
     */
    public AuthenticationException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Constructs a new AuthenticationException with a message and cause.
     * Uses default error code and HTTP 401 Unauthorized status.
     * 
     * @param message The error message
     * @param cause The cause of this exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.UNAUTHORIZED, cause);
    }
    
    /**
     * Constructs a new AuthenticationException with error code and message.
     * Uses HTTP 401 Unauthorized status.
     * 
     * @param errorCode Specific error code for this authentication exception
     * @param message The error message
     */
    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Constructs a new AuthenticationException with error code, message, and cause.
     * Uses HTTP 401 Unauthorized status.
     * 
     * @param errorCode Specific error code for this authentication exception
     * @param message The error message
     * @param cause The cause of this exception
     */
    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED, cause);
    }
    
    /**
     * Common authentication error codes as constants.
     */
    public static class ErrorCodes {
        public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
        public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
        public static final String TOKEN_INVALID = "TOKEN_INVALID";
        public static final String TOKEN_MISSING = "TOKEN_MISSING";
        public static final String SESSION_EXPIRED = "SESSION_EXPIRED";
        public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
        public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
        public static final String TWO_FACTOR_REQUIRED = "TWO_FACTOR_REQUIRED";
        public static final String TWO_FACTOR_FAILED = "TWO_FACTOR_FAILED";
        
        private ErrorCodes() {
            // Prevent instantiation
        }
    }
    
    /**
     * Factory method for invalid credentials exception.
     * 
     * @return AuthenticationException for invalid credentials
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(ErrorCodes.INVALID_CREDENTIALS, 
            "Invalid username or password");
    }
    
    /**
     * Factory method for expired token exception.
     * 
     * @return AuthenticationException for expired token
     */
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException(ErrorCodes.TOKEN_EXPIRED, 
            "Authentication token has expired");
    }
    
    /**
     * Factory method for invalid token exception.
     * 
     * @return AuthenticationException for invalid token
     */
    public static AuthenticationException tokenInvalid() {
        return new AuthenticationException(ErrorCodes.TOKEN_INVALID, 
            "Authentication token is invalid");
    }
    
    /**
     * Factory method for missing token exception.
     * 
     * @return AuthenticationException for missing token
     */
    public static AuthenticationException tokenMissing() {
        return new AuthenticationException(ErrorCodes.TOKEN_MISSING, 
            "Authentication token is required");
    }
}