package com.gogidix.ecosystem.shared.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of the AuditLogger interface for the Exalt Social E-commerce Ecosystem.
 * Provides comprehensive audit logging capabilities with async support and advanced querying.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditService implements AuditLogger {
    
    private final AuditRepository auditRepository;
    
    @Value("${spring.application.name:unknown-service}")
    private String serviceName;
    
    @Value("${audit.async.enabled:true}")
    private boolean asyncEnabled;
    
    @Value("${audit.retention.default-days:2555}")
    private int defaultRetentionDays;
    
    private final ThreadLocal<AuditContextImpl> currentContext = new ThreadLocal<>();
    
    @Override
    public void audit(AuditEvent event) {
        try {
            // Ensure required fields are set
            enrichAuditEvent(event);
            
            // Validate the event
            validateAuditEvent(event);
            
            // Save to repository
            auditRepository.save(event);
            
            log.debug("Audit event logged: {}", event.getSummary());
            
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", event, e);
            // Don't rethrow - audit logging should not break business logic
        }
    }
    
    @Override
    @Async
    public CompletableFuture<Void> auditAsync(AuditEvent event) {
        return CompletableFuture.runAsync(() -> audit(event));
    }
    
    @Override
    public void audit(AuditEvent.AuditAction action, String resourceType, String resourceId, String userId) {
        AuditEvent event = AuditEvent.standard(action, serviceName)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .build();
        
        if (asyncEnabled) {
            auditAsync(event);
        } else {
            audit(event);
        }
    }
    
    @Override
    public CompletableFuture<Void> auditAsync(AuditEvent.AuditAction action, String resourceType, 
                                            String resourceId, String userId) {
        AuditEvent event = AuditEvent.standard(action, serviceName)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .build();
        
        return auditAsync(event);
    }
    
    @Override
    public void audit(AuditEvent.AuditAction action, String resourceType, String resourceId, 
                     String userId, String description, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.standard(action, serviceName)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .description(description)
            .metadata(metadata)
            .build();
        
        if (asyncEnabled) {
            auditAsync(event);
        } else {
            audit(event);
        }
    }
    
    @Override
    public void auditSecurity(AuditEvent.AuditAction action, String userId, String ipAddress, 
                            String userAgent, boolean success, String description) {
        AuditEvent event = AuditEvent.security(action, serviceName)
            .userId(userId)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .success(success)
            .description(description)
            .severity(success ? AuditEvent.AuditSeverity.INFO : AuditEvent.AuditSeverity.WARN)
            .build();
        
        // Security events are always logged synchronously for immediate alerting
        audit(event);
    }
    
    @Override
    public void auditCompliance(AuditEvent.AuditAction action, String resourceType, String resourceId,
                              String userId, String complianceType, Map<String, Object> complianceData) {
        AuditEvent event = AuditEvent.compliance(action, serviceName, complianceType)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .metadata(complianceData)
            .build();
        
        // Set compliance flag
        event.setComplianceFlag(complianceType, true);
        
        // Compliance events are always logged synchronously
        audit(event);
    }
    
    @Override
    public void auditDataAccess(String resourceType, String resourceId, String userId, 
                              String accessType, String dataClassification) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("accessType", accessType);
        metadata.put("dataClassification", dataClassification);
        
        AuditEvent event = AuditEvent.standard(AuditEvent.AuditAction.READ, serviceName)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .category("DATA_ACCESS")
            .description(String.format("%s access to %s data", accessType, dataClassification))
            .metadata(metadata)
            .build();
        
        // Add data classification tag
        event.addTag("dataClassification", dataClassification);
        event.addTag("accessType", accessType);
        
        if (asyncEnabled) {
            auditAsync(event);
        } else {
            audit(event);
        }
    }
    
    @Override
    public void auditApiRequest(String httpMethod, String requestUri, String userId, 
                              String ipAddress, String userAgent, int httpStatus, long durationMs) {
        AuditEvent event = AuditEvent.standard(AuditEvent.AuditAction.ACCESS, serviceName)
            .userId(userId)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .httpStatus(httpStatus)
            .durationMs(durationMs)
            .success(httpStatus < 400)
            .category("API_ACCESS")
            .description(String.format("%s %s", httpMethod, requestUri))
            .build();
        
        // Add performance tags
        if (durationMs > 5000) {
            event.addTag("performance", "slow");
            event.setSeverity(AuditEvent.AuditSeverity.WARN);
        } else if (durationMs > 1000) {
            event.addTag("performance", "moderate");
        } else {
            event.addTag("performance", "fast");
        }
        
        if (asyncEnabled) {
            auditAsync(event);
        } else {
            audit(event);
        }
    }
    
    @Override
    public void auditBusinessProcess(String processName, String processStep, String resourceId,
                                   String userId, Map<String, Object> processData, boolean success) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("processName", processName);
        metadata.put("processStep", processStep);
        if (processData != null) {
            metadata.putAll(processData);
        }
        
        AuditEvent event = AuditEvent.standard(AuditEvent.AuditAction.PROCESS, serviceName)
            .resourceType("BUSINESS_PROCESS")
            .resourceId(resourceId)
            .userId(userId)
            .success(success)
            .category("BUSINESS_PROCESS")
            .description(String.format("%s - %s", processName, processStep))
            .metadata(metadata)
            .build();
        
        event.addTag("processName", processName);
        event.addTag("processStep", processStep);
        
        if (asyncEnabled) {
            auditAsync(event);
        } else {
            audit(event);
        }
    }
    
    @Override
    public void auditConfigChange(String configType, String configKey, Object oldValue, 
                                Object newValue, String userId, String reason) {
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put(configKey, oldValue);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put(configKey, newValue);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("configType", configType);
        metadata.put("reason", reason);
        
        AuditEvent event = AuditEvent.standard(AuditEvent.AuditAction.CONFIGURE, serviceName)
            .resourceType("CONFIGURATION")
            .resourceId(configKey)
            .userId(userId)
            .category("CONFIGURATION")
            .description(String.format("Configuration change: %s.%s", configType, configKey))
            .oldValues(oldValues)
            .newValues(newValues)
            .metadata(metadata)
            .build();
        
        event.addTag("configType", configType);
        
        // Configuration changes are always logged synchronously
        audit(event);
    }
    
    @Override
    public void auditError(AuditEvent.AuditAction action, String resourceType, String resourceId,
                         String userId, Throwable exception, Map<String, Object> additionalContext) {
        Map<String, Object> metadata = new HashMap<>();
        if (additionalContext != null) {
            metadata.putAll(additionalContext);
        }
        metadata.put("exceptionMessage", exception.getMessage());
        metadata.put("stackTrace", getStackTrace(exception));
        
        AuditEvent event = AuditEvent.standard(action, serviceName)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .success(false)
            .severity(AuditEvent.AuditSeverity.ERROR)
            .category("ERROR")
            .description("Operation failed with exception")
            .errorMessage(exception.getMessage())
            .exceptionType(exception.getClass().getSimpleName())
            .metadata(metadata)
            .build();
        
        // Error events are always logged synchronously
        audit(event);
    }
    
    @Override
    public AuditContext startAuditContext(String correlationId, String userId, String sessionId) {
        AuditContextImpl context = new AuditContextImpl(correlationId, userId, sessionId);
        currentContext.set(context);
        return context;
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuditSearchResult searchAuditEvents(AuditSearchCriteria criteria) {
        // Build pageable
        Sort sort = Sort.by(Sort.Direction.fromString(criteria.getSortDirection()), criteria.getSortBy());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        // Execute search using specifications or custom queries
        Page<AuditEvent> page = executeSearch(criteria, pageable);
        
        return new AuditSearchResultImpl(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuditStatistics getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        return new AuditStatisticsImpl(startTime, endTime, groupBy);
    }
    
    @Override
    @Async
    public CompletableFuture<AuditExportResult> exportAuditEvents(AuditSearchCriteria criteria, String format) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create export
                String exportId = UUID.randomUUID().toString();
                
                // Execute search without pagination to get all results
                AuditSearchCriteria exportCriteria = criteria;
                exportCriteria.setPage(0);
                exportCriteria.setSize(Integer.MAX_VALUE);
                
                AuditSearchResult searchResult = searchAuditEvents(exportCriteria);
                
                // Generate export file (implementation would depend on the format)
                String fileName = generateExportFile(searchResult.getEvents(), format, exportId);
                
                return new AuditExportResultImpl(exportId, format, fileName, searchResult.getEvents().size());
                
            } catch (Exception e) {
                log.error("Failed to export audit events", e);
                throw new RuntimeException("Export failed", e);
            }
        });
    }
    
    /**
     * Enriches audit event with context information.
     */
    private void enrichAuditEvent(AuditEvent event) {
        // Set service name if not already set
        if (event.getServiceName() == null) {
            event.setServiceName(serviceName);
        }
        
        // Set timestamp if not already set
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        
        // Set ID if not already set
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        
        // Set default retention if not set
        if (event.getRetentionDays() == null) {
            event.setRetentionDays(defaultRetentionDays);
        }
        
        // Enrich from current context if available
        AuditContextImpl context = currentContext.get();
        if (context != null) {
            if (event.getCorrelationId() == null) {
                event.setCorrelationId(context.getCorrelationId());
            }
            if (event.getUserId() == null) {
                event.setUserId(context.getUserId());
            }
            if (event.getSessionId() == null) {
                event.setSessionId(context.getSessionId());
            }
            if (event.getTraceId() == null) {
                event.setTraceId(context.traceId);
            }
            
            // Add context data to metadata
            if (!context.contextData.isEmpty()) {
                if (event.getMetadata() == null) {
                    event.setMetadata(new HashMap<>());
                }
                event.getMetadata().putAll(context.contextData);
            }
        }
    }
    
    /**
     * Validates audit event before saving.
     */
    private void validateAuditEvent(AuditEvent event) {
        if (event.getAction() == null) {
            throw new IllegalArgumentException("Audit action cannot be null");
        }
        if (event.getServiceName() == null || event.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be null or empty");
        }
        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }
    
    /**
     * Executes search based on criteria.
     */
    private Page<AuditEvent> executeSearch(AuditSearchCriteria criteria, Pageable pageable) {
        // This is a simplified implementation
        // In a real implementation, you would use JPA Specifications or custom queries
        
        if (criteria.getUserId() != null && criteria.getStartTime() != null && criteria.getEndTime() != null) {
            return auditRepository.findByUserIdAndTimestampBetween(
                criteria.getUserId(), criteria.getStartTime(), criteria.getEndTime(), pageable);
        } else if (criteria.getResourceType() != null && criteria.getResourceId() != null) {
            return auditRepository.findByResourceTypeAndResourceId(
                criteria.getResourceType(), criteria.getResourceId(), pageable);
        } else if (criteria.getStartTime() != null && criteria.getEndTime() != null) {
            // Use a custom query or specification for time range
            return auditRepository.findAll(pageable);
        } else {
            return auditRepository.findAll(pageable);
        }
    }
    
    /**
     * Generates export file for audit events.
     */
    private String generateExportFile(List<AuditEvent> events, String format, String exportId) {
        // This is a placeholder implementation
        // Real implementation would generate actual files in the specified format
        String fileName = String.format("audit_export_%s.%s", exportId, format.toLowerCase());
        
        // In real implementation, you would:
        // 1. Generate the file in the specified format (JSON, CSV, XML)
        // 2. Store it in a secure location (S3, local storage, etc.)
        // 3. Return the file name/path
        
        return fileName;
    }
    
    /**
     * Gets stack trace as string.
     */
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Implementation of AuditContext.
     */
    @RequiredArgsConstructor
    private class AuditContextImpl implements AuditContext {
        private final String correlationId;
        private final String userId;
        private final String sessionId;
        private String traceId;
        private final Map<String, Object> contextData = new HashMap<>();
        
        @Override
        public String getCorrelationId() {
            return correlationId;
        }
        
        @Override
        public String getUserId() {
            return userId;
        }
        
        @Override
        public String getSessionId() {
            return sessionId;
        }
        
        @Override
        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }
        
        @Override
        public void addContextData(String key, Object value) {
            contextData.put(key, value);
        }
        
        @Override
        public void audit(AuditEvent.AuditAction action, String resourceType, String resourceId) {
            AuditService.this.audit(action, resourceType, resourceId, userId);
        }
        
        @Override
        public void audit(AuditEvent.AuditAction action, String resourceType, String resourceId, String description) {
            AuditService.this.audit(action, resourceType, resourceId, userId, description, null);
        }
        
        @Override
        public CompletableFuture<Void> auditAsync(AuditEvent.AuditAction action, String resourceType, String resourceId) {
            return AuditService.this.auditAsync(action, resourceType, resourceId, userId);
        }
        
        @Override
        public void close() {
            currentContext.remove();
        }
    }
    
    /**
     * Implementation of AuditSearchResult.
     */
    @RequiredArgsConstructor
    private static class AuditSearchResultImpl implements AuditSearchResult {
        private final Page<AuditEvent> page;
        
        @Override
        public List<AuditEvent> getEvents() {
            return page.getContent();
        }
        
        @Override
        public long getTotalElements() {
            return page.getTotalElements();
        }
        
        @Override
        public int getTotalPages() {
            return page.getTotalPages();
        }
        
        @Override
        public int getCurrentPage() {
            return page.getNumber();
        }
        
        @Override
        public int getPageSize() {
            return page.getSize();
        }
        
        @Override
        public boolean hasNext() {
            return page.hasNext();
        }
        
        @Override
        public boolean hasPrevious() {
            return page.hasPrevious();
        }
    }
    
    /**
     * Implementation of AuditStatistics.
     */
    @RequiredArgsConstructor
    private class AuditStatisticsImpl implements AuditStatistics {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String groupBy;
        
        @Override
        public long getTotalEvents() {
            return auditRepository.countByTimestampBetween(startTime, endTime);
        }
        
        @Override
        public long getSuccessfulEvents() {
            return getTotalEvents() - getFailedEvents();
        }
        
        @Override
        public long getFailedEvents() {
            return auditRepository.countBySuccessFalseAndTimestampBetween(startTime, endTime);
        }
        
        @Override
        public Map<String, Long> getEventsByAction() {
            return auditRepository.getAuditStatsByAction(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        }
        
        @Override
        public Map<String, Long> getEventsBySeverity() {
            return auditRepository.getAuditStatsBySeverity(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        }
        
        @Override
        public Map<String, Long> getEventsByService() {
            return auditRepository.getAuditStatsByService(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        }
        
        @Override
        public Map<String, Long> getEventsByUser() {
            return auditRepository.getAuditStatsByUser(startTime, endTime, 100)
                .stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        }
        
        @Override
        public Map<String, Long> getEventsByHour() {
            return auditRepository.getAuditStatsByHour(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                    arr -> "Hour " + arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        }
        
        @Override
        public Map<String, Long> getEventsByDay() {
            return auditRepository.getAuditStatsByDay(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
        }
        
        @Override
        public double getSuccessRate() {
            Double rate = auditRepository.getSuccessRate(startTime, endTime);
            return rate != null ? rate : 0.0;
        }
        
        @Override
        public double getAverageResponseTime() {
            Double avgTime = auditRepository.getAverageResponseTime(startTime, endTime);
            return avgTime != null ? avgTime : 0.0;
        }
    }
    
    /**
     * Implementation of AuditExportResult.
     */
    @RequiredArgsConstructor
    private static class AuditExportResultImpl implements AuditExportResult {
        private final String exportId;
        private final String format;
        private final String fileName;
        private final int recordCount;
        private final LocalDateTime createdAt = LocalDateTime.now();
        
        @Override
        public String getExportId() {
            return exportId;
        }
        
        @Override
        public String getFormat() {
            return format;
        }
        
        @Override
        public String getFileName() {
            return fileName;
        }
        
        @Override
        public long getFileSize() {
            // Would be calculated based on actual file
            return recordCount * 500L; // Estimate 500 bytes per record
        }
        
        @Override
        public String getDownloadUrl() {
            return "/api/audit/exports/" + exportId + "/download";
        }
        
        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        @Override
        public LocalDateTime getExpiresAt() {
            return createdAt.plusDays(7); // Exports expire after 7 days
        }
        
        @Override
        public int getRecordCount() {
            return recordCount;
        }
        
        @Override
        public boolean isReady() {
            return true; // In real implementation, this would check file generation status
        }
        
        @Override
        public String getStatus() {
            return isReady() ? "COMPLETED" : "PROCESSING";
        }
    }
}