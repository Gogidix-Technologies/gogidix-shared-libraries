package com.gogidix.ecosystem.shared.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ComplianceReporter with various compliance scenarios.
 * Tests GDPR, PCI DSS, SOX, HIPAA reporting and compliance dashboard functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceReporter Comprehensive Tests")
class ComplianceReporterComprehensiveTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private ComplianceReporter complianceReporter;

    private LocalDateTime reportStartTime;
    private LocalDateTime reportEndTime;
    private List<AuditEvent> mockGdprEvents;
    private List<AuditEvent> mockPciDssEvents;
    private List<AuditEvent> mockSoxEvents;
    private List<AuditEvent> mockHipaaEvents;

    @BeforeEach
    void setUp() {
        reportStartTime = LocalDateTime.now().minusDays(30);
        reportEndTime = LocalDateTime.now();
        
        setupMockGdprEvents();
        setupMockPciDssEvents();
        setupMockSoxEvents();
        setupMockHipaaEvents();
    }

    private void setupMockGdprEvents() {
        mockGdprEvents = new ArrayList<>();
        
        // Data access event with justification
        AuditEvent dataAccessEvent = createMockAuditEvent(
            AuditEvent.AuditAction.READ, "PersonalData", "user-123", "data-controller",
            "DATA_ACCESS", true);
        dataAccessEvent.getMetadata().put("justification", "Customer support request");
        dataAccessEvent.getMetadata().put("purpose", "customer service");
        dataAccessEvent.setComplianceFlag("GDPR", true);
        mockGdprEvents.add(dataAccessEvent);
        
        // Data export event
        AuditEvent dataExportEvent = createMockAuditEvent(
            AuditEvent.AuditAction.EXPORT, "PersonalData", "user-456", "data-processor",
            "DATA_PROCESSING", true);
        dataExportEvent.getMetadata().put("exportReason", "data portability request");
        dataExportEvent.getMetadata().put("purpose", "data portability");
        dataExportEvent.setComplianceFlag("GDPR", true);
        mockGdprEvents.add(dataExportEvent);
        
        // Data deletion event
        AuditEvent dataDeletionEvent = createMockAuditEvent(
            AuditEvent.AuditAction.DELETE, "PersonalData", "user-789", "data-controller",
            "DATA_PROCESSING", true);
        dataDeletionEvent.getMetadata().put("deletionReason", "retention period expired");
        dataDeletionEvent.setComplianceFlag("GDPR", true);
        mockGdprEvents.add(dataDeletionEvent);
        
        // Consent event
        AuditEvent consentEvent = createMockAuditEvent(
            AuditEvent.AuditAction.GRANT, "Consent", "user-101", "consent-service",
            "CONSENT", true);
        consentEvent.getMetadata().put("consentType", "marketing");
        consentEvent.getMetadata().put("purpose", "consent management");
        consentEvent.setComplianceFlag("GDPR", true);
        mockGdprEvents.add(consentEvent);
    }

    private void setupMockPciDssEvents() {
        mockPciDssEvents = new ArrayList<>();
        
        // Payment data access with encryption
        AuditEvent paymentAccessEvent = createMockAuditEvent(
            AuditEvent.AuditAction.READ, "PAYMENT", "payment-123", "payment-processor",
            "PAYMENT_PROCESSING", true);
        paymentAccessEvent.getMetadata().put("encrypted", true);
        paymentAccessEvent.getMetadata().put("monitored", true);
        paymentAccessEvent.setComplianceFlag("PCI_DSS", true);
        mockPciDssEvents.add(paymentAccessEvent);
        
        // Card data event
        AuditEvent cardDataEvent = createMockAuditEvent(
            AuditEvent.AuditAction.PROCESS, "CARD", "card-****1234", "payment-gateway",
            "PAYMENT_PROCESSING", true);
        cardDataEvent.getMetadata().put("encrypted", true);
        cardDataEvent.getMetadata().put("tokenized", true);
        cardDataEvent.setComplianceFlag("PCI_DSS", true);
        mockPciDssEvents.add(cardDataEvent);
        
        // Network security event
        AuditEvent networkSecurityEvent = createMockAuditEvent(
            AuditEvent.AuditAction.ACCESS, "NetworkResource", "secure-network", "firewall",
            "NETWORK_SECURITY", true);
        networkSecurityEvent.getMetadata().put("tlsVersion", "1.3");
        networkSecurityEvent.getMetadata().put("monitored", true);
        networkSecurityEvent.setComplianceFlag("PCI_DSS", true);
        mockPciDssEvents.add(networkSecurityEvent);
        
        // Access control event
        AuditEvent accessControlEvent = createMockAuditEvent(
            AuditEvent.AuditAction.AUTHENTICATE, "PaymentSystem", "payment-user", "auth-service",
            "ACCESS_CONTROL", true);
        accessControlEvent.getMetadata().put("mfaEnabled", true);
        accessControlEvent.getMetadata().put("monitored", true);
        accessControlEvent.setComplianceFlag("PCI_DSS", true);
        mockPciDssEvents.add(accessControlEvent);
    }

    private void setupMockSoxEvents() {
        mockSoxEvents = new ArrayList<>();
        
        // Financial transaction event
        AuditEvent financialTransactionEvent = createMockAuditEvent(
            AuditEvent.AuditAction.CREATE, "FinancialTransaction", "txn-123", "finance-system",
            "FINANCIAL", true);
        financialTransactionEvent.getMetadata().put("amount", 1000.00);
        financialTransactionEvent.getMetadata().put("authorization", "manager-approval");
        financialTransactionEvent.setComplianceFlag("SOX", true);
        mockSoxEvents.add(financialTransactionEvent);
        
        // Configuration change with approval
        AuditEvent configChangeEvent = createMockAuditEvent(
            AuditEvent.AuditAction.CONFIGURE, "SystemConfig", "revenue-recognition", "admin",
            "CONFIGURATION", true);
        configChangeEvent.getMetadata().put("approver", "cfo@company.com");
        configChangeEvent.getMetadata().put("changeReason", "regulatory update");
        configChangeEvent.setComplianceFlag("SOX", true);
        mockSoxEvents.add(configChangeEvent);
        
        // Access grant with authorization
        AuditEvent accessGrantEvent = createMockAuditEvent(
            AuditEvent.AuditAction.GRANT, "FinancialSystem", "financial-user", "access-manager",
            "ACCESS_MANAGEMENT", true);
        accessGrantEvent.getMetadata().put("authorization", "hr-approved");
        accessGrantEvent.getMetadata().put("role", "financial-analyst");
        accessGrantEvent.setComplianceFlag("SOX", true);
        mockSoxEvents.add(accessGrantEvent);
        
        // Internal control event
        AuditEvent internalControlEvent = createMockAuditEvent(
            AuditEvent.AuditAction.VALIDATE, "InternalControl", "segregation-of-duties", "control-system",
            "INTERNAL_CONTROL", true);
        internalControlEvent.getMetadata().put("controlType", "automated");
        internalControlEvent.getMetadata().put("effectiveness", "high");
        internalControlEvent.setComplianceFlag("SOX", true);
        mockSoxEvents.add(internalControlEvent);
    }

    private void setupMockHipaaEvents() {
        mockHipaaEvents = new ArrayList<>();
        
        // Health record access
        AuditEvent healthRecordEvent = createMockAuditEvent(
            AuditEvent.AuditAction.READ, "HealthRecord", "patient-123", "healthcare-provider",
            "HEALTHCARE", true);
        healthRecordEvent.getMetadata().put("minimumNecessary", true);
        healthRecordEvent.getMetadata().put("patientConsent", "explicit");
        healthRecordEvent.setComplianceFlag("HIPAA", true);
        mockHipaaEvents.add(healthRecordEvent);
    }

    private AuditEvent createMockAuditEvent(AuditEvent.AuditAction action, String resourceType, 
                                          String resourceId, String userId, String category, boolean success) {
        AuditEvent event = AuditEvent.builder()
            .id(UUID.randomUUID())
            .action(action)
            .serviceName("test-service")
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .category(category)
            .timestamp(reportStartTime.plusHours(1))
            .success(success)
            .severity(AuditEvent.AuditSeverity.INFO)
            .metadata(new HashMap<>())
            .tags(new HashMap<>())
            .complianceFlags(new HashMap<>())
            .retentionDays(2555)
            .build();
        
        return event;
    }

    @Nested
    @DisplayName("GDPR Compliance Reporting Tests")
    class GdprComplianceReportingTests {

        @Test
        @DisplayName("Should generate comprehensive GDPR compliance report")
        void shouldGenerateComprehensiveGdprComplianceReport() {
            // Given
            Page<AuditEvent> mockPage = new PageImpl<>(mockGdprEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockGdprEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.GDPR"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                reportStartTime, reportEndTime, null);

            // Then
            assertThat(report).isNotNull();
            assertThat(report.getStartTime()).isEqualTo(reportStartTime);
            assertThat(report.getEndTime()).isEqualTo(reportEndTime);
            assertThat(report.getGeneratedAt()).isNotNull();
            assertThat(report.getTotalEvents()).isEqualTo(4);
            
            // Verify event categorization
            assertThat(report.getDataAccessEvents()).hasSize(1);
            assertThat(report.getDataExportEvents()).hasSize(1);
            assertThat(report.getDataDeletionEvents()).hasSize(1);
            assertThat(report.getConsentEvents()).hasSize(1);
            
            // Verify compliance scores
            assertThat(report.getDataMinimizationCompliance()).isEqualTo(100.0); // All access events have justification
            assertThat(report.getPurposeLimitationCompliance()).isEqualTo(100.0); // All events have purpose
            assertThat(report.getStorageLimitationCompliance()).isEqualTo(100.0); // All events have retention
            
            // Verify recommendations
            assertThat(report.getRecommendations()).isNotNull();
        }

        @Test
        @DisplayName("Should generate GDPR report for specific user")
        void shouldGenerateGdprReportForSpecificUser() {
            // Given
            String specificUserId = "user-123";
            List<AuditEvent> userEvents = mockGdprEvents.stream()
                .filter(event -> specificUserId.equals(event.getUserId()))
                .toList();
            
            Page<AuditEvent> mockPage = new PageImpl<>(mockGdprEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockGdprEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.GDPR"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                reportStartTime, reportEndTime, specificUserId);

            // Then
            assertThat(report.getUserId()).isEqualTo(specificUserId);
            assertThat(report.getTotalEvents()).isEqualTo(4); // All events returned by repository
        }

        @Test
        @DisplayName("Should calculate GDPR compliance scores correctly")
        void shouldCalculateGdprComplianceScoresCorrectly() {
            // Given - Events with missing justifications
            List<AuditEvent> eventsWithMissingData = new ArrayList<>(mockGdprEvents);
            AuditEvent eventWithoutJustification = createMockAuditEvent(
                AuditEvent.AuditAction.READ, "PersonalData", "user-999", "data-controller",
                "DATA_ACCESS", true);
            eventWithoutJustification.setComplianceFlag("GDPR", true);
            eventsWithMissingData.add(eventWithoutJustification);
            
            Page<AuditEvent> mockPage = new PageImpl<>(eventsWithMissingData, 
                PageRequest.of(0, Integer.MAX_VALUE), eventsWithMissingData.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.GDPR"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                reportStartTime, reportEndTime, null);

            // Then
            assertThat(report.getDataMinimizationCompliance()).isEqualTo(50.0); // 1 out of 2 read events has justification
            assertThat(report.getPurposeLimitationCompliance()).isEqualTo(80.0); // 4 out of 5 events have purpose
        }

        @Test
        @DisplayName("Should generate appropriate GDPR recommendations")
        void shouldGenerateAppropriateGdprRecommendations() {
            // Given - Events with compliance issues
            List<AuditEvent> problematicEvents = new ArrayList<>();
            
            // Event without justification
            AuditEvent eventWithoutJustification = createMockAuditEvent(
                AuditEvent.AuditAction.READ, "PersonalData", "user-999", "data-controller",
                "DATA_ACCESS", true);
            eventWithoutJustification.setComplianceFlag("GDPR", true);
            problematicEvents.add(eventWithoutJustification);
            
            // Event with excessive retention
            AuditEvent eventWithLongRetention = createMockAuditEvent(
                AuditEvent.AuditAction.CREATE, "PersonalData", "user-888", "data-controller",
                "DATA_PROCESSING", true);
            eventWithLongRetention.setComplianceFlag("GDPR", true);
            eventWithLongRetention.setRetentionDays(3000); // More than 7 years
            problematicEvents.add(eventWithLongRetention);
            
            Page<AuditEvent> mockPage = new PageImpl<>(problematicEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), problematicEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.GDPR"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                reportStartTime, reportEndTime, null);

            // Then
            assertThat(report.getRecommendations()).contains(
                "Add justification metadata to 1 data access events",
                "Review retention periods for 1 events exceeding 7 years"
            );
        }
    }

    @Nested
    @DisplayName("PCI DSS Compliance Reporting Tests")
    class PciDssComplianceReportingTests {

        @Test
        @DisplayName("Should generate comprehensive PCI DSS compliance report")
        void shouldGenerateComprehensivePciDssComplianceReport() {
            // Given
            Page<AuditEvent> mockPage = new PageImpl<>(mockPciDssEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockPciDssEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.PCI_DSS"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.PciDssComplianceReport report = complianceReporter.generatePciDssReport(
                reportStartTime, reportEndTime);

            // Then
            assertThat(report).isNotNull();
            assertThat(report.getStartTime()).isEqualTo(reportStartTime);
            assertThat(report.getEndTime()).isEqualTo(reportEndTime);
            assertThat(report.getGeneratedAt()).isNotNull();
            assertThat(report.getTotalEvents()).isEqualTo(4);
            
            // Verify event categorization
            assertThat(report.getPaymentDataAccessEvents()).hasSize(1);
            assertThat(report.getCardDataEvents()).hasSize(1);
            assertThat(report.getNetworkSecurityEvents()).hasSize(1);
            assertThat(report.getAccessControlEvents()).hasSize(1);
            
            // Verify compliance scores
            assertThat(report.getSecureNetworkScore()).isEqualTo(100.0);
            assertThat(report.getDataProtectionScore()).isEqualTo(100.0);
            assertThat(report.getAccessControlScore()).isEqualTo(100.0);
            assertThat(report.getMonitoringScore()).isEqualTo(100.0);
            
            assertThat(report.getSecurityRecommendations()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate PCI DSS compliance scores for security issues")
        void shouldCalculatePciDssComplianceScoresForSecurityIssues() {
            // Given - Events with security issues
            List<AuditEvent> securityIssueEvents = new ArrayList<>(mockPciDssEvents);
            
            // Unencrypted payment data access
            AuditEvent unencryptedEvent = createMockAuditEvent(
                AuditEvent.AuditAction.READ, "PAYMENT", "payment-456", "insecure-processor",
                "PAYMENT_PROCESSING", true);
            unencryptedEvent.setComplianceFlag("PCI_DSS", true);
            // No encryption metadata
            securityIssueEvents.add(unencryptedEvent);
            
            // Failed access control
            AuditEvent failedAccessEvent = createMockAuditEvent(
                AuditEvent.AuditAction.AUTHENTICATE, "PaymentSystem", "unauthorized-user", "auth-service",
                "ACCESS_CONTROL", false);
            failedAccessEvent.setComplianceFlag("PCI_DSS", true);
            securityIssueEvents.add(failedAccessEvent);
            
            Page<AuditEvent> mockPage = new PageImpl<>(securityIssueEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), securityIssueEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.PCI_DSS"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.PciDssComplianceReport report = complianceReporter.generatePciDssReport(
                reportStartTime, reportEndTime);

            // Then
            assertThat(report.getDataProtectionScore()).isEqualTo(50.0); // 1 out of 2 payment events encrypted
            assertThat(report.getAccessControlScore()).isEqualTo(50.0); // 1 out of 2 access control events successful
        }

        @Test
        @DisplayName("Should generate appropriate PCI DSS security recommendations")
        void shouldGenerateAppropriatePciDssSecurityRecommendations() {
            // Given - Events with security issues
            List<AuditEvent> securityIssueEvents = new ArrayList<>();
            
            // Unencrypted payment access
            AuditEvent unencryptedEvent = createMockAuditEvent(
                AuditEvent.AuditAction.READ, "PAYMENT", "payment-999", "insecure-processor",
                "PAYMENT_PROCESSING", true);
            unencryptedEvent.setComplianceFlag("PCI_DSS", true);
            securityIssueEvents.add(unencryptedEvent);
            
            // Failed access control
            AuditEvent failedAccessEvent = createMockAuditEvent(
                AuditEvent.AuditAction.AUTHENTICATE, "PaymentSystem", "unauthorized-user", "auth-service",
                "ACCESS_CONTROL", false);
            failedAccessEvent.setComplianceFlag("PCI_DSS", true);
            securityIssueEvents.add(failedAccessEvent);
            
            Page<AuditEvent> mockPage = new PageImpl<>(securityIssueEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), securityIssueEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.PCI_DSS"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.PciDssComplianceReport report = complianceReporter.generatePciDssReport(
                reportStartTime, reportEndTime);

            // Then
            assertThat(report.getSecurityRecommendations()).contains(
                "Ensure encryption for 1 payment data access events",
                "Investigate 1 failed access control events"
            );
        }
    }

    @Nested
    @DisplayName("SOX Compliance Reporting Tests")
    class SoxComplianceReportingTests {

        @Test
        @DisplayName("Should generate comprehensive SOX compliance report")
        void shouldGenerateComprehensiveSoxComplianceReport() {
            // Given
            Page<AuditEvent> mockPage = new PageImpl<>(mockSoxEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockSoxEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.SOX"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.SoxComplianceReport report = complianceReporter.generateSoxReport(
                reportStartTime, reportEndTime);

            // Then
            assertThat(report).isNotNull();
            assertThat(report.getStartTime()).isEqualTo(reportStartTime);
            assertThat(report.getEndTime()).isEqualTo(reportEndTime);
            assertThat(report.getGeneratedAt()).isNotNull();
            assertThat(report.getTotalEvents()).isEqualTo(4);
            
            // Verify event categorization
            assertThat(report.getFinancialTransactionEvents()).hasSize(1);
            assertThat(report.getConfigurationChangeEvents()).hasSize(1);
            assertThat(report.getAccessGrantEvents()).hasSize(1);
            
            // Verify compliance scores
            assertThat(report.getInternalControlScore()).isEqualTo(100.0);
            assertThat(report.getChangeManagementScore()).isEqualTo(100.0);
            assertThat(report.getAccessManagementScore()).isEqualTo(100.0);
            
            assertThat(report.getControlRecommendations()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate SOX compliance scores for control issues")
        void shouldCalculateSoxComplianceScoresForControlIssues() {
            // Given - Events with control issues
            List<AuditEvent> controlIssueEvents = new ArrayList<>(mockSoxEvents);
            
            // Unapproved configuration change
            AuditEvent unapprovedChange = createMockAuditEvent(
                AuditEvent.AuditAction.CONFIGURE, "SystemConfig", "financial-rule", "admin",
                "CONFIGURATION", true);
            unapprovedChange.setComplianceFlag("SOX", true);
            // No approver metadata
            controlIssueEvents.add(unapprovedChange);
            
            // Unauthorized access grant
            AuditEvent unauthorizedGrant = createMockAuditEvent(
                AuditEvent.AuditAction.GRANT, "FinancialSystem", "temp-user", "access-manager",
                "ACCESS_MANAGEMENT", true);
            unauthorizedGrant.setComplianceFlag("SOX", true);
            // No authorization metadata
            controlIssueEvents.add(unauthorizedGrant);
            
            Page<AuditEvent> mockPage = new PageImpl<>(controlIssueEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), controlIssueEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.SOX"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.SoxComplianceReport report = complianceReporter.generateSoxReport(
                reportStartTime, reportEndTime);

            // Then
            assertThat(report.getChangeManagementScore()).isEqualTo(50.0); // 1 out of 2 config changes approved
            assertThat(report.getAccessManagementScore()).isEqualTo(50.0); // 1 out of 2 access grants authorized
        }

        @Test
        @DisplayName("Should generate appropriate SOX control recommendations")
        void shouldGenerateAppropriateSoxControlRecommendations() {
            // Given - Events with control issues
            List<AuditEvent> controlIssueEvents = new ArrayList<>();
            
            // Unapproved configuration change
            AuditEvent unapprovedChange = createMockAuditEvent(
                AuditEvent.AuditAction.CONFIGURE, "SystemConfig", "financial-rule", "admin",
                "CONFIGURATION", true);
            unapprovedChange.setComplianceFlag("SOX", true);
            controlIssueEvents.add(unapprovedChange);
            
            // Unauthorized access grant
            AuditEvent unauthorizedGrant = createMockAuditEvent(
                AuditEvent.AuditAction.GRANT, "FinancialSystem", "temp-user", "access-manager",
                "ACCESS_MANAGEMENT", true);
            unauthorizedGrant.setComplianceFlag("SOX", true);
            controlIssueEvents.add(unauthorizedGrant);
            
            Page<AuditEvent> mockPage = new PageImpl<>(controlIssueEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), controlIssueEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.SOX"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When
            ComplianceReporter.SoxComplianceReport report = complianceReporter.generateSoxReport(
                reportStartTime, reportEndTime);

            // Then
            assertThat(report.getControlRecommendations()).contains(
                "Ensure approval workflow for 1 configuration changes",
                "Review authorization for 1 access grant events"
            );
        }
    }

    @Nested
    @DisplayName("Compliance Dashboard Tests")
    class ComplianceDashboardTests {

        @Test
        @DisplayName("Should generate comprehensive compliance dashboard")
        void shouldGenerateComprehensiveComplianceDashboard() {
            // Given
            setupMockRepositoryForDashboard();

            // When
            ComplianceReporter.ComplianceDashboard dashboard = complianceReporter.generateComplianceDashboard(
                reportStartTime, reportEndTime);

            // Then
            assertThat(dashboard).isNotNull();
            assertThat(dashboard.getStartTime()).isEqualTo(reportStartTime);
            assertThat(dashboard.getEndTime()).isEqualTo(reportEndTime);
            assertThat(dashboard.getGeneratedAt()).isNotNull();
            
            // Verify compliance summaries
            assertThat(dashboard.getGdprSummary()).isNotNull();
            assertThat(dashboard.getGdprSummary().getComplianceType()).isEqualTo("GDPR");
            assertThat(dashboard.getPciDssSummary()).isNotNull();
            assertThat(dashboard.getPciDssSummary().getComplianceType()).isEqualTo("PCI_DSS");
            assertThat(dashboard.getSoxSummary()).isNotNull();
            assertThat(dashboard.getSoxSummary().getComplianceType()).isEqualTo("SOX");
            assertThat(dashboard.getHipaaSummary()).isNotNull();
            assertThat(dashboard.getHipaaSummary().getComplianceType()).isEqualTo("HIPAA");
            
            // Verify overall compliance score
            assertThat(dashboard.getOverallComplianceScore()).isGreaterThan(0);
            assertThat(dashboard.getOverallComplianceScore()).isLessThanOrEqualTo(100);
            
            // Verify trends and risk indicators
            assertThat(dashboard.getComplianceTrends()).isNotNull();
            assertThat(dashboard.getRiskIndicators()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate overall compliance score correctly")
        void shouldCalculateOverallComplianceScoreCorrectly() {
            // Given
            setupMockRepositoryForDashboard();

            // When
            ComplianceReporter.ComplianceDashboard dashboard = complianceReporter.generateComplianceDashboard(
                reportStartTime, reportEndTime);

            // Then
            // Overall score should be average of all compliance type scores
            double expectedScore = (dashboard.getGdprSummary().getComplianceScore() +
                                  dashboard.getPciDssSummary().getComplianceScore() +
                                  dashboard.getSoxSummary().getComplianceScore() +
                                  dashboard.getHipaaSummary().getComplianceScore()) / 4.0;
            
            assertThat(dashboard.getOverallComplianceScore()).isEqualTo(expectedScore);
        }

        @Test
        @DisplayName("Should identify high error rate risk indicators")
        void shouldIdentifyHighErrorRateRiskIndicators() {
            // Given
            setupMockRepositoryForDashboard();
            
            // Mock high error rate
            when(auditRepository.countByTimestampBetween(reportStartTime, reportEndTime)).thenReturn(100L);
            when(auditRepository.countBySuccessFalseAndTimestampBetween(reportStartTime, reportEndTime)).thenReturn(10L);

            // When
            ComplianceReporter.ComplianceDashboard dashboard = complianceReporter.generateComplianceDashboard(
                reportStartTime, reportEndTime);

            // Then
            assertThat(dashboard.getRiskIndicators()).hasSize(1);
            Map<String, Object> riskIndicator = dashboard.getRiskIndicators().get(0);
            assertThat(riskIndicator.get("type")).isEqualTo("HIGH_ERROR_RATE");
            assertThat(riskIndicator.get("severity")).isEqualTo("HIGH");
            assertThat(riskIndicator.get("value")).isEqualTo(10.0);
        }

        private void setupMockRepositoryForDashboard() {
            // Mock GDPR events
            Page<AuditEvent> gdprPage = new PageImpl<>(mockGdprEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockGdprEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.GDPR"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(gdprPage);
            
            // Mock PCI DSS events
            Page<AuditEvent> pciPage = new PageImpl<>(mockPciDssEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockPciDssEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.PCI_DSS"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(pciPage);
            
            // Mock SOX events
            Page<AuditEvent> soxPage = new PageImpl<>(mockSoxEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockSoxEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.SOX"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(soxPage);
            
            // Mock HIPAA events
            Page<AuditEvent> hipaaPage = new PageImpl<>(mockHipaaEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockHipaaEvents.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.HIPAA"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(hipaaPage);
            
            // Mock trending data
            Page<AuditEvent> recentEventsPage = new PageImpl<>(mockGdprEvents, 
                PageRequest.of(0, Integer.MAX_VALUE), mockGdprEvents.size());
            when(auditRepository.findRecentAuditEvents(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(recentEventsPage);
            
            // Mock statistics for risk indicators
            when(auditRepository.countByTimestampBetween(reportStartTime, reportEndTime)).thenReturn(50L);
            when(auditRepository.countBySuccessFalseAndTimestampBetween(reportStartTime, reportEndTime)).thenReturn(2L);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle empty audit events gracefully")
        void shouldHandleEmptyAuditEventsGracefully() {
            // Given
            Page<AuditEvent> emptyPage = new PageImpl<>(Collections.emptyList(), 
                PageRequest.of(0, Integer.MAX_VALUE), 0);
            when(auditRepository.findComplianceAuditEvents(anyString(), any(LocalDateTime.class), 
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(emptyPage);

            // When
            ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                reportStartTime, reportEndTime, null);

            // Then
            assertThat(report).isNotNull();
            assertThat(report.getTotalEvents()).isEqualTo(0);
            assertThat(report.getDataMinimizationCompliance()).isEqualTo(100.0);
            assertThat(report.getPurposeLimitationCompliance()).isEqualTo(100.0);
            assertThat(report.getStorageLimitationCompliance()).isEqualTo(100.0);
            assertThat(report.getRecommendations()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null metadata gracefully")
        void shouldHandleNullMetadataGracefully() {
            // Given
            List<AuditEvent> eventsWithNullMetadata = new ArrayList<>();
            AuditEvent eventWithNullMetadata = createMockAuditEvent(
                AuditEvent.AuditAction.READ, "PersonalData", "user-123", "data-controller",
                "DATA_ACCESS", true);
            eventWithNullMetadata.setMetadata(null);
            eventWithNullMetadata.setComplianceFlags(new HashMap<>());
            eventWithNullMetadata.getComplianceFlags().put("GDPR", true);
            eventsWithNullMetadata.add(eventWithNullMetadata);
            
            Page<AuditEvent> mockPage = new PageImpl<>(eventsWithNullMetadata, 
                PageRequest.of(0, Integer.MAX_VALUE), eventsWithNullMetadata.size());
            when(auditRepository.findComplianceAuditEvents(eq("$.GDPR"), eq(reportStartTime), 
                eq(reportEndTime), any(Pageable.class))).thenReturn(mockPage);

            // When/Then - Should not throw exception
            assertThatCode(() -> {
                ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                    reportStartTime, reportEndTime, null);
                assertThat(report).isNotNull();
                assertThat(report.getTotalEvents()).isEqualTo(1);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle invalid time ranges")
        void shouldHandleInvalidTimeRanges() {
            // Given
            LocalDateTime invalidEndTime = reportStartTime.minusDays(1); // End before start
            Page<AuditEvent> emptyPage = new PageImpl<>(Collections.emptyList(), 
                PageRequest.of(0, Integer.MAX_VALUE), 0);
            when(auditRepository.findComplianceAuditEvents(anyString(), any(LocalDateTime.class), 
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(emptyPage);

            // When/Then - Should not throw exception
            assertThatCode(() -> {
                ComplianceReporter.GdprComplianceReport report = complianceReporter.generateGdprReport(
                    reportStartTime, invalidEndTime, null);
                assertThat(report).isNotNull();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(auditRepository.findComplianceAuditEvents(anyString(), any(LocalDateTime.class), 
                any(LocalDateTime.class), any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

            // When/Then - Should throw exception (this is expected behavior for repository errors)
            assertThatThrownBy(() -> complianceReporter.generateGdprReport(reportStartTime, reportEndTime, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
        }
    }
}