package com.exalt.ecosystem.shared.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Base integration test class for the Exalt Social E-commerce Ecosystem.
 * Provides comprehensive testing infrastructure with containers, security, and utilities.
 * 
 * <p>This base class provides:</p>
 * <ul>
 *   <li>Spring Boot test configuration with random port</li>
 *   <li>Testcontainers integration for external dependencies</li>
 *   <li>MockMvc setup for web layer testing</li>
 *   <li>TestRestTemplate for full-stack integration testing</li>
 *   <li>Transaction management with rollback support</li>
 *   <li>Authentication utilities for security testing</li>
 *   <li>Database setup and cleanup utilities</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @SpringBootTest
 * class MyServiceIntegrationTest extends BaseIntegrationTest {
 *     
 *     @Test
 *     void shouldProcessOrderSuccessfully() {
 *         // Given
 *         authenticateAsCustomer();
 *         Map<String, Object> order = TestDataFactory.OrderData.createOrder();
 *         
 *         // When
 *         ResponseEntity<String> response = performPost("/api/orders", order);
 *         
 *         // Then
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
public abstract class BaseIntegrationTest {
    
    @LocalServerPort
    protected int port;
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired(required = false)
    protected DataSource dataSource;
    
    protected String baseUrl;
    protected Map<String, String> defaultHeaders;
    protected String currentAuthToken;
    
    @BeforeEach
    public void setUpBaseIntegrationTest() {
        this.baseUrl = "http://localhost:" + port;
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put("Content-Type", "application/json");
        this.defaultHeaders.put("Accept", "application/json");
        
        log.info("Integration test setup completed for: {}", this.getClass().getSimpleName());
        log.debug("Test server running on port: {}", port);
        log.debug("Base URL: {}", baseUrl);
    }
    
    /**
     * Authentication Utilities
     */
    
    /**
     * Authenticates as a customer user for testing customer-specific endpoints.
     * 
     * @return JWT token for authenticated customer
     */
    protected String authenticateAsCustomer() {
        return authenticateAs("customer@test.com", "CUSTOMER");
    }
    
    /**
     * Authenticates as a vendor user for testing vendor-specific endpoints.
     * 
     * @return JWT token for authenticated vendor
     */
    protected String authenticateAsVendor() {
        return authenticateAs("vendor@test.com", "VENDOR");
    }
    
    /**
     * Authenticates as an admin user for testing admin-specific endpoints.
     * 
     * @return JWT token for authenticated admin
     */
    protected String authenticateAsAdmin() {
        return authenticateAs("admin@test.com", "ADMIN");
    }
    
    /**
     * Authenticates with specified credentials and role.
     * 
     * @param email user email
     * @param role user role
     * @return JWT token for authenticated user
     */
    protected String authenticateAs(String email, String role) {
        try {
            // Create test user
            Map<String, Object> user = TestDataFactory.UserData.createUserWithRole(role);
            user.put("email", email);
            
            // Mock authentication request
            Map<String, String> authRequest = new HashMap<>();
            authRequest.put("email", email);
            authRequest.put("password", "testPassword123");
            
            // In a real implementation, this would call the auth service
            String token = generateTestJwtToken(user);
            this.currentAuthToken = token;
            
            // Set authorization header
            this.defaultHeaders.put("Authorization", "Bearer " + token);
            
            log.debug("Authenticated as {} with role: {}", email, role);
            return token;
            
        } catch (Exception e) {
            log.error("Authentication failed for {} with role: {}", email, role, e);
            throw new RuntimeException("Authentication failed", e);
        }
    }
    
    /**
     * Clears current authentication.
     */
    protected void clearAuthentication() {
        this.currentAuthToken = null;
        this.defaultHeaders.remove("Authorization");
        log.debug("Authentication cleared");
    }
    
    /**
     * HTTP Request Utilities
     */
    
    /**
     * Performs a GET request to the specified endpoint.
     * 
     * @param endpoint API endpoint (relative to base URL)
     * @return response entity
     */
    protected org.springframework.http.ResponseEntity<String> performGet(String endpoint) {
        return performGet(endpoint, new HashMap<>());
    }
    
    /**
     * Performs a GET request with query parameters.
     * 
     * @param endpoint API endpoint
     * @param params query parameters
     * @return response entity
     */
    protected org.springframework.http.ResponseEntity<String> performGet(String endpoint, Map<String, Object> params) {
        try {
            String url = buildUrl(endpoint, params);
            org.springframework.http.HttpHeaders headers = createHeaders();
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            log.debug("Performing GET request to: {}", url);
            return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
        } catch (Exception e) {
            log.error("GET request failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("GET request failed", e);
        }
    }
    
    /**
     * Performs a POST request with JSON body.
     * 
     * @param endpoint API endpoint
     * @param body request body
     * @return response entity
     */
    protected org.springframework.http.ResponseEntity<String> performPost(String endpoint, Object body) {
        try {
            String url = baseUrl + endpoint;
            String jsonBody = objectMapper.writeValueAsString(body);
            org.springframework.http.HttpHeaders headers = createHeaders();
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(jsonBody, headers);
            
            log.debug("Performing POST request to: {} with body: {}", url, jsonBody);
            return restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, String.class);
            
        } catch (Exception e) {
            log.error("POST request failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("POST request failed", e);
        }
    }
    
    /**
     * Performs a PUT request with JSON body.
     * 
     * @param endpoint API endpoint
     * @param body request body
     * @return response entity
     */
    protected org.springframework.http.ResponseEntity<String> performPut(String endpoint, Object body) {
        try {
            String url = baseUrl + endpoint;
            String jsonBody = objectMapper.writeValueAsString(body);
            org.springframework.http.HttpHeaders headers = createHeaders();
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(jsonBody, headers);
            
            log.debug("Performing PUT request to: {} with body: {}", url, jsonBody);
            return restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, entity, String.class);
            
        } catch (Exception e) {
            log.error("PUT request failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("PUT request failed", e);
        }
    }
    
    /**
     * Performs a DELETE request.
     * 
     * @param endpoint API endpoint
     * @return response entity
     */
    protected org.springframework.http.ResponseEntity<String> performDelete(String endpoint) {
        try {
            String url = baseUrl + endpoint;
            org.springframework.http.HttpHeaders headers = createHeaders();
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            log.debug("Performing DELETE request to: {}", url);
            return restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, entity, String.class);
            
        } catch (Exception e) {
            log.error("DELETE request failed for endpoint: {}", endpoint, e);
            throw new RuntimeException("DELETE request failed", e);
        }
    }
    
    /**
     * Database Utilities
     */
    
    /**
     * Executes SQL script for test data setup.
     * 
     * @param sqlScript SQL script content
     */
    protected void executeSqlScript(String sqlScript) {
        if (dataSource == null) {
            log.warn("DataSource not available, skipping SQL script execution");
            return;
        }
        
        try {
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = 
                new org.springframework.jdbc.core.JdbcTemplate(dataSource);
            
            String[] statements = sqlScript.split(";");
            for (String statement : statements) {
                if (statement.trim().length() > 0) {
                    jdbcTemplate.execute(statement.trim());
                }
            }
            
            log.debug("SQL script executed successfully");
            
        } catch (Exception e) {
            log.error("Failed to execute SQL script", e);
            throw new RuntimeException("SQL script execution failed", e);
        }
    }
    
    /**
     * Truncates specified tables for test cleanup.
     * 
     * @param tableNames names of tables to truncate
     */
    protected void truncateTables(String... tableNames) {
        if (dataSource == null) {
            log.warn("DataSource not available, skipping table truncation");
            return;
        }
        
        try {
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = 
                new org.springframework.jdbc.core.JdbcTemplate(dataSource);
            
            for (String tableName : tableNames) {
                jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            }
            
            log.debug("Tables truncated: {}", String.join(", ", tableNames));
            
        } catch (Exception e) {
            log.error("Failed to truncate tables", e);
            throw new RuntimeException("Table truncation failed", e);
        }
    }
    
    /**
     * Assertion Utilities
     */
    
    /**
     * Asserts that response contains expected JSON structure.
     * 
     * @param response HTTP response
     * @param expectedKeys expected JSON keys
     */
    protected void assertJsonContainsKeys(org.springframework.http.ResponseEntity<String> response, String... expectedKeys) {
        try {
            String responseBody = response.getBody();
            org.springframework.util.Assert.notNull(responseBody, "Response body should not be null");
            
            com.jayway.jsonpath.DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(responseBody);
            
            for (String key : expectedKeys) {
                Object value = jsonContext.read("$." + key);
                org.springframework.util.Assert.notNull(value, "Expected key '" + key + "' not found in response");
            }
            
            log.debug("JSON response validation passed for keys: {}", String.join(", ", expectedKeys));
            
        } catch (Exception e) {
            log.error("JSON assertion failed", e);
            throw new AssertionError("JSON assertion failed: " + e.getMessage());
        }
    }
    
    /**
     * Asserts that response has expected status code.
     * 
     * @param response HTTP response
     * @param expectedStatus expected HTTP status
     */
    protected void assertStatus(org.springframework.http.ResponseEntity<String> response, 
                              org.springframework.http.HttpStatus expectedStatus) {
        org.springframework.util.Assert.isTrue(
            response.getStatusCode() == expectedStatus,
            "Expected status " + expectedStatus + " but got " + response.getStatusCode()
        );
        log.debug("Status assertion passed: {}", expectedStatus);
    }
    
    // Private helper methods
    
    private String buildUrl(String endpoint, Map<String, Object> params) {
        StringBuilder url = new StringBuilder(baseUrl + endpoint);
        
        if (!params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> 
                url.append(key).append("=").append(value).append("&")
            );
            // Remove trailing &
            url.setLength(url.length() - 1);
        }
        
        return url.toString();
    }
    
    private org.springframework.http.HttpHeaders createHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        defaultHeaders.forEach(headers::add);
        return headers;
    }
    
    private String generateTestJwtToken(Map<String, Object> user) {
        // Simplified JWT token generation for testing
        // In production, this would use proper JWT libraries
        return "test.jwt.token." + user.get("email") + "." + user.get("role");
    }
}