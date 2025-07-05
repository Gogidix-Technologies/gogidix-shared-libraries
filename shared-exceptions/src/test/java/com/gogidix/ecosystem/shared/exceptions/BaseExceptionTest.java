package com.gogidix.ecosystem.shared.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for BaseException functionality.
 * Tests exception creation, HTTP status mapping, and error code handling.
 */
class BaseExceptionTest {
    
    // Test implementation of BaseException for testing purposes
    private static class TestException extends BaseException {
        public TestException(String errorCode, String message, HttpStatus httpStatus) {
            super(errorCode, message, httpStatus);
        }
        
        public TestException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
            super(errorCode, message, httpStatus, cause);
        }
        
        public TestException(String errorCode, String message, HttpStatus httpStatus, Object... messageArgs) {
            super(errorCode, message, httpStatus, messageArgs);
        }
    }
    
    @Test
    void shouldCreateExceptionWithBasicProperties() {
        // Given
        String message = "Test error message";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "TEST_ERROR";
        
        // When
        TestException exception = new TestException(errorCode, message, status);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessageArgs()).isEmpty();
        assertThat(exception.getCause()).isNull();
    }
    
    @Test
    void shouldCreateExceptionWithCause() {
        // Given
        String message = "Test error with cause";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "INTERNAL_ERROR";
        Throwable cause = new RuntimeException("Root cause");
        
        // When
        TestException exception = new TestException(errorCode, message, status, cause);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
    
    @Test
    void shouldCreateExceptionWithMessageArguments() {
        // Given
        String message = "Error for user {0} in operation {1}";
        HttpStatus status = HttpStatus.FORBIDDEN;
        String errorCode = "ACCESS_DENIED";
        Object[] args = {"john.doe", "DELETE_USER"};
        
        // When
        TestException exception = new TestException(errorCode, message, status, args);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessageArgs()).containsExactly(args);
    }
    
    @Test
    void shouldHandleNullValues() {
        // When
        TestException exception = new TestException(null, null, null);
        
        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getHttpStatus()).isNull();
        assertThat(exception.getErrorCode()).isNull();
        assertThat(exception.getMessageArgs()).isEmpty();
    }
    
    @Test
    void shouldHandleEmptyMessageArguments() {
        // Given
        String message = "Simple error message";
        HttpStatus status = HttpStatus.NOT_FOUND;
        String errorCode = "NOT_FOUND";
        Object[] emptyArgs = {};
        
        // When
        TestException exception = new TestException(errorCode, message, status, emptyArgs);
        
        // Then
        assertThat(exception.getMessageArgs()).isEmpty();
    }
    
    @Test
    void shouldPreserveStackTrace() {
        // Given
        String message = "Test stack trace";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "STACK_TRACE_TEST";
        
        // When
        TestException exception = new TestException(errorCode, message, status);
        
        // Then
        assertThat(exception.getStackTrace()).isNotEmpty();
        assertThat(exception.getStackTrace()[0].getMethodName()).isEqualTo("shouldPreserveStackTrace");
    }
    
    @Test
    void shouldSupportDifferentHttpStatuses() {
        // Test various HTTP status codes
        HttpStatus[] statuses = {
            HttpStatus.BAD_REQUEST,
            HttpStatus.UNAUTHORIZED,
            HttpStatus.FORBIDDEN,
            HttpStatus.NOT_FOUND,
            HttpStatus.CONFLICT,
            HttpStatus.INTERNAL_SERVER_ERROR,
            HttpStatus.SERVICE_UNAVAILABLE
        };
        
        for (HttpStatus status : statuses) {
            // When
            TestException exception = new TestException("TEST", "Test", status);
            
            // Then
            assertThat(exception.getHttpStatus()).isEqualTo(status);
        }
    }
    
    @Test
    void shouldHandleLongErrorMessages() {
        // Given
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long error message. ");
        }
        String message = longMessage.toString();
        
        // When
        TestException exception = new TestException("LONG_MESSAGE", message, HttpStatus.BAD_REQUEST);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage().length()).isGreaterThan(1000);
    }
    
    @Test
    void shouldHandleSpecialCharactersInErrorCode() {
        // Given
        String errorCode = "ERROR_CODE_WITH_SPECIAL_CHARS_123_!@#$%";
        
        // When
        TestException exception = new TestException(errorCode, "Test", HttpStatus.BAD_REQUEST);
        
        // Then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    }
    
    @Test
    void shouldHandleMultipleMessageArguments() {
        // Given
        Object[] args = {
            "stringArg", 
            123, 
            true, 
            new java.util.Date(),
            null,
            new Object[] {"nested", "array"}
        };
        
        // When
        TestException exception = new TestException("MULTI_ARGS", "Test with args", HttpStatus.BAD_REQUEST, args);
        
        // Then
        assertThat(exception.getMessageArgs()).hasSize(6);
        assertThat(exception.getMessageArgs()[0]).isEqualTo("stringArg");
        assertThat(exception.getMessageArgs()[1]).isEqualTo(123);
        assertThat(exception.getMessageArgs()[2]).isEqualTo(true);
        assertThat(exception.getMessageArgs()[4]).isNull();
    }
}