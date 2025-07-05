package com.gogidix.ecosystem.shared.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for ValidationException functionality.
 * Tests field validation, error aggregation, and static factory methods.
 */
class ValidationExceptionTest {
    
    @Test
    void shouldCreateValidationExceptionWithSingleError() {
        // Given
        String message = "Validation failed";
        
        // When
        ValidationException exception = new ValidationException(message);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getFieldErrors()).isEmpty();
    }
    
    @Test
    void shouldCreateValidationExceptionWithFieldErrors() {
        // Given
        String message = "Multiple validation errors";
        Map<String, List<String>> fieldErrors = Map.of(
            "email", Arrays.asList("Email is required"),
            "password", Arrays.asList("Password too short"),
            "age", Arrays.asList("Age must be positive")
        );
        
        // When
        ValidationException exception = new ValidationException(message, fieldErrors);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getFieldErrors()).isNotEmpty();
        assertThat(exception.hasFieldErrors()).isTrue();
        assertThat(exception.getErrorCount()).isEqualTo(3);
    }
    
    @Test
    void shouldCreateValidationExceptionWithCause() {
        // Given
        String message = "Validation error with cause";
        Throwable cause = new IllegalArgumentException("Invalid input");
        
        // When
        ValidationException exception = new ValidationException(message, cause);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getFieldErrors()).isEmpty();
    }
    
    @Test
    void shouldCreateFieldValidationExceptionUsingStaticMethod() {
        // Given
        String fieldName = "email";
        String fieldError = "Email format is invalid";
        
        // When
        ValidationException exception = ValidationException.forField(fieldName, fieldError);
        
        // Then
        assertThat(exception.getMessage()).contains("email");
        assertThat(exception.getFieldErrors("email")).contains(fieldError);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
    
    @Test
    void shouldHandleNullFieldName() {
        // When
        ValidationException exception = ValidationException.forField(null, "Error message");
        
        // Then
        assertThat(exception.getMessage()).contains("null");
        assertThat(exception.hasFieldErrors()).isTrue();
    }
    
    @Test
    void shouldHandleNullFieldError() {
        // When
        ValidationException exception = ValidationException.forField("testField", null);
        
        // Then
        assertThat(exception.getMessage()).contains("testField");
        assertThat(exception.hasFieldErrors()).isTrue();
    }
    
    @Test
    void shouldHandleEmptyValidationErrorsMap() {
        // Given
        Map<String, List<String>> emptyErrors = Collections.emptyMap();
        
        // When
        ValidationException exception = new ValidationException("Test", emptyErrors);
        
        // Then
        assertThat(exception.getFieldErrors()).isEmpty();
    }
    
    @Test
    void shouldHandleNullValidationErrorsMap() {
        // When
        ValidationException exception = new ValidationException("Test", (Map<String, List<String>>) null);
        
        // Then
        assertThat(exception.getFieldErrors()).isEmpty();
    }
    
    @Test
    void shouldAddFieldErrors() {
        // Given
        ValidationException exception = new ValidationException("Test");
        
        // When
        exception.addFieldError("field1", "Error 1");
        exception.addFieldError("field1", "Error 2");
        exception.addFieldError("field2", "Error 3");
        
        // Then
        assertThat(exception.hasFieldErrors()).isTrue();
        assertThat(exception.getErrorCount()).isEqualTo(3);
        assertThat(exception.getFieldErrors("field1")).hasSize(2);
        assertThat(exception.getFieldErrors("field2")).hasSize(1);
    }
    
    @Test
    void shouldHandleLargeNumberOfValidationErrors() {
        // Given
        ValidationException exception = new ValidationException("Many errors");
        
        // When
        for (int i = 1; i <= 15; i++) {
            exception.addFieldError("field" + i, "Error " + i);
        }
        
        // Then
        assertThat(exception.getErrorCount()).isEqualTo(15);
        assertThat(exception.hasFieldErrors()).isTrue();
    }
    
    @Test
    void shouldHandleSpecialCharactersInFieldNames() {
        // Given
        String specialFieldName = "field-with_special.chars[0]";
        String errorMessage = "Special field error";
        
        // When
        ValidationException exception = ValidationException.forField(specialFieldName, errorMessage);
        
        // Then
        assertThat(exception.getMessage()).contains(specialFieldName);
        assertThat(exception.getFieldErrors(specialFieldName)).contains(errorMessage);
    }
    
    @Test
    void shouldHandleMultilineErrorMessages() {
        // Given
        String multilineError = "This is a multiline error message.\nIt spans multiple lines.\nAnd provides detailed information.";
        
        // When
        ValidationException exception = ValidationException.forField("description", multilineError);
        
        // Then
        assertThat(exception.getFieldErrors("description")).contains(multilineError);
    }
    
    @Test
    void shouldHandleUnicodeCharacters() {
        // Given
        String unicodeFieldName = "用户名"; // Chinese characters
        String unicodeError = "用户名不能为空"; // Chinese error message
        
        // When
        ValidationException exception = ValidationException.forField(unicodeFieldName, unicodeError);
        
        // Then
        assertThat(exception.getMessage()).contains(unicodeFieldName);
        assertThat(exception.getFieldErrors(unicodeFieldName)).contains(unicodeError);
    }
    
    @Test
    void shouldMaintainImmutableFieldErrorsMap() {
        // Given
        Map<String, List<String>> originalErrors = Map.of("field1", Arrays.asList("Error 1", "Error 2"));
        ValidationException exception = new ValidationException("Test", originalErrors);
        
        // When
        Map<String, List<String>> retrievedErrors = exception.getFieldErrors();
        
        // Then - Should not be able to modify the returned map
        assertThat(retrievedErrors).isNotNull();
        assertThat(retrievedErrors).isNotSameAs(originalErrors);
    }
    
    @Test
    void shouldCreateFromFieldErrors() {
        // Given
        String message = "Complex validation";
        Map<String, List<String>> fieldErrors = Map.of(
            "username", Arrays.asList("Required", "Too short"),
            "email", Arrays.asList("Invalid format")
        );
        
        // When
        ValidationException exception = ValidationException.fromFieldErrors(message, fieldErrors);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.hasFieldErrors()).isTrue();
        assertThat(exception.getErrorCount()).isEqualTo(3);
    }
}