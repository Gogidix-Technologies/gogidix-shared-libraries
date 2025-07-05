package com.gogidix.ecosystem.shared.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test suite for ExceptionHandler functionality.
 * Tests exception handling in a web context with real HTTP responses.
 */
@WebMvcTest
@ContextConfiguration(classes = {ExceptionHandlerIntegrationTest.TestConfig.class})
class ExceptionHandlerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Configuration
    static class TestConfig {
        
        @Bean
        public ExceptionHandler exceptionHandler() {
            return new ExceptionHandler();
        }
        
        @Bean
        public TestController testController() {
            return new TestController();
        }
        
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
    
    @RestController
    static class TestController {
        
        @GetMapping("/test/validation-exception")
        public String triggerValidationException() {
            ValidationException ex = new ValidationException("Validation failed");
            ex.addFieldError("email", "Email is required");
            ex.addFieldError("password", "Password too short");
            throw ex;
        }
        
        @GetMapping("/test/business-logic-exception")
        public String triggerBusinessLogicException() {
            throw new BusinessException("Business rule violated");
        }
        
        @GetMapping("/test/resource-not-found/{id}")
        public String triggerResourceNotFoundException(@PathVariable String id) {
            throw new ResourceNotFoundException("User", id);
        }
        
        @GetMapping("/test/unauthorized-exception")
        public String triggerUnauthorizedException() {
            throw new AuthorizationException("Access denied");
        }
        
        @GetMapping("/test/external-service-exception")
        public String triggerExternalServiceException() {
            throw new ServiceUnavailableException("Payment gateway service error: Gateway timeout");
        }
        
        @GetMapping("/test/data-integrity-exception")
        public String triggerDataIntegrityException() {
            throw new BusinessException("Unique constraint violation");
        }
        
        @GetMapping("/test/runtime-exception")
        public String triggerRuntimeException() {
            throw new RuntimeException("Unexpected runtime error");
        }
        
        @GetMapping("/test/null-pointer-exception")
        public String triggerNullPointerException() {
            throw new NullPointerException("Null pointer encountered");
        }
    }
    
    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(get("/test/validation-exception"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/validation-exception"))
            .andExpect(jsonPath("$.fieldErrors").exists())
            .andExpect(jsonPath("$.fieldErrors.email[0]").value("Email is required"))
            .andExpect(jsonPath("$.fieldErrors.password[0]").value("Password too short"));
    }
    
    @Test
    void shouldHandleBusinessLogicException() throws Exception {
        mockMvc.perform(get("/test/business-logic-exception"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"))
            .andExpect(jsonPath("$.message").value("Business rule violated"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/business-logic-exception"));
    }
    
    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found/123"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("User not found with id: 123"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/resource-not-found/123"));
    }
    
    @Test
    void shouldHandleUnauthorizedException() throws Exception {
        mockMvc.perform(get("/test/unauthorized-exception"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("AUTHORIZATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Access denied"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/unauthorized-exception"));
    }
    
    @Test
    void shouldHandleExternalServiceException() throws Exception {
        mockMvc.perform(get("/test/external-service-exception"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.error").value("SERVICE_UNAVAILABLE"))
            .andExpect(jsonPath("$.message").value("Payment gateway service error: Gateway timeout"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/external-service-exception"));
    }
    
    @Test
    void shouldHandleDataIntegrityException() throws Exception {
        mockMvc.perform(get("/test/data-integrity-exception"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"))
            .andExpect(jsonPath("$.message").value("Unique constraint violation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/data-integrity-exception"));
    }
    
    @Test
    void shouldHandleGenericRuntimeException() throws Exception {
        mockMvc.perform(get("/test/runtime-exception"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/runtime-exception"));
    }
    
    @Test
    void shouldHandleNullPointerException() throws Exception {
        mockMvc.perform(get("/test/null-pointer-exception"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/test/null-pointer-exception"));
    }
    
    @Test
    void shouldIncludeRequestIdInResponse() throws Exception {
        // Test that request ID is included when present
        mockMvc.perform(get("/test/validation-exception")
                .header("X-Request-ID", "test-request-123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.requestId").exists());
    }
    
    @Test
    void shouldHandleMethodNotAllowed() throws Exception {
        // Test HTTP method not allowed scenarios
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/test/validation-exception"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }
    
    @Test
    void shouldValidateErrorResponseStructure() throws Exception {
        // Test that all error responses have consistent structure
        mockMvc.perform(get("/test/validation-exception"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").exists());
    }
    
    @Test
    void shouldNotExposeInternalStackTrace() throws Exception {
        // Test that internal stack traces are not exposed in production
        mockMvc.perform(get("/test/runtime-exception"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.trace").doesNotExist());
    }
}