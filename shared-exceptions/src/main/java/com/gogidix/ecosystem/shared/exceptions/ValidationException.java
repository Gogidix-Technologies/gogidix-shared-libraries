package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation errors occur.
 * This exception can handle multiple field validation errors
 * and provides detailed information about what failed validation.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class ValidationException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";
    private final Map<String, List<String>> fieldErrors;
    
    /**
     * Constructs a new ValidationException with a message.
     * 
     * @param message The validation error message
     */
    public ValidationException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * Constructs a new ValidationException with a message and cause.
     * 
     * @param message The validation error message
     * @param cause The cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST, cause);
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * Constructs a new ValidationException with field errors.
     * 
     * @param message The validation error message
     * @param fieldErrors Map of field names to error messages
     */
    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }
    
    /**
     * Constructs a new ValidationException with error code and message.
     * 
     * @param errorCode Specific error code for this validation exception
     * @param message The validation error message
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * Creates a ValidationException for a single field error.
     * 
     * @param fieldName The name of the field that failed validation
     * @param errorMessage The validation error message for the field
     * @return New ValidationException with field error
     */
    public static ValidationException forField(String fieldName, String errorMessage) {
        ValidationException ex = new ValidationException(
            String.format("Validation failed for field '%s': %s", fieldName, errorMessage));
        ex.addFieldError(fieldName, errorMessage);
        return ex;
    }
    
    /**
     * Adds a field error to this validation exception.
     * 
     * @param fieldName The name of the field
     * @param errorMessage The error message for the field
     */
    public void addFieldError(String fieldName, String errorMessage) {
        fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
    }
    
    /**
     * Gets all field errors.
     * 
     * @return Map of field names to lists of error messages
     */
    public Map<String, List<String>> getFieldErrors() {
        return new HashMap<>(fieldErrors);
    }
    
    /**
     * Gets errors for a specific field.
     * 
     * @param fieldName The field name
     * @return List of error messages for the field, or empty list if no errors
     */
    public List<String> getFieldErrors(String fieldName) {
        return fieldErrors.getOrDefault(fieldName, new ArrayList<>());
    }
    
    /**
     * Checks if there are any field errors.
     * 
     * @return true if there are field errors, false otherwise
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
    
    /**
     * Gets the total count of field errors.
     * 
     * @return Total number of field errors
     */
    public int getErrorCount() {
        return fieldErrors.values().stream()
                .mapToInt(List::size)
                .sum();
    }
    
    /**
     * Creates a ValidationException from Spring's BindingResult or similar validation results.
     * 
     * @param message Overall validation message
     * @param fieldErrors Map of field errors
     * @return New ValidationException instance
     */
    public static ValidationException fromFieldErrors(String message, Map<String, List<String>> fieldErrors) {
        return new ValidationException(message, fieldErrors);
    }
}