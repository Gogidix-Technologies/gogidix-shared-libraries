package com.gogidix.ecosystem.shared.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for AuditEvent class with compliance scenarios.
 * Tests all audit functionality, validation, compliance flags, and security features.
 */
@DisplayName("AuditEvent Comprehensive Tests")
class AuditEventComprehensiveTest {

    private AuditEvent.AuditEventBuilder baseEventBuilder;
    private Map<String, Object> testMetadata;
    private Map<String, String> testTags;

    @BeforeEach
    void setUp() {
        baseEventBuilder = AuditEvent.builder()
            .id(UUID.randomUUID())
            .action(AuditEvent.AuditAction.CREATE)
            .serviceName("test-service")
            .timestamp(LocalDateTime.now())
            .severity(AuditEvent.AuditSeverity.INFO)
            .success(true);

        testMetadata = new HashMap<>();
        testMetadata.put("requestId", UUID.randomUUID().toString());
        testMetadata.put("environment", "test");

        testTags = new HashMap<>();
        testTags.put("source", "unit-test");
        testTags.put("category", "testing");
    }

    @Nested
    @DisplayName("Basic Event Creation Tests")
    class BasicEventCreationTests {

        @Test
        @DisplayName("Should create audit event with all required fields")
        void shouldCreateAuditEventWithAllRequiredFields() {
            // When
            AuditEvent event = baseEventBuilder.build();

            // Then
            assertThat(event.getId()).isNotNull();
            assertThat(event.getAction()).isEqualTo(AuditEvent.AuditAction.CREATE);
            assertThat(event.getServiceName()).isEqualTo("test-service");
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSeverity()).isEqualTo(AuditEvent.AuditSeverity.INFO);
            assertThat(event.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should set default values correctly")
        void shouldSetDefaultValuesCorrectly() {
            // When
            AuditEvent event = AuditEvent.builder()
                .action(AuditEvent.AuditAction.READ)
                .serviceName("default-test")
                .build();

            // Then
            assertThat(event.getSeverity()).isEqualTo(AuditEvent.AuditSeverity.INFO);
            assertThat(event.getSuccess()).isTrue();
            assertThat(event.getRetentionDays()).isEqualTo(2555); // 7 years default
        }

        @Test
        @DisplayName("Should generate unique IDs for different events")
        void shouldGenerateUniqueIdsForDifferentEvents() {
            // When
            AuditEvent event1 = baseEventBuilder.build();
            AuditEvent event2 = baseEventBuilder.id(UUID.randomUUID()).build();

            // Then
            assertThat(event1.getId()).isNotEqualTo(event2.getId());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create standard audit event with pre-populated fields")
        void shouldCreateStandardAuditEventWithPrePopulatedFields() {
            // When
            AuditEvent event = AuditEvent.standard(AuditEvent.AuditAction.UPDATE, "order-service").build();

            // Then
            assertThat(event.getId()).isNotNull();
            assertThat(event.getAction()).isEqualTo(AuditEvent.AuditAction.UPDATE);
            assertThat(event.getServiceName()).isEqualTo("order-service");
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSeverity()).isEqualTo(AuditEvent.AuditSeverity.INFO);
            assertThat(event.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should create security audit event with appropriate defaults")
        void shouldCreateSecurityAuditEventWithAppropriateDefaults() {
            // When
            AuditEvent event = AuditEvent.security(AuditEvent.AuditAction.LOGIN, "auth-service").build();

            // Then
            assertThat(event.getAction()).isEqualTo(AuditEvent.AuditAction.LOGIN);
            assertThat(event.getServiceName()).isEqualTo("auth-service");
            assertThat(event.getSeverity()).isEqualTo(AuditEvent.AuditSeverity.WARN);
            assertThat(event.getCategory()).isEqualTo("SECURITY");
        }

        @Test
        @DisplayName("Should create compliance audit event with extended retention")
        void shouldCreateComplianceAuditEventWithExtendedRetention() {
            // When
            AuditEvent event = AuditEvent.compliance(AuditEvent.AuditAction.EXPORT, "data-service", "GDPR").build();

            // Then
            assertThat(event.getAction()).isEqualTo(AuditEvent.AuditAction.EXPORT);
            assertThat(event.getServiceName()).isEqualTo("data-service");
            assertThat(event.getCategory()).isEqualTo("COMPLIANCE");
            assertThat(event.getRetentionDays()).isEqualTo(2555); // 7 years for compliance
        }
    }

    @Nested
    @DisplayName("Action and Severity Tests")
    class ActionAndSeverityTests {

        @ParameterizedTest
        @EnumSource(AuditEvent.AuditAction.class)
        @DisplayName("Should handle all audit actions")
        void shouldHandleAllAuditActions(AuditEvent.AuditAction action) {
            // When
            AuditEvent event = baseEventBuilder.action(action).build();

            // Then
            assertThat(event.getAction()).isEqualTo(action);
        }

        @ParameterizedTest
        @EnumSource(AuditEvent.AuditSeverity.class)
        @DisplayName("Should handle all severity levels")
        void shouldHandleAllSeverityLevels(AuditEvent.AuditSeverity severity) {
            // When
            AuditEvent event = baseEventBuilder.severity(severity).build();

            // Then
            assertThat(event.getSeverity()).isEqualTo(severity);
        }

        @Test
        @DisplayName("Should identify security-sensitive events correctly")
        void shouldIdentifySecuritySensitiveEventsCorrectly() {
            // Given
            AuditEvent loginEvent = baseEventBuilder.action(AuditEvent.AuditAction.LOGIN).build();
            AuditEvent logoutEvent = baseEventBuilder.action(AuditEvent.AuditAction.LOGOUT).build();
            AuditEvent authenticateEvent = baseEventBuilder.action(AuditEvent.AuditAction.AUTHENTICATE).build();
            AuditEvent authorizeEvent = baseEventBuilder.action(AuditEvent.AuditAction.AUTHORIZE).build();
            AuditEvent grantEvent = baseEventBuilder.action(AuditEvent.AuditAction.GRANT).build();
            AuditEvent revokeEvent = baseEventBuilder.action(AuditEvent.AuditAction.REVOKE).build();
            AuditEvent criticalEvent = baseEventBuilder.action(AuditEvent.AuditAction.CREATE).severity(AuditEvent.AuditSeverity.CRITICAL).build();
            AuditEvent errorEvent = baseEventBuilder.action(AuditEvent.AuditAction.READ).severity(AuditEvent.AuditSeverity.ERROR).build();
            AuditEvent normalEvent = baseEventBuilder.action(AuditEvent.AuditAction.READ).severity(AuditEvent.AuditSeverity.INFO).build();

            // Then
            assertThat(loginEvent.isSecuritySensitive()).isTrue();
            assertThat(logoutEvent.isSecuritySensitive()).isTrue();
            assertThat(authenticateEvent.isSecuritySensitive()).isTrue();
            assertThat(authorizeEvent.isSecuritySensitive()).isTrue();
            assertThat(grantEvent.isSecuritySensitive()).isTrue();
            assertThat(revokeEvent.isSecuritySensitive()).isTrue();
            assertThat(criticalEvent.isSecuritySensitive()).isTrue();
            assertThat(errorEvent.isSecuritySensitive()).isTrue();
            assertThat(normalEvent.isSecuritySensitive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Metadata and Tags Management Tests")
    class MetadataAndTagsManagementTests {

        @Test
        @DisplayName("Should add and retrieve tags correctly")
        void shouldAddAndRetrieveTagsCorrectly() {
            // Given
            AuditEvent event = baseEventBuilder.build();

            // When
            event.addTag("environment", "production");
            event.addTag("region", "us-east-1");
            event.addTag("dataClassification", "PII");

            // Then
            assertThat(event.getTags()).hasSize(3);
            assertThat(event.getTags().get("environment")).isEqualTo("production");
            assertThat(event.getTags().get("region")).isEqualTo("us-east-1");
            assertThat(event.getTags().get("dataClassification")).isEqualTo("PII");
        }

        @Test
        @DisplayName("Should add and retrieve metadata correctly")
        void shouldAddAndRetrieveMetadataCorrectly() {
            // Given
            AuditEvent event = baseEventBuilder.build();
            UUID requestId = UUID.randomUUID();
            Map<String, Object> requestDetails = new HashMap<>();
            requestDetails.put("method", "POST");
            requestDetails.put("path", "/api/users");

            // When
            event.addMetadata("requestId", requestId);
            event.addMetadata("userAgent", "Test-Client/1.0");
            event.addMetadata("requestDetails", requestDetails);

            // Then
            assertThat(event.getMetadata()).hasSize(3);
            assertThat(event.getMetadata().get("requestId")).isEqualTo(requestId);
            assertThat(event.getMetadata().get("userAgent")).isEqualTo("Test-Client/1.0");
            assertThat(event.getMetadata().get("requestDetails")).isEqualTo(requestDetails);
        }

        @Test
        @DisplayName("Should add and retrieve security context correctly")
        void shouldAddAndRetrieveSecurityContextCorrectly() {
            // Given
            AuditEvent event = baseEventBuilder.build();

            // When
            event.addSecurityContext("roles", java.util.Arrays.asList("ADMIN", "USER"));
            event.addSecurityContext("authMethod", "JWT");
            event.addSecurityContext("mfaEnabled", true);

            // Then
            assertThat(event.getSecurityContext()).hasSize(3);
            assertThat(event.getSecurityContext().get("roles")).isEqualTo(java.util.Arrays.asList("ADMIN", "USER"));
            assertThat(event.getSecurityContext().get("authMethod")).isEqualTo("JWT");
            assertThat(event.getSecurityContext().get("mfaEnabled")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should handle null collections gracefully")
        void shouldHandleNullCollectionsGracefully() {
            // Given
            AuditEvent event = AuditEvent.builder()
                .action(AuditEvent.AuditAction.CREATE)
                .serviceName("test-service")
                .build();

            // When/Then - Should not throw exceptions
            assertThatCode(() -> {
                event.addTag("key", "value");
                event.addMetadata("key", "value");
                event.addSecurityContext("key", "value");
            }).doesNotThrowAnyException();

            assertThat(event.getTags()).hasSize(1);
            assertThat(event.getMetadata()).hasSize(1);
            assertThat(event.getSecurityContext()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Compliance Scenarios Tests")
    class ComplianceScenariosTests {

        @Test
        @DisplayName("Should handle GDPR compliance scenarios")
        void shouldHandleGdprComplianceScenarios() {
            // Given
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.EXPORT)
                .resourceType("PersonalData")
                .resourceId("user-123")
                .userId("data-controller-456")
                .category("COMPLIANCE")
                .build();

            // When
            event.setComplianceFlag("GDPR", true);
            event.addTag("dataSubject", "user-123");
            event.addTag("legalBasis", "consent");
            event.addMetadata("exportReason", "data portability request");
            event.addMetadata("dataCategories", java.util.Arrays.asList("contact", "preferences", "transactions"));

            // Then
            assertThat(event.getComplianceFlags()).containsEntry("GDPR", true);
            assertThat(event.requiresLongTermRetention()).isTrue();
            assertThat(event.getTags().get("dataSubject")).isEqualTo("user-123");
            assertThat(event.getTags().get("legalBasis")).isEqualTo("consent");
        }

        @Test
        @DisplayName("Should handle PCI DSS compliance scenarios")
        void shouldHandlePciDssComplianceScenarios() {
            // Given
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.ACCESS)
                .resourceType("PaymentData")
                .resourceId("card-****1234")
                .userId("payment-processor-789")
                .category("COMPLIANCE")
                .build();

            // When
            event.setComplianceFlag("PCI_DSS", true);
            event.addTag("cardType", "VISA");
            event.addTag("environment", "PCI_COMPLIANT");
            event.addMetadata("accessPurpose", "payment processing");
            event.addMetadata("encryptionStatus", "encrypted");
            event.addSecurityContext("pciCompliantSystem", true);

            // Then
            assertThat(event.getComplianceFlags()).containsEntry("PCI_DSS", true);
            assertThat(event.requiresLongTermRetention()).isTrue();
            assertThat(event.getTags().get("environment")).isEqualTo("PCI_COMPLIANT");
            assertThat(event.getSecurityContext().get("pciCompliantSystem")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should handle SOX compliance scenarios")
        void shouldHandleSoxComplianceScenarios() {
            // Given
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.CONFIGURE)
                .resourceType("FinancialControl")
                .resourceId("revenue-recognition-rule")
                .userId("financial-controller-101")
                .category("COMPLIANCE")
                .build();

            // When
            event.setComplianceFlag("SOX", true);
            event.addTag("controlType", "automated");
            event.addTag("riskLevel", "high");
            event.addMetadata("changeReason", "regulatory update");
            event.addMetadata("approver", "cfo@company.com");

            // Then
            assertThat(event.getComplianceFlags()).containsEntry("SOX", true);
            assertThat(event.requiresLongTermRetention()).isTrue();
            assertThat(event.getTags().get("controlType")).isEqualTo("automated");
            assertThat(event.getTags().get("riskLevel")).isEqualTo("high");
        }

        @Test
        @DisplayName("Should handle HIPAA compliance scenarios")
        void shouldHandleHipaaComplianceScenarios() {
            // Given
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.READ)
                .resourceType("HealthRecord")
                .resourceId("patient-456")
                .userId("healthcare-provider-789")
                .category("COMPLIANCE")
                .build();

            // When
            event.setComplianceFlag("HIPAA", true);
            event.addTag("phi", "true");
            event.addTag("accessType", "treatment");
            event.addMetadata("minimumNecessary", true);
            event.addMetadata("patientConsent", "explicit");

            // Then
            assertThat(event.getComplianceFlags()).containsEntry("HIPAA", true);
            assertThat(event.requiresLongTermRetention()).isTrue();
            assertThat(event.getTags().get("phi")).isEqualTo("true");
            assertThat(event.getTags().get("accessType")).isEqualTo("treatment");
        }

        @Test
        @DisplayName("Should handle multi-compliance scenarios")
        void shouldHandleMultiComplianceScenarios() {
            // Given
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.DELETE)
                .resourceType("CustomerData")
                .resourceId("customer-789")
                .userId("data-retention-service")
                .category("COMPLIANCE")
                .build();

            // When
            event.setComplianceFlag("GDPR", true);
            event.setComplianceFlag("PCI_DSS", true);
            event.setComplianceFlag("SOX", true);
            event.addTag("retentionPolicy", "7-years");
            event.addTag("deletionReason", "retention-expired");
            event.addMetadata("dataTypes", java.util.Arrays.asList("personal", "payment", "financial"));

            // Then
            assertThat(event.getComplianceFlags()).hasSize(3);
            assertThat(event.getComplianceFlags()).containsEntry("GDPR", true);
            assertThat(event.getComplianceFlags()).containsEntry("PCI_DSS", true);
            assertThat(event.getComplianceFlags()).containsEntry("SOX", true);
            assertThat(event.requiresLongTermRetention()).isTrue();
        }

        @Test
        @DisplayName("Should not require long-term retention for non-compliance events")
        void shouldNotRequireLongTermRetentionForNonComplianceEvents() {
            // Given
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.READ)
                .resourceType("PublicData")
                .build();

            // When/Then
            assertThat(event.requiresLongTermRetention()).isFalse();
        }
    }

    @Nested
    @DisplayName("HTTP and API Context Tests")
    class HttpAndApiContextTests {

        @Test
        @DisplayName("Should handle HTTP request context correctly")
        void shouldHandleHttpRequestContextCorrectly() {
            // When
            AuditEvent event = baseEventBuilder
                .httpMethod("POST")
                .requestUri("/api/v1/users")
                .httpStatus(201)
                .durationMs(150L)
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .build();

            // Then
            assertThat(event.getHttpMethod()).isEqualTo("POST");
            assertThat(event.getRequestUri()).isEqualTo("/api/v1/users");
            assertThat(event.getHttpStatus()).isEqualTo(201);
            assertThat(event.getDurationMs()).isEqualTo(150L);
            assertThat(event.getIpAddress()).isEqualTo("192.168.1.100");
            assertThat(event.getUserAgent()).isEqualTo("Mozilla/5.0");
        }

        @ParameterizedTest
        @ValueSource(ints = {200, 201, 204, 400, 401, 403, 404, 500, 503})
        @DisplayName("Should handle various HTTP status codes")
        void shouldHandleVariousHttpStatusCodes(int statusCode) {
            // When
            AuditEvent event = baseEventBuilder.httpStatus(statusCode).build();

            // Then
            assertThat(event.getHttpStatus()).isEqualTo(statusCode);
        }
    }

    @Nested
    @DisplayName("User and Session Context Tests")
    class UserAndSessionContextTests {

        @Test
        @DisplayName("Should handle user context information")
        void shouldHandleUserContextInformation() {
            // When
            AuditEvent event = baseEventBuilder
                .userId("user-123")
                .username("john.doe")
                .sessionId("session-456")
                .tenantId("tenant-789")
                .build();

            // Then
            assertThat(event.getUserId()).isEqualTo("user-123");
            assertThat(event.getUsername()).isEqualTo("john.doe");
            assertThat(event.getSessionId()).isEqualTo("session-456");
            assertThat(event.getTenantId()).isEqualTo("tenant-789");
        }

        @Test
        @DisplayName("Should handle correlation and trace information")
        void shouldHandleCorrelationAndTraceInformation() {
            // When
            AuditEvent event = baseEventBuilder
                .correlationId("correlation-123")
                .traceId("trace-456")
                .build();

            // Then
            assertThat(event.getCorrelationId()).isEqualTo("correlation-123");
            assertThat(event.getTraceId()).isEqualTo("trace-456");
        }
    }

    @Nested
    @DisplayName("Error and Exception Handling Tests")
    class ErrorAndExceptionHandlingTests {

        @Test
        @DisplayName("Should handle error scenarios correctly")
        void shouldHandleErrorScenariosCorrectly() {
            // When
            AuditEvent event = baseEventBuilder
                .success(false)
                .severity(AuditEvent.AuditSeverity.ERROR)
                .errorMessage("Database connection failed")
                .exceptionType("SQLException")
                .build();

            // Then
            assertThat(event.getSuccess()).isFalse();
            assertThat(event.getSeverity()).isEqualTo(AuditEvent.AuditSeverity.ERROR);
            assertThat(event.getErrorMessage()).isEqualTo("Database connection failed");
            assertThat(event.getExceptionType()).isEqualTo("SQLException");
        }

        @Test
        @DisplayName("Should handle critical failures appropriately")
        void shouldHandleCriticalFailuresAppropriately() {
            // When
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.AUTHENTICATE)
                .success(false)
                .severity(AuditEvent.AuditSeverity.CRITICAL)
                .errorMessage("Security breach detected")
                .category("SECURITY")
                .build();

            // Then
            assertThat(event.getSuccess()).isFalse();
            assertThat(event.getSeverity()).isEqualTo(AuditEvent.AuditSeverity.CRITICAL);
            assertThat(event.isSecuritySensitive()).isTrue();
            assertThat(event.getCategory()).isEqualTo("SECURITY");
        }
    }

    @Nested
    @DisplayName("Summary and Description Tests")
    class SummaryAndDescriptionTests {

        @Test
        @DisplayName("Should generate correct summary for successful operation")
        void shouldGenerateCorrectSummaryForSuccessfulOperation() {
            // Given
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.CREATE)
                .resourceType("Order")
                .resourceId("order-123")
                .username("john.doe")
                .timestamp(timestamp)
                .success(true)
                .build();

            // When
            String summary = event.getSummary();

            // Then
            assertThat(summary).contains("CREATE succeeded on Order (ID: order-123) by john.doe at 2023-12-25T10:30");
        }

        @Test
        @DisplayName("Should generate correct summary for failed operation")
        void shouldGenerateCorrectSummaryForFailedOperation() {
            // Given
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.DELETE)
                .resourceType("User")
                .resourceId("user-456")
                .username("admin")
                .timestamp(timestamp)
                .success(false)
                .build();

            // When
            String summary = event.getSummary();

            // Then
            assertThat(summary).contains("DELETE failed on User (ID: user-456) by admin at 2023-12-25T10:30");
        }

        @Test
        @DisplayName("Should handle null values in summary generation")
        void shouldHandleNullValuesInSummaryGeneration() {
            // Given
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.READ)
                .timestamp(timestamp)
                .success(true)
                .build();

            // When
            String summary = event.getSummary();

            // Then
            assertThat(summary).contains("READ succeeded on system by system at 2023-12-25T10:30");
        }
    }

    @Nested
    @DisplayName("Data Change Tracking Tests")
    class DataChangeTrackingTests {

        @Test
        @DisplayName("Should track old and new values for updates")
        void shouldTrackOldAndNewValuesForUpdates() {
            // Given
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("email", "old@example.com");
            oldValues.put("status", "inactive");

            Map<String, Object> newValues = new HashMap<>();
            newValues.put("email", "new@example.com");
            newValues.put("status", "active");

            // When
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.UPDATE)
                .resourceType("User")
                .resourceId("user-123")
                .oldValues(oldValues)
                .newValues(newValues)
                .build();

            // Then
            assertThat(event.getOldValues()).isEqualTo(oldValues);
            assertThat(event.getNewValues()).isEqualTo(newValues);
            assertThat(event.getOldValues().get("email")).isEqualTo("old@example.com");
            assertThat(event.getNewValues().get("email")).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("Should track only new values for create operations")
        void shouldTrackOnlyNewValuesForCreateOperations() {
            // Given
            Map<String, Object> newValues = new HashMap<>();
            newValues.put("name", "John Doe");
            newValues.put("email", "john@example.com");
            newValues.put("role", "USER");

            // When
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.CREATE)
                .resourceType("User")
                .resourceId("user-456")
                .newValues(newValues)
                .build();

            // Then
            assertThat(event.getNewValues()).isEqualTo(newValues);
            assertThat(event.getOldValues()).isNull();
        }

        @Test
        @DisplayName("Should track only old values for delete operations")
        void shouldTrackOnlyOldValuesForDeleteOperations() {
            // Given
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("name", "Jane Doe");
            oldValues.put("email", "jane@example.com");
            oldValues.put("lastLogin", LocalDateTime.now().minusDays(30));

            // When
            AuditEvent event = baseEventBuilder
                .action(AuditEvent.AuditAction.DELETE)
                .resourceType("User")
                .resourceId("user-789")
                .oldValues(oldValues)
                .build();

            // Then
            assertThat(event.getOldValues()).isEqualTo(oldValues);
            assertThat(event.getNewValues()).isNull();
        }
    }

    @Nested
    @DisplayName("Performance and Resource Tracking Tests")
    class PerformanceAndResourceTrackingTests {

        @Test
        @DisplayName("Should track resource information correctly")
        void shouldTrackResourceInformationCorrectly() {
            // When
            AuditEvent event = baseEventBuilder
                .resourceType("Product")
                .resourceId("product-123")
                .category("BUSINESS_OPERATION")
                .description("Product inventory update")
                .build();

            // Then
            assertThat(event.getResourceType()).isEqualTo("Product");
            assertThat(event.getResourceId()).isEqualTo("product-123");
            assertThat(event.getCategory()).isEqualTo("BUSINESS_OPERATION");
            assertThat(event.getDescription()).isEqualTo("Product inventory update");
        }

        @Test
        @DisplayName("Should track performance metrics")
        void shouldTrackPerformanceMetrics() {
            // When
            AuditEvent event = baseEventBuilder
                .durationMs(1500L)
                .httpStatus(200)
                .build();

            // Then
            assertThat(event.getDurationMs()).isEqualTo(1500L);
            assertThat(event.getHttpStatus()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Retention Policy Tests")
    class RetentionPolicyTests {

        @Test
        @DisplayName("Should use default retention period")
        void shouldUseDefaultRetentionPeriod() {
            // When
            AuditEvent event = baseEventBuilder.build();

            // Then
            assertThat(event.getRetentionDays()).isEqualTo(2555); // 7 years default
        }

        @Test
        @DisplayName("Should allow custom retention period")
        void shouldAllowCustomRetentionPeriod() {
            // When
            AuditEvent event = baseEventBuilder
                .retentionDays(90) // 90 days
                .build();

            // Then
            assertThat(event.getRetentionDays()).isEqualTo(90);
        }

        @Test
        @DisplayName("Should require long-term retention for compliance events")
        void shouldRequireLongTermRetentionForComplianceEvents() {
            // Given
            AuditEvent gdprEvent = baseEventBuilder.build();
            gdprEvent.setComplianceFlag("GDPR", true);

            AuditEvent pciEvent = baseEventBuilder.build();
            pciEvent.setComplianceFlag("PCI_DSS", true);

            AuditEvent normalEvent = baseEventBuilder.build();

            // Then
            assertThat(gdprEvent.requiresLongTermRetention()).isTrue();
            assertThat(pciEvent.requiresLongTermRetention()).isTrue();
            assertThat(normalEvent.requiresLongTermRetention()).isFalse();
        }
    }
}