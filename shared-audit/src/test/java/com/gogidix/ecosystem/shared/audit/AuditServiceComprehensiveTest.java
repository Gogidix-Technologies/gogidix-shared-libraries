package com.gogidix.ecosystem.shared.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for AuditService with compliance scenarios.
 * Tests all audit logging methods, async operations, search, statistics, and export functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Comprehensive Tests")
class AuditServiceComprehensiveTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditService auditService;
    private AuditEvent testAuditEvent;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditRepository);
        
        // Set test properties
        ReflectionTestUtils.setField(auditService, "serviceName", "test-service");
        ReflectionTestUtils.setField(auditService, "asyncEnabled", true);
        ReflectionTestUtils.setField(auditService, "defaultRetentionDays", 2555);

        testAuditEvent = AuditEvent.builder()
            .id(UUID.randomUUID())
            .action(AuditEvent.AuditAction.CREATE)
            .serviceName("test-service")
            .timestamp(LocalDateTime.now())
            .severity(AuditEvent.AuditSeverity.INFO)
            .success(true)
            .build();
    }

    @Nested
    @DisplayName("Basic Audit Logging Tests")
    class BasicAuditLoggingTests {

        @Test
        @DisplayName("Should audit event synchronously with enrichment")
        void shouldAuditEventSynchronouslyWithEnrichment() {
            // Given
            AuditEvent incompleteEvent = AuditEvent.builder()
                .action(AuditEvent.AuditAction.CREATE)
                .build();

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(incompleteEvent);

            // When
            auditService.audit(incompleteEvent);

            // Then
            verify(auditRepository).save(argThat(event -> 
                event.getServiceName().equals("test-service") &&
                event.getTimestamp() != null &&
                event.getId() != null &&
                event.getRetentionDays() == 2555
            ));
        }

        @Test
        @DisplayName("Should audit event asynchronously")
        void shouldAuditEventAsynchronously() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            CompletableFuture<Void> future = auditService.auditAsync(testAuditEvent);

            // Then
            assertThat(future).isNotNull();
            // Give time for async execution
            assertThatCode(() -> future.get(java.util.concurrent.TimeUnit.SECONDS.toMillis(1), java.util.concurrent.TimeUnit.MILLISECONDS))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle audit failures gracefully without throwing")
        void shouldHandleAuditFailuresGracefullyWithoutThrowing() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenThrow(new RuntimeException("Database error"));

            // When/Then - Should not throw exception
            assertThatCode(() -> auditService.audit(testAuditEvent))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should validate audit event before saving")
        void shouldValidateAuditEventBeforeSaving() {
            // Given
            AuditEvent invalidEvent = AuditEvent.builder().build(); // Missing required fields

            // When/Then - Should not throw but log error
            assertThatCode(() -> auditService.audit(invalidEvent))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Simple Audit Methods Tests")
    class SimpleAuditMethodsTests {

        @Test
        @DisplayName("Should audit simple action with async enabled")
        void shouldAuditSimpleActionWithAsyncEnabled() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.audit(AuditEvent.AuditAction.UPDATE, "Product", "product-123", "user-456");

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.UPDATE &&
                event.getResourceType().equals("Product") &&
                event.getResourceId().equals("product-123") &&
                event.getUserId().equals("user-456") &&
                event.getServiceName().equals("test-service")
            ));
        }

        @Test
        @DisplayName("Should audit simple action asynchronously")
        void shouldAuditSimpleActionAsynchronously() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            CompletableFuture<Void> future = auditService.auditAsync(
                AuditEvent.AuditAction.DELETE, "Order", "order-789", "admin-user");

            // Then
            assertThat(future).isNotNull();
            assertThatCode(() -> future.get(1000, java.util.concurrent.TimeUnit.MILLISECONDS))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should audit action with description and metadata")
        void shouldAuditActionWithDescriptionAndMetadata() {
            // Given
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("requestId", UUID.randomUUID());
            metadata.put("clientIp", "192.168.1.100");

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.audit(AuditEvent.AuditAction.READ, "User", "user-123", "viewer-456", 
                "User profile accessed", metadata);

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getDescription().equals("User profile accessed") &&
                event.getMetadata().equals(metadata)
            ));
        }
    }

    @Nested
    @DisplayName("Security Audit Tests")
    class SecurityAuditTests {

        @Test
        @DisplayName("Should audit security events synchronously")
        void shouldAuditSecurityEventsSynchronously() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditSecurity(AuditEvent.AuditAction.LOGIN, "user-123", "192.168.1.100", 
                "Mozilla/5.0", true, "Successful login");

            // Then
            verify(auditRepository).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.LOGIN &&
                event.getUserId().equals("user-123") &&
                event.getIpAddress().equals("192.168.1.100") &&
                event.getUserAgent().equals("Mozilla/5.0") &&
                event.getSuccess() &&
                event.getDescription().equals("Successful login") &&
                event.getCategory().equals("SECURITY") &&
                event.getSeverity() == AuditEvent.AuditSeverity.INFO
            ));
        }

        @Test
        @DisplayName("Should audit failed security events with warning severity")
        void shouldAuditFailedSecurityEventsWithWarningSeverity() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditSecurity(AuditEvent.AuditAction.AUTHENTICATE, "user-456", "192.168.1.200", 
                "Suspicious-Client/1.0", false, "Authentication failed - invalid credentials");

            // Then
            verify(auditRepository).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.AUTHENTICATE &&
                !event.getSuccess() &&
                event.getSeverity() == AuditEvent.AuditSeverity.WARN &&
                event.getDescription().equals("Authentication failed - invalid credentials")
            ));
        }
    }

    @Nested
    @DisplayName("Compliance Audit Tests")
    class ComplianceAuditTests {

        @Test
        @DisplayName("Should audit GDPR compliance events")
        void shouldAuditGdprComplianceEvents() {
            // Given
            Map<String, Object> complianceData = new HashMap<>();
            complianceData.put("dataSubject", "user-123");
            complianceData.put("legalBasis", "consent");
            complianceData.put("dataCategories", Arrays.asList("personal", "contact"));

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditCompliance(AuditEvent.AuditAction.EXPORT, "PersonalData", "user-123", 
                "data-controller", "GDPR", complianceData);

            // Then
            verify(auditRepository).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.EXPORT &&
                event.getResourceType().equals("PersonalData") &&
                event.getResourceId().equals("user-123") &&
                event.getUserId().equals("data-controller") &&
                event.getCategory().equals("COMPLIANCE") &&
                event.getMetadata().equals(complianceData) &&
                event.getComplianceFlags().get("GDPR").equals(true) &&
                event.getRetentionDays() == 2555
            ));
        }

        @Test
        @DisplayName("Should audit PCI DSS compliance events")
        void shouldAuditPciDssComplianceEvents() {
            // Given
            Map<String, Object> complianceData = new HashMap<>();
            complianceData.put("cardType", "VISA");
            complianceData.put("maskedPAN", "****1234");
            complianceData.put("encryptionStatus", "encrypted");

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditCompliance(AuditEvent.AuditAction.PROCESS, "PaymentData", "card-****1234", 
                "payment-processor", "PCI_DSS", complianceData);

            // Then
            verify(auditRepository).save(argThat(event ->
                event.getCategory().equals("COMPLIANCE") &&
                event.getComplianceFlags().get("PCI_DSS").equals(true) &&
                event.getMetadata().get("encryptionStatus").equals("encrypted")
            ));
        }
    }

    @Nested
    @DisplayName("Data Access Audit Tests")
    class DataAccessAuditTests {

        @Test
        @DisplayName("Should audit data access with classification")
        void shouldAuditDataAccessWithClassification() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditDataAccess("CustomerData", "customer-456", "analyst-123", 
                "EXPORT", "PII");

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.READ &&
                event.getResourceType().equals("CustomerData") &&
                event.getResourceId().equals("customer-456") &&
                event.getUserId().equals("analyst-123") &&
                event.getCategory().equals("DATA_ACCESS") &&
                event.getDescription().equals("EXPORT access to PII data") &&
                event.getMetadata().get("accessType").equals("EXPORT") &&
                event.getMetadata().get("dataClassification").equals("PII") &&
                event.getTags().get("dataClassification").equals("PII") &&
                event.getTags().get("accessType").equals("EXPORT")
            ));
        }
    }

    @Nested
    @DisplayName("API Request Audit Tests")
    class ApiRequestAuditTests {

        @Test
        @DisplayName("Should audit API requests with performance tagging")
        void shouldAuditApiRequestsWithPerformanceTagging() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When - Test slow request
            auditService.auditApiRequest("POST", "/api/v1/orders", "user-123", "192.168.1.100", 
                "curl/7.68.0", 201, 6000L);

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.ACCESS &&
                event.getHttpMethod().equals("POST") &&
                event.getRequestUri().equals("/api/v1/orders") &&
                event.getUserId().equals("user-123") &&
                event.getIpAddress().equals("192.168.1.100") &&
                event.getUserAgent().equals("curl/7.68.0") &&
                event.getHttpStatus() == 201 &&
                event.getDurationMs() == 6000L &&
                event.getSuccess() &&
                event.getCategory().equals("API_ACCESS") &&
                event.getDescription().equals("POST /api/v1/orders") &&
                event.getTags().get("performance").equals("slow") &&
                event.getSeverity() == AuditEvent.AuditSeverity.WARN
            ));
        }

        @Test
        @DisplayName("Should audit fast API requests appropriately")
        void shouldAuditFastApiRequestsAppropriately() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When - Test fast request
            auditService.auditApiRequest("GET", "/api/v1/health", "system", "127.0.0.1", 
                "health-check/1.0", 200, 50L);

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getDurationMs() == 50L &&
                event.getTags().get("performance").equals("fast") &&
                event.getSeverity() == AuditEvent.AuditSeverity.INFO
            ));
        }

        @Test
        @DisplayName("Should audit moderate API requests")
        void shouldAuditModerateApiRequests() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When - Test moderate request
            auditService.auditApiRequest("PUT", "/api/v1/users/123", "user-456", "10.0.0.1", 
                "MyApp/2.0", 200, 2000L);

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getDurationMs() == 2000L &&
                event.getTags().get("performance").equals("moderate") &&
                event.getSeverity() == AuditEvent.AuditSeverity.INFO
            ));
        }
    }

    @Nested
    @DisplayName("Business Process Audit Tests")
    class BusinessProcessAuditTests {

        @Test
        @DisplayName("Should audit business process steps")
        void shouldAuditBusinessProcessSteps() {
            // Given
            Map<String, Object> processData = new HashMap<>();
            processData.put("orderId", "order-123");
            processData.put("amount", 99.99);
            processData.put("currency", "USD");

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditBusinessProcess("Order Processing", "Payment Validation", 
                "order-123", "payment-service", processData, true);

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.PROCESS &&
                event.getResourceType().equals("BUSINESS_PROCESS") &&
                event.getResourceId().equals("order-123") &&
                event.getUserId().equals("payment-service") &&
                event.getSuccess() &&
                event.getCategory().equals("BUSINESS_PROCESS") &&
                event.getDescription().equals("Order Processing - Payment Validation") &&
                event.getMetadata().get("processName").equals("Order Processing") &&
                event.getMetadata().get("processStep").equals("Payment Validation") &&
                event.getMetadata().get("orderId").equals("order-123") &&
                event.getTags().get("processName").equals("Order Processing") &&
                event.getTags().get("processStep").equals("Payment Validation")
            ));
        }

        @Test
        @DisplayName("Should audit failed business process steps")
        void shouldAuditFailedBusinessProcessSteps() {
            // Given
            Map<String, Object> processData = new HashMap<>();
            processData.put("errorCode", "INSUFFICIENT_FUNDS");
            processData.put("attemptNumber", 1);

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditBusinessProcess("Payment Processing", "Charge Card", 
                "payment-456", "payment-gateway", processData, false);

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                !event.getSuccess() &&
                event.getMetadata().get("errorCode").equals("INSUFFICIENT_FUNDS")
            ));
        }
    }

    @Nested
    @DisplayName("Configuration Change Audit Tests")
    class ConfigurationChangeAuditTests {

        @Test
        @DisplayName("Should audit configuration changes synchronously")
        void shouldAuditConfigurationChangesSynchronously() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditConfigChange("Database", "connection.pool.size", 10, 20, 
                "admin-user", "Performance optimization");

            // Then
            verify(auditRepository).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.CONFIGURE &&
                event.getResourceType().equals("CONFIGURATION") &&
                event.getResourceId().equals("connection.pool.size") &&
                event.getUserId().equals("admin-user") &&
                event.getCategory().equals("CONFIGURATION") &&
                event.getDescription().equals("Configuration change: Database.connection.pool.size") &&
                event.getOldValues().get("connection.pool.size").equals(10) &&
                event.getNewValues().get("connection.pool.size").equals(20) &&
                event.getMetadata().get("configType").equals("Database") &&
                event.getMetadata().get("reason").equals("Performance optimization") &&
                event.getTags().get("configType").equals("Database")
            ));
        }
    }

    @Nested
    @DisplayName("Error Audit Tests")
    class ErrorAuditTests {

        @Test
        @DisplayName("Should audit errors with exception details")
        void shouldAuditErrorsWithExceptionDetails() {
            // Given
            RuntimeException exception = new RuntimeException("Database connection failed");
            Map<String, Object> additionalContext = new HashMap<>();
            additionalContext.put("retryAttempt", 3);
            additionalContext.put("dbHost", "db.example.com");

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.auditError(AuditEvent.AuditAction.READ, "User", "user-123", 
                "user-service", exception, additionalContext);

            // Then
            verify(auditRepository).save(argThat(event ->
                event.getAction() == AuditEvent.AuditAction.READ &&
                event.getResourceType().equals("User") &&
                event.getResourceId().equals("user-123") &&
                event.getUserId().equals("user-service") &&
                !event.getSuccess() &&
                event.getSeverity() == AuditEvent.AuditSeverity.ERROR &&
                event.getCategory().equals("ERROR") &&
                event.getDescription().equals("Operation failed with exception") &&
                event.getErrorMessage().equals("Database connection failed") &&
                event.getExceptionType().equals("RuntimeException") &&
                event.getMetadata().get("retryAttempt").equals(3) &&
                event.getMetadata().get("dbHost").equals("db.example.com") &&
                event.getMetadata().containsKey("exceptionMessage") &&
                event.getMetadata().containsKey("stackTrace")
            ));
        }
    }

    @Nested
    @DisplayName("Audit Context Tests")
    class AuditContextTests {

        @Test
        @DisplayName("Should create and use audit context")
        void shouldCreateAndUseAuditContext() {
            // Given
            String correlationId = "correlation-123";
            String userId = "user-456";
            String sessionId = "session-789";

            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            try (AuditLogger.AuditContext context = auditService.startAuditContext(correlationId, userId, sessionId)) {
                assertThat(context.getCorrelationId()).isEqualTo(correlationId);
                assertThat(context.getUserId()).isEqualTo(userId);
                assertThat(context.getSessionId()).isEqualTo(sessionId);

                // Add context data
                context.setTraceId("trace-101");
                context.addContextData("requestSource", "mobile-app");

                // Audit within context
                context.audit(AuditEvent.AuditAction.CREATE, "Order", "order-123");
            }

            // Then
            verify(auditRepository, timeout(1000)).save(argThat(event ->
                event.getCorrelationId().equals(correlationId) &&
                event.getUserId().equals(userId) &&
                event.getSessionId().equals(sessionId) &&
                event.getTraceId().equals("trace-101") &&
                event.getMetadata().get("requestSource").equals("mobile-app")
            ));
        }

        @Test
        @DisplayName("Should audit asynchronously within context")
        void shouldAuditAsynchronouslyWithinContext() {
            // Given
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            try (AuditLogger.AuditContext context = auditService.startAuditContext("corr-123", "user-456", "sess-789")) {
                CompletableFuture<Void> future = context.auditAsync(AuditEvent.AuditAction.UPDATE, "Product", "prod-123");
                
                // Then
                assertThat(future).isNotNull();
                assertThatCode(() -> future.get(1000, java.util.concurrent.TimeUnit.MILLISECONDS))
                    .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Search and Statistics Tests")
    class SearchAndStatisticsTests {

        @Test
        @DisplayName("Should search audit events with criteria")
        void shouldSearchAuditEventsWithCriteria() {
            // Given
            List<AuditEvent> mockEvents = Arrays.asList(testAuditEvent);
            Page<AuditEvent> mockPage = new PageImpl<>(mockEvents, PageRequest.of(0, 10), 1);

            when(auditRepository.findByUserIdAndTimestampBetween(eq("user-123"), any(LocalDateTime.class), 
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(mockPage);

            AuditLogger.AuditSearchCriteria criteria = new AuditLogger.AuditSearchCriteria()
                .setUserId("user-123")
                .setStartTime(LocalDateTime.now().minusDays(1))
                .setEndTime(LocalDateTime.now())
                .setPage(0)
                .setSize(10);

            // When
            AuditLogger.AuditSearchResult result = auditService.searchAuditEvents(criteria);

            // Then
            assertThat(result.getEvents()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
            assertThat(result.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should get audit statistics")
        void shouldGetAuditStatistics() {
            // Given
            LocalDateTime startTime = LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = LocalDateTime.now();

            when(auditRepository.countByTimestampBetween(startTime, endTime)).thenReturn(100L);
            when(auditRepository.countBySuccessFalseAndTimestampBetween(startTime, endTime)).thenReturn(5L);
            when(auditRepository.getSuccessRate(startTime, endTime)).thenReturn(95.0);
            when(auditRepository.getAverageResponseTime(startTime, endTime)).thenReturn(250.0);

            // Mock statistics queries
            when(auditRepository.getAuditStatsByAction(startTime, endTime))
                .thenReturn(Arrays.asList(new Object[]{"CREATE", 50L}, new Object[]{"READ", 30L}));
            when(auditRepository.getAuditStatsBySeverity(startTime, endTime))
                .thenReturn(Arrays.asList(new Object[]{"INFO", 90L}, new Object[]{"ERROR", 10L}));
            when(auditRepository.getAuditStatsByService(startTime, endTime))
                .thenReturn(Arrays.asList(new Object[]{"user-service", 60L}, new Object[]{"order-service", 40L}));
            when(auditRepository.getAuditStatsByUser(startTime, endTime, 100))
                .thenReturn(Arrays.asList(new Object[]{"user-123", 25L}, new Object[]{"user-456", 20L}));
            when(auditRepository.getAuditStatsByHour(startTime, endTime))
                .thenReturn(Arrays.asList(new Object[]{10, 15L}, new Object[]{11, 20L}));
            when(auditRepository.getAuditStatsByDay(startTime, endTime))
                .thenReturn(Arrays.asList(new Object[]{"2023-12-25", 50L}, new Object[]{"2023-12-26", 50L}));

            // When
            AuditLogger.AuditStatistics stats = auditService.getAuditStatistics(startTime, endTime, null);

            // Then
            assertThat(stats.getTotalEvents()).isEqualTo(100L);
            assertThat(stats.getSuccessfulEvents()).isEqualTo(95L);
            assertThat(stats.getFailedEvents()).isEqualTo(5L);
            assertThat(stats.getSuccessRate()).isEqualTo(95.0);
            assertThat(stats.getAverageResponseTime()).isEqualTo(250.0);
            assertThat(stats.getEventsByAction()).containsEntry("CREATE", 50L);
            assertThat(stats.getEventsBySeverity()).containsEntry("INFO", 90L);
            assertThat(stats.getEventsByService()).containsEntry("user-service", 60L);
            assertThat(stats.getEventsByUser()).containsKey("user-123");
            assertThat(stats.getEventsByHour()).containsKey("Hour 10");
            assertThat(stats.getEventsByDay()).containsKey("2023-12-25");
        }

        @Test
        @DisplayName("Should export audit events asynchronously")
        void shouldExportAuditEventsAsynchronously() {
            // Given
            List<AuditEvent> mockEvents = Arrays.asList(testAuditEvent);
            Page<AuditEvent> mockPage = new PageImpl<>(mockEvents, PageRequest.of(0, Integer.MAX_VALUE), 1);

            when(auditRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            AuditLogger.AuditSearchCriteria criteria = new AuditLogger.AuditSearchCriteria()
                .setStartTime(LocalDateTime.now().minusDays(1))
                .setEndTime(LocalDateTime.now());

            // When
            CompletableFuture<AuditLogger.AuditExportResult> future = 
                auditService.exportAuditEvents(criteria, "JSON");

            // Then
            assertThat(future).isNotNull();
            assertThatCode(() -> {
                AuditLogger.AuditExportResult result = future.get(2000, java.util.concurrent.TimeUnit.MILLISECONDS);
                assertThat(result.getFormat()).isEqualTo("JSON");
                assertThat(result.getRecordCount()).isEqualTo(1);
                assertThat(result.isReady()).isTrue();
                assertThat(result.getStatus()).isEqualTo("COMPLETED");
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use async when enabled")
        void shouldUseAsyncWhenEnabled() {
            // Given
            ReflectionTestUtils.setField(auditService, "asyncEnabled", true);
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.audit(AuditEvent.AuditAction.CREATE, "Test", "test-123", "user-456");

            // Then
            verify(auditRepository, timeout(1000)).save(any(AuditEvent.class));
        }

        @Test
        @DisplayName("Should use sync when async disabled")
        void shouldUseSyncWhenAsyncDisabled() {
            // Given
            ReflectionTestUtils.setField(auditService, "asyncEnabled", false);
            when(auditRepository.save(any(AuditEvent.class))).thenReturn(testAuditEvent);

            // When
            auditService.audit(AuditEvent.AuditAction.CREATE, "Test", "test-123", "user-456");

            // Then
            verify(auditRepository).save(any(AuditEvent.class));
        }
    }
}