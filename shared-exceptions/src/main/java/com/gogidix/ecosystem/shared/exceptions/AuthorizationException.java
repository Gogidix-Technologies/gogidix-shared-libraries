package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authorization fails.
 * This exception should be used when an authenticated user attempts to access
 * a resource or perform an action they don't have permission for.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class AuthorizationException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "AUTHORIZATION_FAILED";
    
    /**
     * Constructs a new AuthorizationException with a message.
     * Uses default error code and HTTP 403 Forbidden status.
     * 
     * @param message The error message
     */
    public AuthorizationException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Constructs a new AuthorizationException with a message and cause.
     * Uses default error code and HTTP 403 Forbidden status.
     * 
     * @param message The error message
     * @param cause The cause of this exception
     */
    public AuthorizationException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.FORBIDDEN, cause);
    }
    
    /**
     * Constructs a new AuthorizationException with error code and message.
     * Uses HTTP 403 Forbidden status.
     * 
     * @param errorCode Specific error code for this authorization exception
     * @param message The error message
     */
    public AuthorizationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Constructs a new AuthorizationException with error code, message, and cause.
     * Uses HTTP 403 Forbidden status.
     * 
     * @param errorCode Specific error code for this authorization exception
     * @param message The error message
     * @param cause The cause of this exception
     */
    public AuthorizationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.FORBIDDEN, cause);
    }
    
    /**
     * Common authorization error codes as constants.
     */
    public static class ErrorCodes {
        public static final String INSUFFICIENT_PRIVILEGES = "INSUFFICIENT_PRIVILEGES";
        public static final String ROLE_REQUIRED = "ROLE_REQUIRED";
        public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
        public static final String RESOURCE_ACCESS_DENIED = "RESOURCE_ACCESS_DENIED";
        public static final String OPERATION_NOT_ALLOWED = "OPERATION_NOT_ALLOWED";
        public static final String DOMAIN_ACCESS_DENIED = "DOMAIN_ACCESS_DENIED";
        public static final String IP_BLOCKED = "IP_BLOCKED";
        public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
        
        private ErrorCodes() {
            // Prevent instantiation
        }
    }
    
    /**
     * Factory method for insufficient privileges exception.
     * 
     * @return AuthorizationException for insufficient privileges
     */
    public static AuthorizationException insufficientPrivileges() {
        return new AuthorizationException(ErrorCodes.INSUFFICIENT_PRIVILEGES, 
            "You do not have sufficient privileges to perform this action");
    }
    
    /**
     * Factory method for role required exception.
     * 
     * @param requiredRole The role that is required
     * @return AuthorizationException for missing required role
     */
    public static AuthorizationException roleRequired(String requiredRole) {
        return new AuthorizationException(ErrorCodes.ROLE_REQUIRED, 
            String.format("Role '%s' is required to perform this action", requiredRole));
    }
    
    /**
     * Factory method for permission denied exception.
     * 
     * @param permission The permission that was denied
     * @return AuthorizationException for denied permission
     */
    public static AuthorizationException permissionDenied(String permission) {
        return new AuthorizationException(ErrorCodes.PERMISSION_DENIED, 
            String.format("Permission '%s' is required", permission));
    }
    
    /**
     * Factory method for resource access denied exception.
     * 
     * @param resourceType Type of resource
     * @param resourceId ID of the resource
     * @return AuthorizationException for denied resource access
     */
    public static AuthorizationException resourceAccessDenied(String resourceType, String resourceId) {
        return new AuthorizationException(ErrorCodes.RESOURCE_ACCESS_DENIED, 
            String.format("Access denied to %s with ID: %s", resourceType, resourceId));
    }
    
    /**
     * Factory method for rate limit exceeded exception.
     * 
     * @return AuthorizationException for rate limit exceeded
     */
    public static AuthorizationException rateLimitExceeded() {
        return new AuthorizationException(ErrorCodes.RATE_LIMIT_EXCEEDED, 
            "Rate limit exceeded. Please try again later");
    }
}