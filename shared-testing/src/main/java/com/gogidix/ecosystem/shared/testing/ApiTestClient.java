package com.gogidix.ecosystem.shared.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive API test client for the Exalt Social E-commerce Ecosystem.
 * Provides fluent interface for API testing with validation, authentication, and utilities.
 * 
 * <p>This client provides:</p>
 * <ul>
 *   <li>Fluent API for REST operations (GET, POST, PUT, DELETE)</li>
 *   <li>Authentication management for different user roles</li>
 *   <li>Request/response validation with detailed assertions</li>
 *   <li>JSON schema validation for API contracts</li>
 *   <li>Performance testing with response time validation</li>
 *   <li>Error handling and detailed logging</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * {@code
 * // Basic API testing
 * ApiTestClient.create("http://localhost:8080")
 *     .authenticateAsCustomer()
 *     .get("/api/orders")
 *     .expectStatus(200)
 *     .expectJsonPath("$.data", hasSize(greaterThan(0)))
 *     .validate();
 * 
 * // POST with validation
 * Map<String, Object> order = TestDataFactory.OrderData.createOrder();
 * ApiTestClient.create("http://localhost:8080")
 *     .authenticateAsVendor()
 *     .post("/api/orders", order)
 *     .expectStatus(201)
 *     .expectJsonPath("$.id", notNullValue())
 *     .expectResponseTime(lessThan(2000L))
 *     .validate();
 * 
 * // Schema validation
 * ApiTestClient.create("http://localhost:8080")
 *     .get("/api/products/123")
 *     .expectStatus(200)
 *     .expectJsonSchema("product-schema.json")
 *     .validate();
 * }
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
public class ApiTestClient {
    
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final Map<String, Object> headers;
    private final Map<String, Object> queryParams;
    
    private RequestSpecification requestSpec;
    private Response lastResponse;
    private final Map<String, Object> expectations;
    
    private ApiTestClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.expectations = new HashMap<>();
        
        // Configure RestAssured
        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Set default headers
        this.headers.put("Content-Type", "application/json");
        this.headers.put("Accept", "application/json");
        
