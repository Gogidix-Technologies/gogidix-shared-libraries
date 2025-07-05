package com.gogidix.ecosystem.shared.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating compliance reports from audit events in the Exalt Social E-commerce Ecosystem.
 * Provides specialized reporting capabilities for various regulatory requirements.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ComplianceReporter {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplianceReporter.class);
    
    @Autowired
    private AuditRepository auditRepository;
    
    /**
     * Generates a GDPR compliance report for data access and processing activities.
     * 
     * @param startTime Start time for the report
     * @param endTime End time for the report
     * @param userId Optional user ID to filter by
     * @return GDPR compliance report
     */
    public GdprComplianceReport generateGdprReport(LocalDateTime startTime, LocalDateTime endTime, String userId) {
        logger.info("Generating GDPR compliance report from {} to {} for user: {}", 
            startTime, endTime, userId);
        
        GdprComplianceReport report = new GdprComplianceReport();
        report.setReportPeriod(startTime, endTime);
        report.setGeneratedAt(LocalDateTime.now());
        report.setUserId(userId);
        
        // Get all GDPR-related audit events
        List<AuditEvent> gdprEvents = getComplianceEvents("GDPR", startTime, endTime, userId);
        report.setTotalEvents(gdprEvents.size());
        
        // Categorize events by GDPR principles
        report.setDataProcessingEvents(filterEventsByCategory(gdprEvents, "DATA_PROCESSING"));
        report.setDataAccessEvents(filterEventsByAction(gdprEvents, AuditEvent.AuditAction.READ));
        report.setDataExportEvents(filterEventsByAction(gdprEvents, AuditEvent.AuditAction.EXPORT));
        report.setDataDeletionEvents(filterEventsByAction(gdprEvents, AuditEvent.AuditAction.DELETE));
        report.setConsentEvents(filterEventsByCategory(gdprEvents, "CONSENT"));
        
        // Calculate compliance metrics
        report.setDataMinimizationCompliance(calculateDataMinimizationCompliance(gdprEvents));
        report.setPurposeLimitationCompliance(calculatePurposeLimitationCompliance(gdprEvents));
        report.setStorageLimitationCompliance(calculateStorageLimitationCompliance(gdprEvents));
        
        // Generate recommendations
        report.setRecommendations(generateGdprRecommendations(gdprEvents));
        
        return report;
    }
    
    /**
     * Generates a PCI DSS compliance report for payment data security.
     * 
     * @param startTime Start time for the report
     * @param endTime End time for the report
     * @return PCI DSS compliance report
     */
    public PciDssComplianceReport generatePciDssReport(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Generating PCI DSS compliance report from {} to {}", startTime, endTime);
        
        PciDssComplianceReport report = new PciDssComplianceReport();
        report.setReportPeriod(startTime, endTime);
        report.setGeneratedAt(LocalDateTime.now());
        
        // Get all PCI DSS-related audit events
        List<AuditEvent> pciEvents = getComplianceEvents("PCI_DSS", startTime, endTime, null);
        report.setTotalEvents(pciEvents.size());
        
        // Categorize events by PCI DSS requirements
        report.setPaymentDataAccessEvents(filterEventsByResourceType(pciEvents, "PAYMENT"));
        report.setCardDataEvents(filterEventsByResourceType(pciEvents, "CARD"));
        report.setEncryptionEvents(filterEventsByCategory(pciEvents, "ENCRYPTION"));
        report.setAccessControlEvents(filterEventsByCategory(pciEvents, "ACCESS_CONTROL"));
        report.setNetworkSecurityEvents(filterEventsByCategory(pciEvents, "NETWORK_SECURITY"));
        
        // Calculate compliance scores
        report.setSecureNetworkScore(calculateSecureNetworkCompliance(pciEvents));
        report.setDataProtectionScore(calculateDataProtectionCompliance(pciEvents));
        report.setAccessControlScore(calculateAccessControlCompliance(pciEvents));
        report.setMonitoringScore(calculateMonitoringCompliance(pciEvents));
        
        // Generate security recommendations
        report.setSecurityRecommendations(generatePciSecurityRecommendations(pciEvents));
        
        return report;
    }
    
    /**
     * Generates a SOX compliance report for financial controls.
     * 
     * @param startTime Start time for the report
     * @param endTime End time for the report
     * @return SOX compliance report
     */
    public SoxComplianceReport generateSoxReport(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Generating SOX compliance report from {} to {}", startTime, endTime);
        
        SoxComplianceReport report = new SoxComplianceReport();
        report.setReportPeriod(startTime, endTime);
        report.setGeneratedAt(LocalDateTime.now());
        
        // Get all SOX-related audit events
        List<AuditEvent> soxEvents = getComplianceEvents("SOX", startTime, endTime, null);
        report.setTotalEvents(soxEvents.size());
        
        // Categorize events by SOX controls
        report.setFinancialTransactionEvents(filterEventsByCategory(soxEvents, "FINANCIAL"));
        report.setConfigurationChangeEvents(filterEventsByAction(soxEvents, AuditEvent.AuditAction.CONFIGURE));
        report.setAccessGrantEvents(filterEventsByAction(soxEvents, AuditEvent.AuditAction.GRANT));
        report.setPrivilegedAccessEvents(filterEventsByCategory(soxEvents, "PRIVILEGED_ACCESS"));
        
        // Calculate control effectiveness
        report.setInternalControlScore(calculateInternalControlEffectiveness(soxEvents));
        report.setChangeManagementScore(calculateChangeManagementCompliance(soxEvents));
        report.setAccessManagementScore(calculateAccessManagementCompliance(soxEvents));
        
        // Generate control recommendations
        report.setControlRecommendations(generateSoxControlRecommendations(soxEvents));
        
        return report;
    }
    
    /**
     * Generates a comprehensive compliance dashboard showing all regulatory statuses.
     * 
     * @param startTime Start time for the dashboard
     * @param endTime End time for the dashboard
     * @return Compliance dashboard
     */
    public ComplianceDashboard generateComplianceDashboard(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Generating compliance dashboard from {} to {}", startTime, endTime);
        
        ComplianceDashboard dashboard = new ComplianceDashboard();
        dashboard.setReportPeriod(startTime, endTime);
        dashboard.setGeneratedAt(LocalDateTime.now());
        
        // Get summary statistics for each compliance type
        dashboard.setGdprSummary(getComplianceSummary("GDPR", startTime, endTime));
        dashboard.setPciDssSummary(getComplianceSummary("PCI_DSS", startTime, endTime));
        dashboard.setSoxSummary(getComplianceSummary("SOX", startTime, endTime));
        dashboard.setHipaaSummary(getComplianceSummary("HIPAA", startTime, endTime));
        
        // Calculate overall compliance score
        dashboard.setOverallComplianceScore(calculateOverallComplianceScore(dashboard));
        
        // Get trending data
        dashboard.setComplianceTrends(getComplianceTrends(startTime, endTime));
        
        // Get risk indicators
        dashboard.setRiskIndicators(getComplianceRiskIndicators(startTime, endTime));
        
        return dashboard;
    }
    
    /**
     * Gets compliance events for a specific type and time range.
     */
    private List<AuditEvent> getComplianceEvents(String complianceType, LocalDateTime startTime, 
                                               LocalDateTime endTime, String userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("timestamp").descending());
        
        // Use the repository method to find compliance events
        Page<AuditEvent> page = auditRepository.findComplianceAuditEvents(
            "$." + complianceType, startTime, endTime, pageable);
        
        List<AuditEvent> events = page.getContent();
        
        // Additional filtering by user if specified
        if (userId != null) {
            events = events.stream()
                .filter(event -> userId.equals(event.getUserId()))
                .collect(Collectors.toList());
        }
        
        return events;
    }
    
    /**
     * Filters events by category.
     */
    private List<AuditEvent> filterEventsByCategory(List<AuditEvent> events, String category) {
        return events.stream()
            .filter(event -> category.equals(event.getCategory()))
            .collect(Collectors.toList());
    }
    
    /**
     * Filters events by action.
     */
    private List<AuditEvent> filterEventsByAction(List<AuditEvent> events, AuditEvent.AuditAction action) {
        return events.stream()
            .filter(event -> action.equals(event.getAction()))
            .collect(Collectors.toList());
    }
    
    /**
     * Filters events by resource type.
     */
    private List<AuditEvent> filterEventsByResourceType(List<AuditEvent> events, String resourceType) {
        return events.stream()
            .filter(event -> resourceType.equals(event.getResourceType()))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculates GDPR data minimization compliance score.
     */
    private double calculateDataMinimizationCompliance(List<AuditEvent> events) {
        // Implementation would analyze data access patterns
        // This is a simplified calculation
        long totalDataAccess = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.READ)
            .count();
        
        long justifiedAccess = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.READ)
            .filter(e -> e.getMetadata() != null && e.getMetadata().containsKey("justification"))
            .count();
        
        return totalDataAccess > 0 ? (double) justifiedAccess / totalDataAccess * 100 : 100.0;
    }
    
    /**
     * Calculates GDPR purpose limitation compliance score.
     */
    private double calculatePurposeLimitationCompliance(List<AuditEvent> events) {
        // Simplified calculation based on metadata presence
        long totalEvents = events.size();
        long eventsWithPurpose = events.stream()
            .filter(e -> e.getMetadata() != null && e.getMetadata().containsKey("purpose"))
            .count();
        
        return totalEvents > 0 ? (double) eventsWithPurpose / totalEvents * 100 : 100.0;
    }
    
    /**
     * Calculates GDPR storage limitation compliance score.
     */
    private double calculateStorageLimitationCompliance(List<AuditEvent> events) {
        // Check retention policies are set
        long eventsWithRetention = events.stream()
            .filter(e -> e.getRetentionDays() != null && e.getRetentionDays() > 0)
            .count();
        
        return events.size() > 0 ? (double) eventsWithRetention / events.size() * 100 : 100.0;
    }
    
    /**
     * Generates GDPR compliance recommendations.
     */
    private List<String> generateGdprRecommendations(List<AuditEvent> events) {
        List<String> recommendations = new ArrayList<>();
        
        // Check for missing justifications
        long eventsWithoutJustification = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.READ)
            .filter(e -> e.getMetadata() == null || !e.getMetadata().containsKey("justification"))
            .count();
        
        if (eventsWithoutJustification > 0) {
            recommendations.add("Add justification metadata to " + eventsWithoutJustification + " data access events");
        }
        
        // Check for excessive data retention
        long longRetentionEvents = events.stream()
            .filter(e -> e.getRetentionDays() != null && e.getRetentionDays() > 2555) // More than 7 years
            .count();
        
        if (longRetentionEvents > 0) {
            recommendations.add("Review retention periods for " + longRetentionEvents + " events exceeding 7 years");
        }
        
        return recommendations;
    }
    
    /**
     * Calculates PCI DSS secure network compliance score.
     */
    private double calculateSecureNetworkCompliance(List<AuditEvent> events) {
        // Simplified calculation based on network security events
        long networkEvents = filterEventsByCategory(events, "NETWORK_SECURITY").size();
        long secureEvents = events.stream()
            .filter(e -> "NETWORK_SECURITY".equals(e.getCategory()))
            .filter(e -> Boolean.TRUE.equals(e.getSuccess()))
            .count();
        
        return networkEvents > 0 ? (double) secureEvents / networkEvents * 100 : 100.0;
    }
    
    /**
     * Calculates PCI DSS data protection compliance score.
     */
    private double calculateDataProtectionCompliance(List<AuditEvent> events) {
        // Check for encryption compliance
        long dataEvents = events.stream()
            .filter(e -> "PAYMENT".equals(e.getResourceType()) || "CARD".equals(e.getResourceType()))
            .count();
        
        long encryptedEvents = events.stream()
            .filter(e -> "PAYMENT".equals(e.getResourceType()) || "CARD".equals(e.getResourceType()))
            .filter(e -> e.getMetadata() != null && Boolean.TRUE.equals(e.getMetadata().get("encrypted")))
            .count();
        
        return dataEvents > 0 ? (double) encryptedEvents / dataEvents * 100 : 100.0;
    }
    
    /**
     * Calculates PCI DSS access control compliance score.
     */
    private double calculateAccessControlCompliance(List<AuditEvent> events) {
        long accessEvents = filterEventsByCategory(events, "ACCESS_CONTROL").size();
        long authorizedEvents = events.stream()
            .filter(e -> "ACCESS_CONTROL".equals(e.getCategory()))
            .filter(e -> Boolean.TRUE.equals(e.getSuccess()))
            .count();
        
        return accessEvents > 0 ? (double) authorizedEvents / accessEvents * 100 : 100.0;
    }
    
    /**
     * Calculates PCI DSS monitoring compliance score.
     */
    private double calculateMonitoringCompliance(List<AuditEvent> events) {
        // Check if all events have proper monitoring metadata
        long monitoredEvents = events.stream()
            .filter(e -> e.getMetadata() != null && e.getMetadata().containsKey("monitored"))
            .count();
        
        return events.size() > 0 ? (double) monitoredEvents / events.size() * 100 : 100.0;
    }
    
    /**
     * Generates PCI DSS security recommendations.
     */
    private List<String> generatePciSecurityRecommendations(List<AuditEvent> events) {
        List<String> recommendations = new ArrayList<>();
        
        // Check for unencrypted payment data access
        long unencryptedPaymentAccess = events.stream()
            .filter(e -> "PAYMENT".equals(e.getResourceType()) || "CARD".equals(e.getResourceType()))
            .filter(e -> e.getMetadata() == null || !Boolean.TRUE.equals(e.getMetadata().get("encrypted")))
            .count();
        
        if (unencryptedPaymentAccess > 0) {
            recommendations.add("Ensure encryption for " + unencryptedPaymentAccess + " payment data access events");
        }
        
        // Check for failed access control events
        long failedAccessControl = filterEventsByCategory(events, "ACCESS_CONTROL").stream()
            .filter(e -> Boolean.FALSE.equals(e.getSuccess()))
            .count();
        
        if (failedAccessControl > 0) {
            recommendations.add("Investigate " + failedAccessControl + " failed access control events");
        }
        
        return recommendations;
    }
    
    /**
     * Calculates SOX internal control effectiveness.
     */
    private double calculateInternalControlEffectiveness(List<AuditEvent> events) {
        long controlEvents = filterEventsByCategory(events, "INTERNAL_CONTROL").size();
        long effectiveControls = events.stream()
            .filter(e -> "INTERNAL_CONTROL".equals(e.getCategory()))
            .filter(e -> Boolean.TRUE.equals(e.getSuccess()))
            .count();
        
        return controlEvents > 0 ? (double) effectiveControls / controlEvents * 100 : 100.0;
    }
    
    /**
     * Calculates SOX change management compliance score.
     */
    private double calculateChangeManagementCompliance(List<AuditEvent> events) {
        long changeEvents = filterEventsByAction(events, AuditEvent.AuditAction.CONFIGURE).size();
        long approvedChanges = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.CONFIGURE)
            .filter(e -> e.getMetadata() != null && e.getMetadata().containsKey("approver"))
            .count();
        
        return changeEvents > 0 ? (double) approvedChanges / changeEvents * 100 : 100.0;
    }
    
    /**
     * Calculates SOX access management compliance score.
     */
    private double calculateAccessManagementCompliance(List<AuditEvent> events) {
        long accessGrantEvents = filterEventsByAction(events, AuditEvent.AuditAction.GRANT).size();
        long authorizedGrants = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.GRANT)
            .filter(e -> e.getMetadata() != null && e.getMetadata().containsKey("authorization"))
            .count();
        
        return accessGrantEvents > 0 ? (double) authorizedGrants / accessGrantEvents * 100 : 100.0;
    }
    
    /**
     * Generates SOX control recommendations.
     */
    private List<String> generateSoxControlRecommendations(List<AuditEvent> events) {
        List<String> recommendations = new ArrayList<>();
        
        // Check for unapproved configuration changes
        long unapprovedChanges = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.CONFIGURE)
            .filter(e -> e.getMetadata() == null || !e.getMetadata().containsKey("approver"))
            .count();
        
        if (unapprovedChanges > 0) {
            recommendations.add("Ensure approval workflow for " + unapprovedChanges + " configuration changes");
        }
        
        // Check for unauthorized access grants
        long unauthorizedGrants = events.stream()
            .filter(e -> e.getAction() == AuditEvent.AuditAction.GRANT)
            .filter(e -> e.getMetadata() == null || !e.getMetadata().containsKey("authorization"))
            .count();
        
        if (unauthorizedGrants > 0) {
            recommendations.add("Review authorization for " + unauthorizedGrants + " access grant events");
        }
        
        return recommendations;
    }
    
    /**
     * Gets compliance summary for a specific type.
     */
    private ComplianceSummary getComplianceSummary(String complianceType, LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditEvent> events = getComplianceEvents(complianceType, startTime, endTime, null);
        
        ComplianceSummary summary = new ComplianceSummary();
        summary.setComplianceType(complianceType);
        summary.setTotalEvents(events.size());
        summary.setCompliantEvents(events.stream().filter(e -> Boolean.TRUE.equals(e.getSuccess())).count());
        summary.setComplianceScore(summary.getTotalEvents() > 0 ? 
            (double) summary.getCompliantEvents() / summary.getTotalEvents() * 100 : 100.0);
        
        return summary;
    }
    
    /**
     * Calculates overall compliance score across all regulations.
     */
    private double calculateOverallComplianceScore(ComplianceDashboard dashboard) {
        double totalScore = 0;
        int count = 0;
        
        if (dashboard.getGdprSummary() != null) {
            totalScore += dashboard.getGdprSummary().getComplianceScore();
            count++;
        }
        if (dashboard.getPciDssSummary() != null) {
            totalScore += dashboard.getPciDssSummary().getComplianceScore();
            count++;
        }
        if (dashboard.getSoxSummary() != null) {
            totalScore += dashboard.getSoxSummary().getComplianceScore();
            count++;
        }
        if (dashboard.getHipaaSummary() != null) {
            totalScore += dashboard.getHipaaSummary().getComplianceScore();
            count++;
        }
        
        return count > 0 ? totalScore / count : 100.0;
    }
    
    /**
     * Gets compliance trends over time.
     */
    private Map<String, Object> getComplianceTrends(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> trends = new HashMap<>();
        
        // Calculate daily compliance scores
        LocalDateTime current = startTime;
        List<Map<String, Object>> dailyScores = new ArrayList<>();
        
        while (current.isBefore(endTime)) {
            LocalDateTime dayEnd = current.plusDays(1);
            
            Map<String, Object> dayScore = new HashMap<>();
            dayScore.put("date", current.format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            // Get events for this day
            List<AuditEvent> dayEvents = auditRepository.findRecentAuditEvents(current, 
                PageRequest.of(0, Integer.MAX_VALUE)).getContent()
                .stream()
                .filter(e -> e.getTimestamp().isBefore(dayEnd))
                .collect(Collectors.toList());
            
            // Calculate compliance score for the day
            long compliantEvents = dayEvents.stream().filter(e -> Boolean.TRUE.equals(e.getSuccess())).count();
            double score = dayEvents.size() > 0 ? (double) compliantEvents / dayEvents.size() * 100 : 100.0;
            dayScore.put("score", score);
            
            dailyScores.add(dayScore);
            current = current.plusDays(1);
        }
        
        trends.put("dailyScores", dailyScores);
        return trends;
    }
    
    /**
     * Gets compliance risk indicators.
     */
    private List<Map<String, Object>> getComplianceRiskIndicators(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> indicators = new ArrayList<>();
        
        // High error rate indicator
        long totalEvents = auditRepository.countByTimestampBetween(startTime, endTime);
        long failedEvents = auditRepository.countBySuccessFalseAndTimestampBetween(startTime, endTime);
        
        if (totalEvents > 0) {
            double errorRate = (double) failedEvents / totalEvents * 100;
            if (errorRate > 5.0) { // More than 5% error rate
                Map<String, Object> indicator = new HashMap<>();
                indicator.put("type", "HIGH_ERROR_RATE");
                indicator.put("severity", "HIGH");
                indicator.put("message", String.format("Error rate is %.2f%% (threshold: 5%%)", errorRate));
                indicator.put("value", errorRate);
                indicators.add(indicator);
            }
        }
        
        return indicators;
    }
    
    // Inner classes for report structures
    
    public static class GdprComplianceReport {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime generatedAt;
        private String userId;
        private int totalEvents;
        private List<AuditEvent> dataProcessingEvents;
        private List<AuditEvent> dataAccessEvents;
        private List<AuditEvent> dataExportEvents;
        private List<AuditEvent> dataDeletionEvents;
        private List<AuditEvent> consentEvents;
        private double dataMinimizationCompliance;
        private double purposeLimitationCompliance;
        private double storageLimitationCompliance;
        private List<String> recommendations;
        
        // Getters and setters
        public void setReportPeriod(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        // ... (other getters and setters)
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        public List<AuditEvent> getDataProcessingEvents() { return dataProcessingEvents; }
        public void setDataProcessingEvents(List<AuditEvent> dataProcessingEvents) { this.dataProcessingEvents = dataProcessingEvents; }
        public List<AuditEvent> getDataAccessEvents() { return dataAccessEvents; }
        public void setDataAccessEvents(List<AuditEvent> dataAccessEvents) { this.dataAccessEvents = dataAccessEvents; }
        public List<AuditEvent> getDataExportEvents() { return dataExportEvents; }
        public void setDataExportEvents(List<AuditEvent> dataExportEvents) { this.dataExportEvents = dataExportEvents; }
        public List<AuditEvent> getDataDeletionEvents() { return dataDeletionEvents; }
        public void setDataDeletionEvents(List<AuditEvent> dataDeletionEvents) { this.dataDeletionEvents = dataDeletionEvents; }
        public List<AuditEvent> getConsentEvents() { return consentEvents; }
        public void setConsentEvents(List<AuditEvent> consentEvents) { this.consentEvents = consentEvents; }
        public double getDataMinimizationCompliance() { return dataMinimizationCompliance; }
        public void setDataMinimizationCompliance(double dataMinimizationCompliance) { this.dataMinimizationCompliance = dataMinimizationCompliance; }
        public double getPurposeLimitationCompliance() { return purposeLimitationCompliance; }
        public void setPurposeLimitationCompliance(double purposeLimitationCompliance) { this.purposeLimitationCompliance = purposeLimitationCompliance; }
        public double getStorageLimitationCompliance() { return storageLimitationCompliance; }
        public void setStorageLimitationCompliance(double storageLimitationCompliance) { this.storageLimitationCompliance = storageLimitationCompliance; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
    
    public static class PciDssComplianceReport {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime generatedAt;
        private int totalEvents;
        private List<AuditEvent> paymentDataAccessEvents;
        private List<AuditEvent> cardDataEvents;
        private List<AuditEvent> encryptionEvents;
        private List<AuditEvent> accessControlEvents;
        private List<AuditEvent> networkSecurityEvents;
        private double secureNetworkScore;
        private double dataProtectionScore;
        private double accessControlScore;
        private double monitoringScore;
        private List<String> securityRecommendations;
        
        public void setReportPeriod(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        public List<AuditEvent> getPaymentDataAccessEvents() { return paymentDataAccessEvents; }
        public void setPaymentDataAccessEvents(List<AuditEvent> paymentDataAccessEvents) { this.paymentDataAccessEvents = paymentDataAccessEvents; }
        public List<AuditEvent> getCardDataEvents() { return cardDataEvents; }
        public void setCardDataEvents(List<AuditEvent> cardDataEvents) { this.cardDataEvents = cardDataEvents; }
        public List<AuditEvent> getEncryptionEvents() { return encryptionEvents; }
        public void setEncryptionEvents(List<AuditEvent> encryptionEvents) { this.encryptionEvents = encryptionEvents; }
        public List<AuditEvent> getAccessControlEvents() { return accessControlEvents; }
        public void setAccessControlEvents(List<AuditEvent> accessControlEvents) { this.accessControlEvents = accessControlEvents; }
        public List<AuditEvent> getNetworkSecurityEvents() { return networkSecurityEvents; }
        public void setNetworkSecurityEvents(List<AuditEvent> networkSecurityEvents) { this.networkSecurityEvents = networkSecurityEvents; }
        public double getSecureNetworkScore() { return secureNetworkScore; }
        public void setSecureNetworkScore(double secureNetworkScore) { this.secureNetworkScore = secureNetworkScore; }
        public double getDataProtectionScore() { return dataProtectionScore; }
        public void setDataProtectionScore(double dataProtectionScore) { this.dataProtectionScore = dataProtectionScore; }
        public double getAccessControlScore() { return accessControlScore; }
        public void setAccessControlScore(double accessControlScore) { this.accessControlScore = accessControlScore; }
        public double getMonitoringScore() { return monitoringScore; }
        public void setMonitoringScore(double monitoringScore) { this.monitoringScore = monitoringScore; }
        public List<String> getSecurityRecommendations() { return securityRecommendations; }
        public void setSecurityRecommendations(List<String> securityRecommendations) { this.securityRecommendations = securityRecommendations; }
    }
    
    public static class SoxComplianceReport {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime generatedAt;
        private int totalEvents;
        private List<AuditEvent> financialTransactionEvents;
        private List<AuditEvent> configurationChangeEvents;
        private List<AuditEvent> accessGrantEvents;
        private List<AuditEvent> privilegedAccessEvents;
        private double internalControlScore;
        private double changeManagementScore;
        private double accessManagementScore;
        private List<String> controlRecommendations;
        
        public void setReportPeriod(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        public List<AuditEvent> getFinancialTransactionEvents() { return financialTransactionEvents; }
        public void setFinancialTransactionEvents(List<AuditEvent> financialTransactionEvents) { this.financialTransactionEvents = financialTransactionEvents; }
        public List<AuditEvent> getConfigurationChangeEvents() { return configurationChangeEvents; }
        public void setConfigurationChangeEvents(List<AuditEvent> configurationChangeEvents) { this.configurationChangeEvents = configurationChangeEvents; }
        public List<AuditEvent> getAccessGrantEvents() { return accessGrantEvents; }
        public void setAccessGrantEvents(List<AuditEvent> accessGrantEvents) { this.accessGrantEvents = accessGrantEvents; }
        public List<AuditEvent> getPrivilegedAccessEvents() { return privilegedAccessEvents; }
        public void setPrivilegedAccessEvents(List<AuditEvent> privilegedAccessEvents) { this.privilegedAccessEvents = privilegedAccessEvents; }
        public double getInternalControlScore() { return internalControlScore; }
        public void setInternalControlScore(double internalControlScore) { this.internalControlScore = internalControlScore; }
        public double getChangeManagementScore() { return changeManagementScore; }
        public void setChangeManagementScore(double changeManagementScore) { this.changeManagementScore = changeManagementScore; }
        public double getAccessManagementScore() { return accessManagementScore; }
        public void setAccessManagementScore(double accessManagementScore) { this.accessManagementScore = accessManagementScore; }
        public List<String> getControlRecommendations() { return controlRecommendations; }
        public void setControlRecommendations(List<String> controlRecommendations) { this.controlRecommendations = controlRecommendations; }
    }
    
    public static class ComplianceDashboard {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime generatedAt;
        private ComplianceSummary gdprSummary;
        private ComplianceSummary pciDssSummary;
        private ComplianceSummary soxSummary;
        private ComplianceSummary hipaaSummary;
        private double overallComplianceScore;
        private Map<String, Object> complianceTrends;
        private List<Map<String, Object>> riskIndicators;
        
        public void setReportPeriod(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public ComplianceSummary getGdprSummary() { return gdprSummary; }
        public void setGdprSummary(ComplianceSummary gdprSummary) { this.gdprSummary = gdprSummary; }
        public ComplianceSummary getPciDssSummary() { return pciDssSummary; }
        public void setPciDssSummary(ComplianceSummary pciDssSummary) { this.pciDssSummary = pciDssSummary; }
        public ComplianceSummary getSoxSummary() { return soxSummary; }
        public void setSoxSummary(ComplianceSummary soxSummary) { this.soxSummary = soxSummary; }
        public ComplianceSummary getHipaaSummary() { return hipaaSummary; }
        public void setHipaaSummary(ComplianceSummary hipaaSummary) { this.hipaaSummary = hipaaSummary; }
        public double getOverallComplianceScore() { return overallComplianceScore; }
        public void setOverallComplianceScore(double overallComplianceScore) { this.overallComplianceScore = overallComplianceScore; }
        public Map<String, Object> getComplianceTrends() { return complianceTrends; }
        public void setComplianceTrends(Map<String, Object> complianceTrends) { this.complianceTrends = complianceTrends; }
        public List<Map<String, Object>> getRiskIndicators() { return riskIndicators; }
        public void setRiskIndicators(List<Map<String, Object>> riskIndicators) { this.riskIndicators = riskIndicators; }
    }
    
    public static class ComplianceSummary {
        private String complianceType;
        private int totalEvents;
        private long compliantEvents;
        private double complianceScore;
        
        // Getters and setters
        public String getComplianceType() { return complianceType; }
        public void setComplianceType(String complianceType) { this.complianceType = complianceType; }
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        public long getCompliantEvents() { return compliantEvents; }
        public void setCompliantEvents(long compliantEvents) { this.compliantEvents = compliantEvents; }
        public double getComplianceScore() { return complianceScore; }
        public void setComplianceScore(double complianceScore) { this.complianceScore = complianceScore; }
    }
}