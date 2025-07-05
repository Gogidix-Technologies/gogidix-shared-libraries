package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom exceptions in the Exalt ecosystem.
 * Provides a consistent structure for error handling across all microservices.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object[] messageArgs;
    
    /**
     * Constructs a new BaseException with error code, message, and HTTP status.
     * 
     * @param errorCode Unique error code for this exception
     * @param message Human-readable error message
     * @param httpStatus HTTP status code to return
     */
    protected BaseException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.messageArgs = new Object[0];
    }
    
    /**
     * Constructs a new BaseException with error code, message, HTTP status, and cause.
     * 
     * @param errorCode Unique error code for this exception
     * @param message Human-readable error message
     * @param httpStatus HTTP status code to return
     * @param cause The cause of this exception
     */
    protected BaseException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.messageArgs = new Object[0];
    }
    
    /**
     * Constructs a new BaseException with error code, message, HTTP status, and message arguments.
     * Used for internationalized error messages with placeholders.
     * 
     * @param errorCode Unique error code for this exception
     * @param message Message template with placeholders
     * @param httpStatus HTTP status code to return
     * @param messageArgs Arguments to fill message placeholders
     */
    protected BaseException(String errorCode, String message, HttpStatus httpStatus, Object... messageArgs) {
        super(formatMessage(message, messageArgs));
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.messageArgs = messageArgs;
    }
    
    /**
     * Gets the unique error code for this exception.
     * 
     * @return The error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the HTTP status associated with this exception.
     * 
     * @return The HTTP status
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * Gets the message arguments for internationalization.
     * 
     * @return Array of message arguments
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }
    
    /**
     * Gets the HTTP status code as an integer.
     * 
     * @return The HTTP status code
     */
    public int getHttpStatusCode() {
        return httpStatus.value();
    }
    
    /**
     * Formats a message template with the provided arguments.
     * 
     * @param message Message template
     * @param args Arguments to fill placeholders
     * @return Formatted message
     */
    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        try {
            return String.format(message, args);
        } catch (Exception e) {
            // If formatting fails, return the original message
            return message;
        }
    }
}