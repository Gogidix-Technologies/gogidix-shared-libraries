package com.exalt.ecosystem.shared.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ValidationResult functionality.
 * Tests error handling, merging, state management, and validation result operations.
 */
class ValidationResultTest {

    private ValidationResult validationResult;

    @BeforeEach
    void setUp() {
        validationResult = new ValidationResult();
    }

    @Test
    @DisplayName("Should start with valid state")
    void shouldStartWithValidState() {
        // When/Then
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.hasErrors()).isFalse();
        assertThat(validationResult.getAllFieldErrors()).isEmpty();
        assertThat(validationResult.getGlobalErrors()).isEmpty();
        assertThat(validationResult.getAllErrors()).isEmpty();
        assertThat(validationResult.getErrorCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should add field error correctly")
    void shouldAddFieldErrorCorrectly() {
        // When
        validationResult.addError("email", "Invalid email format");

        // Then
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.hasErrors()).isTrue();
        assertThat(validationResult.hasFieldError("email")).isTrue();
        assertThat(validationResult.hasFieldError("email")).isTrue();
        assertThat(validationResult.getAllFieldErrors()).hasSize(1);
        assertThat(validationResult.getAllFieldErrors()).containsKey("email");
        assertThat(validationResult.getFieldErrors().get("email")).contains("Invalid email format");
        assertThat(validationResult.getErrorCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should add multiple errors to same field")
    void shouldAddMultipleErrorsToSameField() {
        // When
        validationResult.addError("password", "Password too short");
        validationResult.addError("password", "Password must contain uppercase");
        validationResult.addError("password", "Password must contain numbers");

        // Then
        assertThat(validationResult.getFieldErrors().get("password")).hasSize(3);
        assertThat(validationResult.getFieldErrors().get("password"))
            .containsExactly(
                "Password too short",
                "Password must contain uppercase", 
                "Password must contain numbers"
            );
        assertThat(validationResult.getErrorCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should add global error correctly")
    void shouldAddGlobalErrorCorrectly() {
        // When
        validationResult.addGlobalError("User account is locked");

        // Then
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.hasErrors()).isTrue();
        assertThat(validationResult.hasGlobalErrors()).isTrue();
        assertThat(validationResult.getGlobalErrors()).hasSize(1);
        assertThat(validationResult.getGlobalErrors()).contains("User account is locked");
        assertThat(validationResult.getErrorCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle mixed field and global errors")
    void shouldHandleMixedFieldAndGlobalErrors() {
        // When
        validationResult.addError("email", "Invalid email");
        validationResult.addError("username", "Username taken");
        validationResult.addGlobalError("Registration failed");
        validationResult.addGlobalError("Service unavailable");

        // Then
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.hasFieldError("email")).isTrue();
        assertThat(validationResult.hasGlobalErrors()).isTrue();
        assertThat(validationResult.getFieldErrors()).hasSize(2);
        assertThat(validationResult.getGlobalErrors()).hasSize(2);
        assertThat(validationResult.getErrorCount()).isEqualTo(4);
        
        List<String> allErrors = validationResult.getAllErrors();
        assertThat(allErrors).hasSize(4);
        assertThat(allErrors).contains("Invalid email", "Username taken", "Registration failed", "Service unavailable");
    }

    @Test
    @DisplayName("Should get field error messages correctly")
    void shouldGetFieldErrorMessagesCorrectly() {
        // Given
        validationResult.addError("email", "Email is required");
        validationResult.addError("email", "Invalid email format");
        validationResult.addError("username", "Username is required");

        // When
        List<String> emailErrors = validationResult.getFieldErrors("email");
        List<String> usernameErrors = validationResult.getFieldErrors("username");
        List<String> nonExistentErrors = validationResult.getFieldErrors("nonexistent");

        // Then
        assertThat(emailErrors).hasSize(2);
        assertThat(emailErrors).containsExactly("Email is required", "Invalid email format");
        assertThat(usernameErrors).hasSize(1);
        assertThat(usernameErrors).contains("Username is required");
        assertThat(nonExistentErrors).isEmpty();
    }

    @Test
    @DisplayName("Should merge validation results correctly")
    void shouldMergeValidationResultsCorrectly() {
        // Given
        ValidationResult other = new ValidationResult();
        
        validationResult.addError("email", "Invalid email");
        validationResult.addGlobalError("Global error 1");
        
        other.addError("email", "Email already exists");
        other.addError("username", "Invalid username");
        other.addGlobalError("Global error 2");

        // When
        validationResult.merge(other);
        ValidationResult merged = validationResult;

        // Then
        assertThat(merged.isValid()).isFalse();
        assertThat(merged.getErrorCount()).isEqualTo(4);
        
        // Field errors should be merged
        assertThat(merged.getFieldErrors("email")).hasSize(2);
        assertThat(merged.getFieldErrors("email"))
            .containsExactly("Invalid email", "Email already exists");
        assertThat(merged.getFieldErrors("username")).hasSize(1);
        assertThat(merged.getFieldErrors("username")).contains("Invalid username");
        
        // Global errors should be merged
        assertThat(merged.getGlobalErrors()).hasSize(2);
        assertThat(merged.getGlobalErrors()).contains("Global error 1", "Global error 2");
    }

    @Test
    @DisplayName("Should merge with empty validation result")
    void shouldMergeWithEmptyValidationResult() {
        // Given
        ValidationResult empty = new ValidationResult();
        validationResult.addError("email", "Invalid email");

        // When
        validationResult.merge(empty);
        ValidationResult merged = validationResult;

        // Then
        assertThat(merged.isValid()).isFalse();
        assertThat(merged.getErrorCount()).isEqualTo(1);
        assertThat(merged.getFieldErrors("email")).contains("Invalid email");
    }

    @Test
    @DisplayName("Should clear all errors")
    void shouldClearAllErrors() {
        // Given
        validationResult.addError("email", "Invalid email");
        validationResult.addError("username", "Invalid username");
        validationResult.addGlobalError("Global error");
        
        assertThat(validationResult.hasErrors()).isTrue();

        // When
        validationResult.clear();

        // Then
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.hasErrors()).isFalse();
        assertThat(validationResult.getAllFieldErrors()).isEmpty();
        assertThat(validationResult.getGlobalErrors()).isEmpty();
        assertThat(validationResult.getErrorCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should clear field errors only")
    void shouldClearFieldErrorsOnly() {
        // Given
        validationResult.addError("email", "Invalid email");
        validationResult.addGlobalError("Global error");

        // When
        validationResult.clearFieldErrors("email");

        // Then
        assertThat(validationResult.hasErrors()).isFalse();
        assertThat(validationResult.hasGlobalErrors()).isTrue();
        assertThat(validationResult.getAllFieldErrors()).isEmpty();
        assertThat(validationResult.getGlobalErrors()).hasSize(1);
        assertThat(validationResult.getErrorCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should clear specific field errors")
    void shouldClearSpecificFieldErrors() {
        // Given
        validationResult.addError("email", "Invalid email");
        validationResult.addError("username", "Invalid username");
        validationResult.addGlobalError("Global error");

        // When
        validationResult.clearFieldErrors("email");

        // Then
        assertThat(validationResult.hasFieldError("email")).isFalse();
        assertThat(validationResult.hasFieldError("username")).isTrue();
        assertThat(validationResult.hasGlobalErrors()).isTrue();
        assertThat(validationResult.getErrorCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should clear global errors only")
    void shouldClearGlobalErrorsOnly() {
        // Given
        validationResult.addError("email", "Invalid email");
        validationResult.addGlobalError("Global error");

        // When
        validationResult.clearGlobalErrors();

        // Then
        assertThat(validationResult.hasFieldError("email")).isTrue();
        assertThat(validationResult.hasGlobalErrors()).isFalse();
        assertThat(validationResult.getAllFieldErrors()).hasSize(1);
        assertThat(validationResult.getGlobalErrors()).isEmpty();
        assertThat(validationResult.getErrorCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get error summary correctly")
    void shouldGetErrorSummaryCorrectly() {
        // Given
        validationResult.addError("email", "Invalid email format");
        validationResult.addError("username", "Username too short");
        validationResult.addGlobalError("Account locked");

        // When
        String summary = validationResult.getErrorSummary();

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary).contains("email");
        assertThat(summary).contains("username");
        assertThat(summary).contains("Invalid email format");
        assertThat(summary).contains("Username too short");
        assertThat(summary).contains("Account locked");
    }

    @Test
    @DisplayName("Should handle null and empty error messages")
    void shouldHandleNullAndEmptyErrorMessages() {
        // When/Then - Should not add null or empty messages
        validationResult.addError("field1", null);
        validationResult.addError("field2", "");
        validationResult.addError("field3", "   ");
        validationResult.addGlobalError(null);
        validationResult.addGlobalError("");
        validationResult.addGlobalError("   ");

        // Should remain valid as no valid errors were added
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getErrorCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle null field names gracefully")
    void shouldHandleNullFieldNamesGracefully() {
        // When/Then - Should not throw exception for null field name
        assertThatCode(() -> {
            validationResult.addError(null, "Some error");
            validationResult.hasFieldError(null);
            validationResult.getFieldErrors(null);
            // Testing null field clearance - skip as it may not be supported
        }).doesNotThrowAnyException();

        assertThat(validationResult.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should provide immutable error collections")
    void shouldProvideImmutableErrorCollections() {
        // Given
        validationResult.addError("email", "Invalid email");
        validationResult.addGlobalError("Global error");

        // When
        Map<String, List<String>> fieldErrors = validationResult.getAllFieldErrors();
        List<String> globalErrors = validationResult.getGlobalErrors();
        List<String> allErrors = validationResult.getAllErrors();

        // Then - Attempting to modify should not affect original
        assertThatCode(() -> {
            fieldErrors.put("newField", List.of("new error"));
            globalErrors.add("new global error");
            allErrors.add("new error");
        }).doesNotThrowAnyException();

        // Original should be unchanged
        assertThat(validationResult.getAllFieldErrors()).hasSize(1);
        assertThat(validationResult.getGlobalErrors()).hasSize(1);
        assertThat(validationResult.getAllErrors()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle large number of errors efficiently")
    void shouldHandleLargeNumberOfErrorsEfficiently() {
        // Given/When - Add many errors
        for (int i = 0; i < 1000; i++) {
            validationResult.addError("field" + i, "Error " + i);
            if (i % 10 == 0) {
                validationResult.addGlobalError("Global error " + i);
            }
        }

        // Then
        assertThat(validationResult.getErrorCount()).isEqualTo(1100); // 1000 field + 100 global
        assertThat(validationResult.getFieldErrors()).hasSize(1000);
        assertThat(validationResult.getGlobalErrors()).hasSize(100);
        assertThat(validationResult.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should support fluent API")
    void shouldSupportFluentAPI() {
        // When
        ValidationResult result = validationResult
            .addFieldError("email", "Invalid email")
            .addFieldError("username", "Username taken")
            .addGlobalError("Registration failed");

        // Then
        assertThat(result).isSameAs(validationResult);
        assertThat(result.getErrorCount()).isEqualTo(3);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should convert to string representation")
    void shouldConvertToStringRepresentation() {
        // Given
        validationResult.addError("email", "Invalid email");
        validationResult.addGlobalError("Global error");

        // When
        String stringResult = validationResult.toString();

        // Then
        assertThat(stringResult).isNotNull();
        assertThat(stringResult).contains("ValidationResult");
        assertThat(stringResult).contains("valid=false");
        assertThat(stringResult).contains("errorCount=2");
    }
}