        log.debug("API test client created for base URL: {}", baseUrl);
    }
    
    /**
     * Creates a new API test client instance.
     * 
     * @param baseUrl base URL for the API
     * @return new ApiTestClient instance
     */
    public static ApiTestClient create(String baseUrl) {
        return new ApiTestClient(baseUrl);
    }
    
    /**
     * Authentication Methods
     */
    
    /**
     * Authenticates as a customer user.
     * 
     * @return this client for method chaining
     */
    public ApiTestClient authenticateAsCustomer() {
        return authenticateAs("customer@test.com", "CUSTOMER");
    }
    
    /**
     * Authenticates as a vendor user.
     * 
     * @return this client for method chaining
     */
    public ApiTestClient authenticateAsVendor() {
        return authenticateAs("vendor@test.com", "VENDOR");
    }
    
    /**
     * Authenticates as an admin user.
     * 
     * @return this client for method chaining
     */
    public ApiTestClient authenticateAsAdmin() {
        return authenticateAs("admin@test.com", "ADMIN");
    }
    
    /**
     * Authenticates with specified credentials.
     * 
     * @param email user email
     * @param role user role
     * @return this client for method chaining
     */
    public ApiTestClient authenticateAs(String email, String role) {
        try {
            // Generate test JWT token (simplified for testing)
            String token = generateTestJwtToken(email, role);
            this.headers.put("Authorization", "Bearer " + token);
            
            log.debug("Authenticated as {} with role: {}", email, role);
            return this;
            
        } catch (Exception e) {
            log.error("Authentication failed for {} with role: {}", email, role, e);
            throw new RuntimeException("Authentication failed", e);
        }
    }
    
    /**
     * Sets a custom authorization header.
     * 
     * @param token authorization token
     * @return this client for method chaining
     */
    public ApiTestClient withAuthToken(String token) {
        this.headers.put("Authorization", "Bearer " + token);
        log.debug("Custom auth token set");
        return this;
    }
    
    /**
     * Header and Parameter Methods
     */
    
    /**
     * Adds a custom header.
     * 
     * @param name header name
     * @param value header value
     * @return this client for method chaining
     */
    public ApiTestClient withHeader(String name, Object value) {
        this.headers.put(name, value);
        log.debug("Header added: {} = {}", name, value);
        return this;
    }
    
    /**
     * Adds multiple headers.
     * 
     * @param headers map of headers
     * @return this client for method chaining
     */
    public ApiTestClient withHeaders(Map<String, Object> headers) {
        this.headers.putAll(headers);
        log.debug("Multiple headers added: {}", headers.keySet());
        return this;
    }
    
    /**
     * Adds a query parameter.
     * 
     * @param name parameter name
     * @param value parameter value
     * @return this client for method chaining
     */
    public ApiTestClient withQueryParam(String name, Object value) {
        this.queryParams.put(name, value);
        log.debug("Query parameter added: {} = {}", name, value);
        return this;
    }
    
    /**
     * Adds multiple query parameters.
     * 
     * @param params map of parameters
     * @return this client for method chaining
     */
    public ApiTestClient withQueryParams(Map<String, Object> params) {
        this.queryParams.putAll(params);
        log.debug("Multiple query parameters added: {}", params.keySet());
        return this;
    }
    
    /**
     * HTTP Methods
     */
    
    /**
     * Performs a GET request.
     * 
     * @param endpoint API endpoint
     * @return this client for method chaining
     */
    public ApiTestClient get(String endpoint) {
        this.requestSpec = buildRequestSpec();
        
        log.info("Performing GET request to: {}", endpoint);
        this.lastResponse = requestSpec.when().get(endpoint);
        
        logResponse();
        return this;
    }
    
    /**
     * Performs a POST request with JSON body.
     * 
     * @param endpoint API endpoint
     * @param body request body
     * @return this client for method chaining
     */
    public ApiTestClient post(String endpoint, Object body) {
        this.requestSpec = buildRequestSpec();
        
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            log.info("Performing POST request to: {} with body: {}", endpoint, jsonBody);
            
            this.lastResponse = requestSpec
                .body(jsonBody)
                .when().post(endpoint);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request body", e);
            throw new RuntimeException("Request body serialization failed", e);
        }
        
        logResponse();
        return this;
    }
    
    /**
     * Performs a PUT request with JSON body.
     * 
     * @param endpoint API endpoint
     * @param body request body
     * @return this client for method chaining
     */
    public ApiTestClient put(String endpoint, Object body) {
        this.requestSpec = buildRequestSpec();
        
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            log.info("Performing PUT request to: {} with body: {}", endpoint, jsonBody);
            
            this.lastResponse = requestSpec
                .body(jsonBody)
                .when().put(endpoint);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request body", e);
            throw new RuntimeException("Request body serialization failed", e);
        }
        
        logResponse();
        return this;
    }
    
    /**
     * Performs a PATCH request with JSON body.
     * 
     * @param endpoint API endpoint
     * @param body request body
     * @return this client for method chaining
     */
    public ApiTestClient patch(String endpoint, Object body) {
        this.requestSpec = buildRequestSpec();
        
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            log.info("Performing PATCH request to: {} with body: {}", endpoint, jsonBody);
            
            this.lastResponse = requestSpec
                .body(jsonBody)
                .when().patch(endpoint);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request body", e);
            throw new RuntimeException("Request body serialization failed", e);
        }
        
        logResponse();
        return this;
    }
    
    /**
     * Performs a DELETE request.
     * 
     * @param endpoint API endpoint
     * @return this client for method chaining
     */
    public ApiTestClient delete(String endpoint) {
        this.requestSpec = buildRequestSpec();
        
        log.info("Performing DELETE request to: {}", endpoint);
        this.lastResponse = requestSpec.when().delete(endpoint);
        
        logResponse();
        return this;
    }
    
    /**
     * Expectation Methods
     */
    
    /**
     * Expects specific HTTP status code.
     * 
     * @param statusCode expected status code
     * @return this client for method chaining
     */
    public ApiTestClient expectStatus(int statusCode) {
        this.expectations.put("statusCode", statusCode);
        log.debug("Added status expectation: {}", statusCode);
        return this;
    }
    
    /**
     * Expects specific HTTP status.
     * 
     * @param status expected HTTP status
     * @return this client for method chaining
     */
    public ApiTestClient expectStatus(HttpStatus status) {
        return expectStatus(status.value());
    }
    
    /**
     * Expects JSON path to match a condition.
     * 
     * @param jsonPath JSON path expression
     * @param matcher Hamcrest matcher
     * @return this client for method chaining
     */
    public ApiTestClient expectJsonPath(String jsonPath, org.hamcrest.Matcher<?> matcher) {
        this.expectations.put("jsonPath_" + jsonPath, matcher);
        log.debug("Added JSON path expectation: {} with matcher: {}", jsonPath, matcher);
        return this;
    }
    
    /**
     * Expects response to match JSON schema.
     * 
     * @param schemaPath path to JSON schema file
     * @return this client for method chaining
     */
    public ApiTestClient expectJsonSchema(String schemaPath) {
        this.expectations.put("jsonSchema", schemaPath);
        log.debug("Added JSON schema expectation: {}", schemaPath);
        return this;
    }
    
    /**
     * Expects response time to be within specified limit.
     * 
     * @param timeInMillis maximum response time in milliseconds
     * @return this client for method chaining
     */
    public ApiTestClient expectResponseTime(long timeInMillis) {
        this.expectations.put("responseTime", timeInMillis);
        log.debug("Added response time expectation: {} ms", timeInMillis);
        return this;
    }
    
    /**
     * Expects response time to match a condition.
     * 
     * @param matcher Hamcrest matcher for response time
     * @return this client for method chaining
     */
    public ApiTestClient expectResponseTime(org.hamcrest.Matcher<Long> matcher) {
        this.expectations.put("responseTimeMatcher", matcher);
        log.debug("Added response time matcher expectation: {}", matcher);
        return this;
    }
    
    /**
     * Expects specific content type.
     * 
     * @param contentType expected content type
     * @return this client for method chaining
     */
    public ApiTestClient expectContentType(String contentType) {
        this.expectations.put("contentType", contentType);
        log.debug("Added content type expectation: {}", contentType);
        return this;
    }
    
    /**
     * Expects header to have specific value.
     * 
     * @param headerName header name
     * @param expectedValue expected header value
     * @return this client for method chaining
     */
    public ApiTestClient expectHeader(String headerName, String expectedValue) {
        this.expectations.put("header_" + headerName, expectedValue);
        log.debug("Added header expectation: {} = {}", headerName, expectedValue);
        return this;
    }
    
    /**
     * Validation and Execution
     */
    
    /**
     * Validates all expectations against the last response.
     * 
     * @return this client for method chaining
     */
    public ApiTestClient validate() {
        if (lastResponse == null) {
            throw new IllegalStateException("No response available for validation. Make an HTTP request first.");
        }
        
        log.info("Validating response for expectations: {}", expectations.keySet());
        
        // Validate status code
        if (expectations.containsKey("statusCode")) {
            int expectedStatus = (Integer) expectations.get("statusCode");
            lastResponse.then().statusCode(expectedStatus);
            log.debug("Status code validation passed: {}", expectedStatus);
        }
        
        // Validate JSON paths
        expectations.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("jsonPath_"))
            .forEach(entry -> {
                String jsonPath = entry.getKey().substring("jsonPath_".length());
                org.hamcrest.Matcher<?> matcher = (org.hamcrest.Matcher<?>) entry.getValue();
                lastResponse.then().body(jsonPath, matcher);
                log.debug("JSON path validation passed: {} with matcher: {}", jsonPath, matcher);
            });
        
        // Validate JSON schema
        if (expectations.containsKey("jsonSchema")) {
            String schemaPath = (String) expectations.get("jsonSchema");
            lastResponse.then().body(io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
            log.debug("JSON schema validation passed: {}", schemaPath);
        }
        
        // Validate response time
        if (expectations.containsKey("responseTime")) {
            long maxTime = (Long) expectations.get("responseTime");
            lastResponse.then().time(lessThan(maxTime), TimeUnit.MILLISECONDS);
            log.debug("Response time validation passed: < {} ms", maxTime);
        }
        
        if (expectations.containsKey("responseTimeMatcher")) {
            org.hamcrest.Matcher<Long> matcher = (org.hamcrest.Matcher<Long>) expectations.get("responseTimeMatcher");
            lastResponse.then().time(matcher, TimeUnit.MILLISECONDS);
            log.debug("Response time matcher validation passed: {}", matcher);
        }
        
        // Validate content type
        if (expectations.containsKey("contentType")) {
            String expectedContentType = (String) expectations.get("contentType");
            lastResponse.then().contentType(expectedContentType);
            log.debug("Content type validation passed: {}", expectedContentType);
        }
        
        // Validate headers
        expectations.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("header_"))
            .forEach(entry -> {
                String headerName = entry.getKey().substring("header_".length());
                String expectedValue = (String) entry.getValue();
                lastResponse.then().header(headerName, expectedValue);
                log.debug("Header validation passed: {} = {}", headerName, expectedValue);
            });
        
        log.info("All validations passed successfully");
        return this;
    }
    
    /**
     * Gets the last response for custom validations.
     * 
     * @return last HTTP response
     */
    public Response getResponse() {
        return lastResponse;
    }
    
    /**
     * Gets response body as string.
     * 
     * @return response body
     */
    public String getResponseBody() {
        return lastResponse != null ? lastResponse.getBody().asString() : null;
    }
    
    /**
     * Gets response status code.
     * 
     * @return HTTP status code
     */
    public int getStatusCode() {
        return lastResponse != null ? lastResponse.getStatusCode() : -1;
    }
    
    /**
     * Extracts value from JSON response using JSON path.
     * 
     * @param jsonPath JSON path expression
     * @param <T> expected return type
     * @return extracted value
     */
    public <T> T extractFromResponse(String jsonPath) {
        if (lastResponse == null) {
            throw new IllegalStateException("No response available for extraction");
        }
        
        return lastResponse.jsonPath().get(jsonPath);
    }
    
    // Private helper methods
    
    private RequestSpecification buildRequestSpec() {
        RequestSpecification spec = given()
            .contentType(ContentType.JSON)
            .headers(headers)
            .queryParams(queryParams);
        
        log.debug("Request specification built with headers: {} and query params: {}", 
                 headers.keySet(), queryParams.keySet());
        
        return spec;
    }
    
    private void logResponse() {
        if (lastResponse != null) {
            log.info("Response received - Status: {}, Time: {} ms", 
                    lastResponse.getStatusCode(), lastResponse.getTime());
            log.debug("Response body: {}", lastResponse.getBody().asString());
        }
    }
    
    private String generateTestJwtToken(String email, String role) {
        // Simplified JWT token generation for testing
        // In production, this would use proper JWT libraries
        return "test.jwt.token." + email + "." + role + "." + System.currentTimeMillis();
    }
}