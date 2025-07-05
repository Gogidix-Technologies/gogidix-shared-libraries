package com.gogidix.ecosystem.shared.audit;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for audit logging in the Exalt Social E-commerce Ecosystem.
 * Provides methods for recording audit events with various levels of detail and async support.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public interface AuditLogger {
    
    /**
     * Logs an audit event synchronously.
     * 
     * @param event Audit event to log
     */
    void audit(AuditEvent event);
    
    /**
     * Logs an audit event asynchronously.
     * 
     * @param event Audit event to log
     * @return Future representing the logging operation
     */
    CompletableFuture<Void> auditAsync(AuditEvent event);
    
    /**
     * Logs a simple audit event.
     * 
     * @param action Action being audited
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param userId User performing the action
     */
    void audit(AuditEvent.AuditAction action, String resourceType, String resourceId, String userId);
    
    /**
     * Logs a simple audit event asynchronously.
     * 
     * @param action Action being audited
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param userId User performing the action
     * @return Future representing the logging operation
     */
    CompletableFuture<Void> auditAsync(AuditEvent.AuditAction action, String resourceType, String resourceId, String userId);
    
    /**
     * Logs an audit event with additional context.
     * 
     * @param action Action being audited
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param userId User performing the action
     * @param description Description of the action
     * @param metadata Additional metadata
     */
    void audit(AuditEvent.AuditAction action, String resourceType, String resourceId, 
               String userId, String description, Map<String, Object> metadata);
    
    /**
     * Logs a security-related audit event.
     * 
     * @param action Security action
     * @param userId User ID
     * @param ipAddress IP address
     * @param userAgent User agent
     * @param success Whether the action succeeded
     * @param description Description of the security event
     */
    void auditSecurity(AuditEvent.AuditAction action, String userId, String ipAddress, 
                      String userAgent, boolean success, String description);
    
    /**
     * Logs a compliance-related audit event.
     * 
     * @param action Compliance action
     * @param resourceType Type of resource
     * @param resourceId Resource identifier
     * @param userId User performing the action
     * @param complianceType Type of compliance (GDPR, PCI_DSS, etc.)
     * @param complianceData Compliance-specific data
     */
    void auditCompliance(AuditEvent.AuditAction action, String resourceType, String resourceId,
                        String userId, String complianceType, Map<String, Object> complianceData);
    
    /**
     * Logs a data access audit event.
     * 
     * @param resourceType Type of data accessed
     * @param resourceId Resource identifier
     * @param userId User accessing the data
     * @param accessType Type of access (READ, EXPORT, etc.)
     * @param dataClassification Data classification level
     */
    void auditDataAccess(String resourceType, String resourceId, String userId, 
                        String accessType, String dataClassification);
    
    /**
     * Logs an API request audit event.
     * 
     * @param httpMethod HTTP method
     * @param requestUri Request URI
     * @param userId User making the request
     * @param ipAddress IP address
     * @param userAgent User agent
     * @param httpStatus HTTP response status
     * @param durationMs Request duration in milliseconds
     */
    void auditApiRequest(String httpMethod, String requestUri, String userId, 
                        String ipAddress, String userAgent, int httpStatus, long durationMs);
    
    /**
     * Logs a business process audit event.
     * 
     * @param processName Name of the business process
     * @param processStep Current step in the process
     * @param resourceId Resource being processed
     * @param userId User initiating the process
     * @param processData Process-specific data
     * @param success Whether the step succeeded
     */
    void auditBusinessProcess(String processName, String processStep, String resourceId,
                             String userId, Map<String, Object> processData, boolean success);
    
    /**
     * Logs a configuration change audit event.
     * 
     * @param configType Type of configuration changed
     * @param configKey Configuration key
     * @param oldValue Previous value
     * @param newValue New value
     * @param userId User making the change
     * @param reason Reason for the change
     */
    void auditConfigChange(String configType, String configKey, Object oldValue, 
                          Object newValue, String userId, String reason);
    
    /**
     * Logs an error or exception audit event.
     * 
     * @param action Action that caused the error
     * @param resourceType Type of resource involved
     * @param resourceId Resource identifier
     * @param userId User involved in the error
     * @param exception Exception that occurred
     * @param additionalContext Additional context information
     */
    void auditError(AuditEvent.AuditAction action, String resourceType, String resourceId,
                   String userId, Throwable exception, Map<String, Object> additionalContext);
    
    /**
     * Starts an audit context for tracking related audit events.
     * 
     * @param correlationId Correlation ID for grouping events
     * @param userId User ID for the context
     * @param sessionId Session ID for the context
     * @return Audit context
     */
    AuditContext startAuditContext(String correlationId, String userId, String sessionId);
    
    /**
     * Searches audit events based on criteria.
     * 
     * @param criteria Search criteria
     * @return Search results
     */
    AuditSearchResult searchAuditEvents(AuditSearchCriteria criteria);
    
    /**
     * Gets audit statistics for a time period.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @param groupBy Field to group statistics by
     * @return Audit statistics
     */
    AuditStatistics getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy);
    
    /**
     * Exports audit events for compliance reporting.
     * 
     * @param criteria Export criteria
     * @param format Export format (JSON, CSV, XML)
     * @return Export result with download information
     */
    CompletableFuture<AuditExportResult> exportAuditEvents(AuditSearchCriteria criteria, String format);
    
    /**
     * Audit context for tracking related events.
     */
    interface AuditContext extends AutoCloseable {
        String getCorrelationId();
        String getUserId();
        String getSessionId();
        void setTraceId(String traceId);
        void addContextData(String key, Object value);
        void audit(AuditEvent.AuditAction action, String resourceType, String resourceId);
        void audit(AuditEvent.AuditAction action, String resourceType, String resourceId, String description);
        CompletableFuture<Void> auditAsync(AuditEvent.AuditAction action, String resourceType, String resourceId);
        
        @Override
        void close();
    }
    
    /**
     * Search criteria for audit events.
     */
    class AuditSearchCriteria {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String userId;
        private String resourceType;
        private String resourceId;
        private AuditEvent.AuditAction action;
        private AuditEvent.AuditSeverity severity;
        private String serviceName;
        private String category;
        private Boolean success;
        private String ipAddress;
        private String correlationId;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
        private int page = 0;
        private int size = 100;
        private String sortBy = "timestamp";
        private String sortDirection = "DESC";
        
        // Getters and setters with fluent interface
        public LocalDateTime getStartTime() { return startTime; }
        public AuditSearchCriteria setStartTime(LocalDateTime startTime) { 
            this.startTime = startTime; return this; 
        }
        
        public LocalDateTime getEndTime() { return endTime; }
        public AuditSearchCriteria setEndTime(LocalDateTime endTime) { 
            this.endTime = endTime; return this; 
        }
        
        public String getUserId() { return userId; }
        public AuditSearchCriteria setUserId(String userId) { 
            this.userId = userId; return this; 
        }
        
        public String getResourceType() { return resourceType; }
        public AuditSearchCriteria setResourceType(String resourceType) { 
            this.resourceType = resourceType; return this; 
        }
        
        public String getResourceId() { return resourceId; }
        public AuditSearchCriteria setResourceId(String resourceId) { 
            this.resourceId = resourceId; return this; 
        }
        
        public AuditEvent.AuditAction getAction() { return action; }
        public AuditSearchCriteria setAction(AuditEvent.AuditAction action) { 
            this.action = action; return this; 
        }
        
        public AuditEvent.AuditSeverity getSeverity() { return severity; }
        public AuditSearchCriteria setSeverity(AuditEvent.AuditSeverity severity) { 
            this.severity = severity; return this; 
        }
        
        public String getServiceName() { return serviceName; }
        public AuditSearchCriteria setServiceName(String serviceName) { 
            this.serviceName = serviceName; return this; 
        }
        
        public String getCategory() { return category; }
        public AuditSearchCriteria setCategory(String category) { 
            this.category = category; return this; 
        }
        
        public Boolean getSuccess() { return success; }
        public AuditSearchCriteria setSuccess(Boolean success) { 
            this.success = success; return this; 
        }
        
        public String getIpAddress() { return ipAddress; }
        public AuditSearchCriteria setIpAddress(String ipAddress) { 
            this.ipAddress = ipAddress; return this; 
        }
        
        public String getCorrelationId() { return correlationId; }
        public AuditSearchCriteria setCorrelationId(String correlationId) { 
            this.correlationId = correlationId; return this; 
        }
        
        public Map<String, String> getTags() { return tags; }
        public AuditSearchCriteria setTags(Map<String, String> tags) { 
            this.tags = tags; return this; 
        }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public AuditSearchCriteria setMetadata(Map<String, Object> metadata) { 
            this.metadata = metadata; return this; 
        }
        
        public int getPage() { return page; }
        public AuditSearchCriteria setPage(int page) { 
            this.page = page; return this; 
        }
        
        public int getSize() { return size; }
        public AuditSearchCriteria setSize(int size) { 
            this.size = size; return this; 
        }
        
        public String getSortBy() { return sortBy; }
        public AuditSearchCriteria setSortBy(String sortBy) { 
            this.sortBy = sortBy; return this; 
        }
        
        public String getSortDirection() { return sortDirection; }
        public AuditSearchCriteria setSortDirection(String sortDirection) { 
            this.sortDirection = sortDirection; return this; 
        }
        
        // Convenience methods
        public AuditSearchCriteria timeRange(LocalDateTime start, LocalDateTime end) {
            return setStartTime(start).setEndTime(end);
        }
        
        public AuditSearchCriteria forUser(String userId) {
            return setUserId(userId);
        }
        
        public AuditSearchCriteria forResource(String resourceType, String resourceId) {
            return setResourceType(resourceType).setResourceId(resourceId);
        }
        
        public AuditSearchCriteria withAction(AuditEvent.AuditAction action) {
            return setAction(action);
        }
        
        public AuditSearchCriteria withSeverity(AuditEvent.AuditSeverity severity) {
            return setSeverity(severity);
        }
        
        public AuditSearchCriteria successfulOnly() {
            return setSuccess(true);
        }
        
        public AuditSearchCriteria failedOnly() {
            return setSuccess(false);
        }
        
        public AuditSearchCriteria pagination(int page, int size) {
            return setPage(page).setSize(size);
        }
        
        public AuditSearchCriteria sortBy(String field, String direction) {
            return setSortBy(field).setSortDirection(direction);
        }
    }
    
    /**
     * Result of an audit search operation.
     */
    interface AuditSearchResult {
        java.util.List<AuditEvent> getEvents();
        long getTotalElements();
        int getTotalPages();
        int getCurrentPage();
        int getPageSize();
        boolean hasNext();
        boolean hasPrevious();
    }
    
    /**
     * Audit statistics for reporting.
     */
    interface AuditStatistics {
        long getTotalEvents();
        long getSuccessfulEvents();
        long getFailedEvents();
        Map<String, Long> getEventsByAction();
        Map<String, Long> getEventsBySeverity();
        Map<String, Long> getEventsByService();
        Map<String, Long> getEventsByUser();
        Map<String, Long> getEventsByHour();
        Map<String, Long> getEventsByDay();
        double getSuccessRate();
        double getAverageResponseTime();
    }
    
    /**
     * Result of an audit export operation.
     */
    interface AuditExportResult {
        String getExportId();
        String getFormat();
        String getFileName();
        long getFileSize();
        String getDownloadUrl();
        LocalDateTime getCreatedAt();
        LocalDateTime getExpiresAt();
        int getRecordCount();
        boolean isReady();
        String getStatus();
    }
}