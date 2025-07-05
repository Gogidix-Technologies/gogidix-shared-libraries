package com.gogidix.ecosystem.shared.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
// Mockito imports moved to method-level to avoid compile-time dependencies
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Comprehensive mocking utilities for the Exalt Social E-commerce Ecosystem.
 * Provides advanced mocking capabilities for services, external APIs, and complex scenarios.
 * 
 * <p>This utility class provides:</p>
 * <ul>
 *   <li>WireMock integration for external API mocking</li>
 *   <li>Mockito utilities for service mocking</li>
 *   <li>Realistic test data generation for mocked responses</li>
 *   <li>Error scenario simulation</li>
 *   <li>Performance testing with latency simulation</li>
 *   <li>Authentication and security mocking</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>
 * {@code
 * // External API mocking
 * WireMockServer mockServer = MockUtils.createWireMockServer();
 * MockUtils.mockPaymentGatewaySuccess(mockServer, "payment123", "100.00");
 * 
 * // Service mocking
 * PaymentService mockPaymentService = MockUtils.createMockService(PaymentService.class);
 * MockUtils.whenThenReturn(mockPaymentService::processPayment, "SUCCESS");
 * 
 * // Error scenario testing
 * MockUtils.mockPaymentGatewayTimeout(mockServer);
 * MockUtils.mockDatabaseError(mockService);
 * }
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
public final class MockUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DEFAULT_WIREMOCK_PORT = 8999;
    private static final java.util.Random random = new java.util.Random();
    
    private MockUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * WireMock Server Management
     */
    
    /**
     * Creates and starts a WireMock server with default configuration.
     * 
     * @return started WireMock server
     */
    public static WireMockServer createWireMockServer() {
        return createWireMockServer(DEFAULT_WIREMOCK_PORT);
    }
    
    /**
     * Creates and starts a WireMock server on specified port.
     * 
     * @param port server port
     * @return started WireMock server
     */
    public static WireMockServer createWireMockServer(int port) {
        WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                .port(port)
                .enableBrowserProxying(true)
        );
        
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
        
        log.info("WireMock server started on port: {}", port);
        return wireMockServer;
    }
    
    /**
     * Stops and cleans up WireMock server.
     * 
     * @param server WireMock server to stop
     */
    public static void stopWireMockServer(WireMockServer server) {
        if (server != null && server.isRunning()) {
            server.stop();
            log.info("WireMock server stopped");
        }
    }
    
    /**
     * Resets all WireMock stubs and request history.
     * 
     * @param server WireMock server to reset
     */
    public static void resetWireMockServer(WireMockServer server) {
        if (server != null && server.isRunning()) {
            server.resetAll();
            log.debug("WireMock server reset");
        }
    }
    
    /**
     * Payment Gateway Mocking
     */
    
    /**
     * Mocks successful payment processing.
     * 
     * @param server WireMock server
     * @param transactionId transaction ID
     * @param amount payment amount
     */
    public static void mockPaymentGatewaySuccess(WireMockServer server, String transactionId, String amount) {
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("amount", amount);
        response.put("status", "SUCCESS");
        response.put("message", "Payment processed successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        server.stubFor(post(urlEqualTo("/api/payments/process"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked successful payment: {} for amount: {}", transactionId, amount);
    }
    
    /**
     * Mocks failed payment processing.
     * 
     * @param server WireMock server
     * @param transactionId transaction ID
     * @param errorCode error code
     * @param errorMessage error message
     */
    public static void mockPaymentGatewayFailure(WireMockServer server, String transactionId, 
                                               String errorCode, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("status", "FAILED");
        response.put("errorCode", errorCode);
        response.put("errorMessage", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        
        server.stubFor(post(urlEqualTo("/api/payments/process"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked failed payment: {} with error: {}", transactionId, errorMessage);
    }
    
    /**
     * Mocks payment gateway timeout.
     * 
     * @param server WireMock server
     */
    public static void mockPaymentGatewayTimeout(WireMockServer server) {
        server.stubFor(post(urlEqualTo("/api/payments/process"))
            .willReturn(aResponse()
                .withStatus(500)
                .withFixedDelay(30000) // 30 second delay
                .withBody("Gateway timeout")));
        
        log.debug("Mocked payment gateway timeout");
    }
    
    /**
     * Courier Service Mocking
     */
    
    /**
     * Mocks successful shipment creation.
     * 
     * @param server WireMock server
     * @param trackingNumber tracking number
     * @param estimatedDelivery estimated delivery date
     */
    public static void mockCourierShipmentSuccess(WireMockServer server, String trackingNumber, String estimatedDelivery) {
        Map<String, Object> response = new HashMap<>();
        response.put("trackingNumber", trackingNumber);
        response.put("status", "CREATED");
        response.put("estimatedDelivery", estimatedDelivery);
        response.put("carrier", "TEST_COURIER");
        response.put("timestamp", System.currentTimeMillis());
        
        server.stubFor(post(urlEqualTo("/api/shipments/create"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked successful shipment creation: {}", trackingNumber);
    }
    
    /**
     * Mocks shipment tracking information.
     * 
     * @param server WireMock server
     * @param trackingNumber tracking number
     * @param currentStatus current shipment status
     * @param location current location
     */
    public static void mockCourierTracking(WireMockServer server, String trackingNumber, 
                                         String currentStatus, String location) {
        Map<String, Object> response = new HashMap<>();
        response.put("trackingNumber", trackingNumber);
        response.put("status", currentStatus);
        response.put("currentLocation", location);
        response.put("lastUpdated", System.currentTimeMillis());
        response.put("events", createTrackingEvents());
        
        server.stubFor(get(urlEqualTo("/api/shipments/track/" + trackingNumber))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked shipment tracking: {} - {}", trackingNumber, currentStatus);
    }
    
    /**
     * External Service Mocking
     */
    
    /**
     * Mocks authentication service responses.
     * 
     * @param server WireMock server
     * @param email user email
     * @param role user role
     * @param token JWT token
     */
    public static void mockAuthenticationSuccess(WireMockServer server, String email, String role, String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("role", role);
        response.put("expiresIn", 3600);
        response.put("tokenType", "Bearer");
        
        server.stubFor(post(urlEqualTo("/api/auth/login"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked authentication success for: {} with role: {}", email, role);
    }
    
    /**
     * Mocks email service responses.
     * 
     * @param server WireMock server
     * @param emailId email ID
     */
    public static void mockEmailServiceSuccess(WireMockServer server, String emailId) {
        Map<String, Object> response = new HashMap<>();
        response.put("emailId", emailId);
        response.put("status", "SENT");
        response.put("timestamp", System.currentTimeMillis());
        
        server.stubFor(post(urlEqualTo("/api/email/send"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked email service success: {}", emailId);
    }
    
    /**
     * Mockito Service Utilities
     * Note: These utilities require Mockito to be available at test runtime.
     */
    
    /**
     * Creates a mock instance of specified class using reflection.
     * Note: Requires Mockito to be available at runtime.
     * 
     * @param clazz class to mock
     * @param <T> class type
     * @return mocked instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createMockService(Class<T> clazz) {
        try {
            Class<?> mockitoClass = Class.forName("org.mockito.Mockito");
            java.lang.reflect.Method mockMethod = mockitoClass.getMethod("mock", Class.class);
            T mock = (T) mockMethod.invoke(null, clazz);
            log.debug("Created mock service for: {}", clazz.getSimpleName());
            return mock;
        } catch (Exception e) {
            log.error("Failed to create mock service - Mockito may not be available", e);
            throw new RuntimeException("Mock creation failed - ensure Mockito is on the classpath", e);
        }
    }
    
    /**
     * Mocks method to return specific value using reflection.
     * Note: Requires Mockito to be available at runtime.
     * 
     * @param mock mock object
     * @param returnValue value to return
     * @param <T> return type
     */
    public static <T> void whenThenReturn(T mock, T returnValue) {
        try {
            Class<?> mockitoClass = Class.forName("org.mockito.Mockito");
            java.lang.reflect.Method whenMethod = mockitoClass.getMethod("when", Object.class);
            Object stubbing = whenMethod.invoke(null, mock);
            
            java.lang.reflect.Method thenReturnMethod = stubbing.getClass().getMethod("thenReturn", Object.class);
            thenReturnMethod.invoke(stubbing, returnValue);
            
            log.debug("Mocked method to return: {}", returnValue);
        } catch (Exception e) {
            log.error("Failed to configure mock - Mockito may not be available", e);
            throw new RuntimeException("Mock configuration failed - ensure Mockito is on the classpath", e);
        }
    }
    
    /**
     * Mocks method to throw exception using reflection.
     * Note: Requires Mockito to be available at runtime.
     * 
     * @param mock mock object
     * @param exception exception to throw
     * @param <T> mock type
     */
    public static <T> void whenThenThrow(T mock, Exception exception) {
        try {
            Class<?> mockitoClass = Class.forName("org.mockito.Mockito");
            java.lang.reflect.Method whenMethod = mockitoClass.getMethod("when", Object.class);
            Object stubbing = whenMethod.invoke(null, mock);
            
            java.lang.reflect.Method thenThrowMethod = stubbing.getClass().getMethod("thenThrow", Throwable.class);
            thenThrowMethod.invoke(stubbing, exception);
            
            log.debug("Mocked method to throw: {}", exception.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to configure mock exception - Mockito may not be available", e);
            throw new RuntimeException("Mock exception configuration failed - ensure Mockito is on the classpath", e);
        }
    }
    
    /**
     * Error Scenario Mocking
     */
    
    /**
     * Mocks database connection errors.
     * 
     * @param server WireMock server
     */
    public static void mockDatabaseError(WireMockServer server) {
        server.stubFor(any(urlMatching("/api/.*"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Database connection failed")));
        
        log.debug("Mocked database error scenario");
    }
    
    /**
     * Mocks network timeout scenarios.
     * 
     * @param server WireMock server
     * @param delayMs delay in milliseconds
     */
    public static void mockNetworkTimeout(WireMockServer server, int delayMs) {
        server.stubFor(any(urlMatching("/api/.*"))
            .willReturn(aResponse()
                .withStatus(408)
                .withFixedDelay(delayMs)
                .withBody("Request timeout")));
        
        log.debug("Mocked network timeout: {} ms", delayMs);
    }
    
    /**
     * Mocks rate limiting scenarios.
     * 
     * @param server WireMock server
     */
    public static void mockRateLimiting(WireMockServer server) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Rate limit exceeded");
        response.put("retryAfter", 60);
        response.put("timestamp", System.currentTimeMillis());
        
        server.stubFor(any(urlMatching("/api/.*"))
            .willReturn(aResponse()
                .withStatus(429)
                .withHeader("Retry-After", "60")
                .withHeader("Content-Type", "application/json")
                .withBody(toJson(response))));
        
        log.debug("Mocked rate limiting scenario");
    }
    
    /**
     * Performance Testing Utilities
     */
    
    /**
     * Mocks slow response for performance testing.
     * 
     * @param server WireMock server
     * @param endpoint API endpoint
     * @param delayMs response delay in milliseconds
     */
    public static void mockSlowResponse(WireMockServer server, String endpoint, int delayMs) {
        server.stubFor(get(urlEqualTo(endpoint))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(delayMs)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Slow response\"}")));
        
        log.debug("Mocked slow response for {}: {} ms", endpoint, delayMs);
    }
    
    /**
     * Mocks varying response times for load testing.
     * 
     * @param server WireMock server
     * @param endpoint API endpoint
     * @param minDelayMs minimum delay
     * @param maxDelayMs maximum delay
     */
    public static void mockVariableResponseTime(WireMockServer server, String endpoint, int minDelayMs, int maxDelayMs) {
        server.stubFor(get(urlEqualTo(endpoint))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(minDelayMs + random.nextInt(maxDelayMs - minDelayMs))
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Variable response time\"}")));
        
        log.debug("Mocked variable response time for {}: {}-{} ms", endpoint, minDelayMs, maxDelayMs);
    }
    
    /**
     * Verification Utilities
     */
    
    /**
     * Verifies that an endpoint was called specific number of times.
     * 
     * @param server WireMock server
     * @param endpoint API endpoint
     * @param expectedCount expected call count
     */
    public static void verifyEndpointCalled(WireMockServer server, String endpoint, int expectedCount) {
        server.verify(expectedCount, postRequestedFor(urlEqualTo(endpoint)));
        log.debug("Verified endpoint {} called {} times", endpoint, expectedCount);
    }
    
    /**
     * Verifies that request contained specific header.
     * 
     * @param server WireMock server
     * @param endpoint API endpoint
     * @param headerName header name
     * @param headerValue header value
     */
    public static void verifyRequestHeader(WireMockServer server, String endpoint, String headerName, String headerValue) {
        server.verify(postRequestedFor(urlEqualTo(endpoint))
            .withHeader(headerName, equalTo(headerValue)));
        log.debug("Verified request header: {} = {}", headerName, headerValue);
    }
    
    /**
     * Verifies that request body contained specific content.
     * 
     * @param server WireMock server
     * @param endpoint API endpoint
     * @param bodyContent expected body content
     */
    public static void verifyRequestBody(WireMockServer server, String endpoint, String bodyContent) {
        server.verify(postRequestedFor(urlEqualTo(endpoint))
            .withRequestBody(containing(bodyContent)));
        log.debug("Verified request body contains: {}", bodyContent);
    }
    
    // Private helper methods
    
    private static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }
    
    private static Object[] createTrackingEvents() {
        return new Object[]{
            Map.of("status", "PICKED_UP", "timestamp", System.currentTimeMillis() - 86400000, "location", "Origin"),
            Map.of("status", "IN_TRANSIT", "timestamp", System.currentTimeMillis() - 43200000, "location", "Hub"),
            Map.of("status", "OUT_FOR_DELIVERY", "timestamp", System.currentTimeMillis() - 3600000, "location", "Local Depot")
        };
    }
}