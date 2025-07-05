package com.gogidix.ecosystem.shared.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * Provides centralized exception handling across all microservices.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@ControllerAdvice
public class ExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    
    /**
     * Handles BaseException and all its subclasses.
     * 
     * @param ex The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        logException(ex, request);
        
        ErrorResponse errorResponse = ErrorResponse.fromException(
            ex, 
            request.getRequestURI(), 
            request.getMethod()
        );
        
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }
    
    /**
     * Handles Spring's MethodArgumentNotValidException for @Valid failures.
     * 
     * @param ex The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        logException(ex, request);
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new java.util.ArrayList<>())
                      .add(errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode("VALIDATION_ERROR")
            .message("Validation failed for one or more fields")
            .path(request.getRequestURI())
            .method(request.getMethod())
            .fieldErrors(fieldErrors)
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles ConstraintViolationException for method parameter validation.
     * 
     * @param ex The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, 
            HttpServletRequest request) {
        
        logException(ex, request);
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            fieldErrors.computeIfAbsent(propertyPath, k -> new java.util.ArrayList<>())
                      .add(message);
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode("CONSTRAINT_VIOLATION")
            .message("Constraint validation failed")
            .path(request.getRequestURI())
            .method(request.getMethod())
            .fieldErrors(fieldErrors)
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles type mismatch exceptions.
     * 
     * @param ex The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, 
            HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode("TYPE_MISMATCH")
            .message(message)
            .path(request.getRequestURI())
            .method(request.getMethod())
            .addMetadata("parameter", ex.getName())
            .addMetadata("invalidValue", ex.getValue())
            .addMetadata("expectedType", ex.getRequiredType().getSimpleName())
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles IllegalArgumentException.
     * 
     * @param ex The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        
        logException(ex, request);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode("ILLEGAL_ARGUMENT")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles all other exceptions as internal server errors.
     * 
     * @param ex The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        logger.error("Unhandled exception occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .errorCode("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .path(request.getRequestURI())
            .method(request.getMethod())
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Logs exception details.
     * 
     * @param ex The exception
     * @param request The HTTP request
     */
    private void logException(Exception ex, HttpServletRequest request) {
        if (ex instanceof BaseException) {
            BaseException baseEx = (BaseException) ex;
            if (baseEx.getHttpStatus().is4xxClientError()) {
                logger.warn("Client error occurred: {} - {} [{}] {}", 
                    baseEx.getErrorCode(), 
                    baseEx.getMessage(),
                    request.getMethod(),
                    request.getRequestURI());
            } else {
                logger.error("Server error occurred: {} - {} [{}] {}", 
                    baseEx.getErrorCode(), 
                    baseEx.getMessage(),
                    request.getMethod(),
                    request.getRequestURI(), 
                    ex);
            }
        } else {
            logger.error("Exception occurred during request [{}] {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                ex);
        }
    }
}