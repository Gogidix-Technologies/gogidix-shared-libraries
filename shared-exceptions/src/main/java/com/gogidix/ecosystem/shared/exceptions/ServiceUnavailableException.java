package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service is temporarily unavailable.
 * This exception should be used for transient failures where retry might succeed,
 * such as service overload, maintenance mode, or temporary connectivity issues.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class ServiceUnavailableException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "SERVICE_UNAVAILABLE";
    private final String serviceName;
    private final Long retryAfterSeconds;
    
    /**
     * Constructs a new ServiceUnavailableException with a message.
     * Uses default error code and HTTP 503 Service Unavailable status.
     * 
     * @param message The error message
     */
    public ServiceUnavailableException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.SERVICE_UNAVAILABLE);
        this.serviceName = null;
        this.retryAfterSeconds = null;
    }
    
    /**
     * Constructs a new ServiceUnavailableException with a message and cause.
     * 
     * @param message The error message
     * @param cause The cause of this exception
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.SERVICE_UNAVAILABLE, cause);
        this.serviceName = null;
        this.retryAfterSeconds = null;
    }
    
    /**
     * Constructs a new ServiceUnavailableException for a specific service.
     * 
     * @param serviceName Name of the unavailable service
     * @param retryAfterSeconds Suggested retry delay in seconds
     */
    public ServiceUnavailableException(String serviceName, Long retryAfterSeconds) {
        super(DEFAULT_ERROR_CODE, 
              String.format("Service '%s' is temporarily unavailable. Please retry after %d seconds", 
                           serviceName, retryAfterSeconds), 
              HttpStatus.SERVICE_UNAVAILABLE);
        this.serviceName = serviceName;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    /**
     * Constructs a new ServiceUnavailableException with custom error code.
     * 
     * @param errorCode Specific error code
     * @param message The error message
     * @param serviceName Name of the unavailable service
     * @param retryAfterSeconds Suggested retry delay in seconds
     */
    public ServiceUnavailableException(String errorCode, String message, String serviceName, Long retryAfterSeconds) {
        super(errorCode, message, HttpStatus.SERVICE_UNAVAILABLE);
        this.serviceName = serviceName;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    /**
     * Gets the name of the unavailable service.
     * 
     * @return The service name, or null if not specified
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Gets the suggested retry delay in seconds.
     * 
     * @return The retry delay in seconds, or null if not specified
     */
    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
    
    /**
     * Common service unavailability reasons.
     */
    public static class ErrorCodes {
        public static final String MAINTENANCE_MODE = "MAINTENANCE_MODE";
        public static final String CIRCUIT_BREAKER_OPEN = "CIRCUIT_BREAKER_OPEN";
        public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
        public static final String DEPENDENCY_FAILURE = "DEPENDENCY_FAILURE";
        public static final String OVERLOADED = "SERVICE_OVERLOADED";
        public static final String TIMEOUT = "SERVICE_TIMEOUT";
        
        private ErrorCodes() {
            // Prevent instantiation
        }
    }
    
    /**
     * Factory method for maintenance mode exception.
     * 
     * @param serviceName The service in maintenance
     * @param estimatedDuration Estimated duration in seconds
     * @return ServiceUnavailableException for maintenance
     */
    public static ServiceUnavailableException maintenanceMode(String serviceName, Long estimatedDuration) {
        return new ServiceUnavailableException(
            ErrorCodes.MAINTENANCE_MODE,
            String.format("Service '%s' is under maintenance", serviceName),
            serviceName,
            estimatedDuration
        );
    }
    
    /**
     * Factory method for circuit breaker open exception.
     * 
     * @param serviceName The service with open circuit breaker
     * @return ServiceUnavailableException for circuit breaker
     */
    public static ServiceUnavailableException circuitBreakerOpen(String serviceName) {
        return new ServiceUnavailableException(
            ErrorCodes.CIRCUIT_BREAKER_OPEN,
            String.format("Circuit breaker is open for service '%s'", serviceName),
            serviceName,
            30L // Default 30 seconds retry
        );
    }
    
    /**
     * Factory method for service overload exception.
     * 
     * @param serviceName The overloaded service
     * @return ServiceUnavailableException for overload
     */
    public static ServiceUnavailableException serviceOverloaded(String serviceName) {
        return new ServiceUnavailableException(
            ErrorCodes.OVERLOADED,
            String.format("Service '%s' is currently overloaded", serviceName),
            serviceName,
            60L // Default 60 seconds retry
        );
    }
}