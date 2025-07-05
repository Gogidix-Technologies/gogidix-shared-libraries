package com.gogidix.ecosystem.shared.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository interface for audit events in the Exalt Social E-commerce Ecosystem.
 * Provides data access methods for audit event storage and retrieval with advanced querying capabilities.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Repository
public interface AuditRepository extends JpaRepository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {
    
    /**
     * Finds audit events by user ID within a time range.
     * 
     * @param userId User ID
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findByUserIdAndTimestampBetween(String userId, LocalDateTime startTime, 
                                                    LocalDateTime endTime, Pageable pageable);
    
    /**
     * Finds audit events by resource type and ID.
     * 
     * @param resourceType Resource type
     * @param resourceId Resource ID
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findByResourceTypeAndResourceId(String resourceType, String resourceId, Pageable pageable);
    
    /**
     * Finds audit events by correlation ID.
     * 
     * @param correlationId Correlation ID
     * @return List of related audit events
     */
    List<AuditEvent> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    
    /**
     * Finds audit events by session ID.
     * 
     * @param sessionId Session ID
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findBySessionIdOrderByTimestampDesc(String sessionId, Pageable pageable);
    
    /**
     * Finds audit events by action and success status.
     * 
     * @param action Audit action
     * @param success Success status
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findByActionAndSuccess(AuditEvent.AuditAction action, Boolean success, Pageable pageable);
    
    /**
     * Finds audit events by severity level.
     * 
     * @param severity Severity level
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findBySeverityAndTimestampBetween(AuditEvent.AuditSeverity severity, 
                                                      LocalDateTime startTime, LocalDateTime endTime, 
                                                      Pageable pageable);
    
    /**
     * Finds audit events by service name within a time range.
     * 
     * @param serviceName Service name
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findByServiceNameAndTimestampBetween(String serviceName, LocalDateTime startTime, 
                                                         LocalDateTime endTime, Pageable pageable);
    
    /**
     * Finds failed audit events within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of failed audit events
     */
    Page<AuditEvent> findBySuccessFalseAndTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, 
                                                          Pageable pageable);
    
    /**
     * Finds security-sensitive audit events.
     * 
     * @param actions Security-related actions
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of security audit events
     */
    Page<AuditEvent> findByActionInAndTimestampBetween(List<AuditEvent.AuditAction> actions, 
                                                      LocalDateTime startTime, LocalDateTime endTime, 
                                                      Pageable pageable);
    
    /**
     * Finds audit events by IP address.
     * 
     * @param ipAddress IP address
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of audit events
     */
    Page<AuditEvent> findByIpAddressAndTimestampBetween(String ipAddress, LocalDateTime startTime, 
                                                       LocalDateTime endTime, Pageable pageable);
    
    /**
     * Counts audit events by user within a time range.
     * 
     * @param userId User ID
     * @param startTime Start time
     * @param endTime End time
     * @return Count of audit events
     */
    long countByUserIdAndTimestampBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Counts failed audit events within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Count of failed audit events
     */
    long countBySuccessFalseAndTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Counts audit events within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Count of audit events
     */
    long countByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Gets audit statistics by action within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Map of action counts
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime GROUP BY a.action")
    List<Object[]> getAuditStatsByAction(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Gets audit statistics by severity within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Map of severity counts
     */
    @Query("SELECT a.severity, COUNT(a) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime GROUP BY a.severity")
    List<Object[]> getAuditStatsBySeverity(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * Gets audit statistics by service within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Map of service counts
     */
    @Query("SELECT a.serviceName, COUNT(a) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime GROUP BY a.serviceName")
    List<Object[]> getAuditStatsByService(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Gets audit statistics by user within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @param limit Maximum number of users to return
     * @return Map of user counts
     */
    @Query("SELECT a.userId, COUNT(a) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime AND a.userId IS NOT NULL GROUP BY a.userId ORDER BY COUNT(a) DESC LIMIT :limit")
    List<Object[]> getAuditStatsByUser(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime, 
                                      @Param("limit") int limit);
    
    /**
     * Gets audit statistics by hour within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Map of hourly counts
     */
    @Query("SELECT HOUR(a.timestamp), COUNT(a) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime GROUP BY HOUR(a.timestamp) ORDER BY HOUR(a.timestamp)")
    List<Object[]> getAuditStatsByHour(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * Gets audit statistics by day within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Map of daily counts
     */
    @Query("SELECT DATE(a.timestamp), COUNT(a) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime GROUP BY DATE(a.timestamp) ORDER BY DATE(a.timestamp)")
    List<Object[]> getAuditStatsByDay(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * Gets average response time for audit events within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Average response time in milliseconds
     */
    @Query("SELECT AVG(a.durationMs) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime AND a.durationMs IS NOT NULL")
    Double getAverageResponseTime(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * Gets success rate for audit events within a time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return Success rate as a percentage
     */
    @Query("SELECT (COUNT(CASE WHEN a.success = true THEN 1 END) * 100.0 / COUNT(a)) FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime")
    Double getSuccessRate(@Param("startTime") LocalDateTime startTime, 
                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Finds audit events for compliance reporting.
     * 
     * @param complianceType Type of compliance
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of compliance audit events
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime AND " +
           "JSON_EXTRACT(a.complianceFlags, :complianceKey) = true")
    Page<AuditEvent> findComplianceAuditEvents(@Param("complianceKey") String complianceKey,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              Pageable pageable);
    
    /**
     * Finds audit events with specific metadata.
     * 
     * @param metadataKey Metadata key
     * @param metadataValue Metadata value
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of audit events
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime AND " +
           "JSON_EXTRACT(a.metadata, :metadataKey) = :metadataValue")
    Page<AuditEvent> findByMetadata(@Param("metadataKey") String metadataKey,
                                   @Param("metadataValue") String metadataValue,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   Pageable pageable);
    
    /**
     * Finds audit events with specific tags.
     * 
     * @param tagKey Tag key
     * @param tagValue Tag value
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of audit events
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime AND " +
           "JSON_EXTRACT(a.tags, :tagKey) = :tagValue")
    Page<AuditEvent> findByTag(@Param("tagKey") String tagKey,
                              @Param("tagValue") String tagValue,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime,
                              Pageable pageable);
    
    /**
     * Deletes old audit events based on retention policy.
     * 
     * @param cutoffDate Date before which events should be deleted
     * @return Number of deleted events
     */
    @Modifying
    @Query("DELETE FROM AuditEvent a WHERE a.timestamp < :cutoffDate AND " +
           "(a.retentionDays IS NULL OR a.timestamp < :cutoffDate - INTERVAL a.retentionDays DAY)")
    int deleteOldAuditEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Archives old audit events to a separate table.
     * 
     * @param cutoffDate Date before which events should be archived
     * @return Number of archived events
     */
    @Modifying
    @Query(value = "INSERT INTO audit_events_archive SELECT * FROM audit_events WHERE timestamp < :cutoffDate", 
           nativeQuery = true)
    int archiveOldAuditEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Gets storage usage statistics for audit events.
     * 
     * @return Storage usage information
     */
    @Query(value = "SELECT " +
           "COUNT(*) as total_events, " +
           "COUNT(CASE WHEN timestamp >= CURRENT_DATE - INTERVAL 1 DAY THEN 1 END) as events_last_day, " +
           "COUNT(CASE WHEN timestamp >= CURRENT_DATE - INTERVAL 7 DAY THEN 1 END) as events_last_week, " +
           "COUNT(CASE WHEN timestamp >= CURRENT_DATE - INTERVAL 30 DAY THEN 1 END) as events_last_month, " +
           "pg_size_pretty(pg_total_relation_size('audit_events')) as table_size " +
           "FROM audit_events", 
           nativeQuery = true)
    Map<String, Object> getStorageStatistics();
    
    /**
     * Checks for suspicious activity patterns.
     * 
     * @param userId User ID to check
     * @param timeWindow Time window in minutes
     * @param threshold Threshold for suspicious activity
     * @return True if suspicious activity is detected
     */
    @Query("SELECT COUNT(a) > :threshold FROM AuditEvent a WHERE a.userId = :userId AND " +
           "a.timestamp >= :startTime AND a.success = false")
    boolean hasSuspiciousActivity(@Param("userId") String userId, 
                                 @Param("startTime") LocalDateTime startTime, 
                                 @Param("threshold") long threshold);
    
    /**
     * Finds recent audit events for real-time monitoring.
     * 
     * @param minutes Number of minutes to look back
     * @param pageable Pagination information
     * @return Page of recent audit events
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp >= :startTime ORDER BY a.timestamp DESC")
    Page<AuditEvent> findRecentAuditEvents(@Param("startTime") LocalDateTime startTime, Pageable pageable);
}