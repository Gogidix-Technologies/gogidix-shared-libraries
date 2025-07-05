package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when business logic violations occur.
 * This exception should be used for domain-specific business rule violations
 * that are expected and handled gracefully by the application.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class BusinessException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "BUSINESS_ERROR";
    
    /**
     * Constructs a new BusinessException with a message.
     * Uses default error code and HTTP 400 Bad Request status.
     * 
     * @param message The error message
     */
    public BusinessException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Constructs a new BusinessException with a message and cause.
     * Uses default error code and HTTP 400 Bad Request status.
     * 
     * @param message The error message
     * @param cause The cause of this exception
     */
    public BusinessException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST, cause);
    }
    
    /**
     * Constructs a new BusinessException with error code and message.
     * Uses HTTP 400 Bad Request status.
     * 
     * @param errorCode Specific error code for this business exception
     * @param message The error message
     */
    public BusinessException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Constructs a new BusinessException with error code, message, and cause.
     * Uses HTTP 400 Bad Request status.
     * 
     * @param errorCode Specific error code for this business exception
     * @param message The error message
     * @param cause The cause of this exception
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.BAD_REQUEST, cause);
    }
    
    /**
     * Constructs a new BusinessException with error code, message, and custom HTTP status.
     * 
     * @param errorCode Specific error code for this business exception
     * @param message The error message
     * @param httpStatus Custom HTTP status for this exception
     */
    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }
    
    /**
     * Constructs a new BusinessException with message template and arguments.
     * Uses default error code and HTTP 400 Bad Request status.
     * 
     * @param message Message template with placeholders
     * @param messageArgs Arguments to fill placeholders
     */
    public BusinessException(String message, Object... messageArgs) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST, messageArgs);
    }
    
    /**
     * Constructs a new BusinessException with error code, message template, and arguments.
     * Uses HTTP 400 Bad Request status.
     * 
     * @param errorCode Specific error code for this business exception
     * @param message Message template with placeholders
     * @param messageArgs Arguments to fill placeholders
     */
    public BusinessException(String errorCode, String message, Object... messageArgs) {
        super(errorCode, message, HttpStatus.BAD_REQUEST, messageArgs);
    }
}