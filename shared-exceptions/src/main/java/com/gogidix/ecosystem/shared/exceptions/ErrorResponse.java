package com.gogidix.ecosystem.shared.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Standardized error response structure for REST APIs.
 * Provides consistent error information across all microservices.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonProperty("timestamp")
    private final LocalDateTime timestamp;
    
    @JsonProperty("traceId")
    private final String traceId;
    
    @JsonProperty("status")
    private final int status;
    
    @JsonProperty("error")
    private final String error;
    
    @JsonProperty("errorCode")
    private final String errorCode;
    
    @JsonProperty("message")
    private final String message;
    
    @JsonProperty("path")
    private final String path;
    
    @JsonProperty("method")
    private final String method;
    
    @JsonProperty("fieldErrors")
    private final Map<String, List<String>> fieldErrors;
    
    @JsonProperty("metadata")
    private final Map<String, Object> metadata;
    
    /**
     * Private constructor - use builder pattern.
     */
    private ErrorResponse(Builder builder) {
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.traceId = builder.traceId != null ? builder.traceId : UUID.randomUUID().toString();
        this.status = builder.status;
        this.error = builder.error;
        this.errorCode = builder.errorCode;
        this.message = builder.message;
        this.path = builder.path;
        this.method = builder.method;
        this.fieldErrors = builder.fieldErrors;
        this.metadata = builder.metadata;
    }
    
    // Getters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getTraceId() {
        return traceId;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getError() {
        return error;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getMethod() {
        return method;
    }
    
    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * Creates an ErrorResponse from a BaseException.
     * 
     * @param exception The exception to convert
     * @param path The request path
     * @param method The HTTP method
     * @return ErrorResponse instance
     */
    public static ErrorResponse fromException(BaseException exception, String path, String method) {
        Builder builder = ErrorResponse.builder()
            .status(exception.getHttpStatusCode())
            .error(exception.getHttpStatus().getReasonPhrase())
            .errorCode(exception.getErrorCode())
            .message(exception.getMessage())
            .path(path)
            .method(method);
        
        // Add field errors if it's a ValidationException
        if (exception instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) exception;
            if (validationEx.hasFieldErrors()) {
                builder.fieldErrors(validationEx.getFieldErrors());
            }
        }
        
        // Add retry information if it's a ServiceUnavailableException
        if (exception instanceof ServiceUnavailableException) {
            ServiceUnavailableException serviceEx = (ServiceUnavailableException) exception;
            if (serviceEx.getRetryAfterSeconds() != null) {
                builder.addMetadata("retryAfterSeconds", serviceEx.getRetryAfterSeconds());
            }
            if (serviceEx.getServiceName() != null) {
                builder.addMetadata("serviceName", serviceEx.getServiceName());
            }
        }
        
        // Add resource information if it's a ResourceNotFoundException
        if (exception instanceof ResourceNotFoundException) {
            ResourceNotFoundException resourceEx = (ResourceNotFoundException) exception;
            if (resourceEx.getResourceType() != null) {
                builder.addMetadata("resourceType", resourceEx.getResourceType());
            }
            if (resourceEx.getResourceId() != null) {
                builder.addMetadata("resourceId", resourceEx.getResourceId());
            }
        }
        
        return builder.build();
    }
    
    /**
     * Creates a new ErrorResponse builder.
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for ErrorResponse.
     */
    public static class Builder {
        private LocalDateTime timestamp;
        private String traceId;
        private int status;
        private String error;
        private String errorCode;
        private String message;
        private String path;
        private String method;
        private Map<String, List<String>> fieldErrors;
        private Map<String, Object> metadata;
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }
        
        public Builder status(int status) {
            this.status = status;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        
        public Builder fieldErrors(Map<String, List<String>> fieldErrors) {
            this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : null;
            return this;
        }
        
        public Builder addFieldError(String field, String error) {
            if (this.fieldErrors == null) {
                this.fieldErrors = new HashMap<>();
            }
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : null;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}