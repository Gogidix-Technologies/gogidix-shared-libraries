package com.gogidix.ecosystem.shared.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Audit event entity representing a trackable action in the Exalt Social E-commerce Ecosystem.
 * Stores comprehensive audit information for compliance, security, and operational monitoring.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "audit_events", 
       indexes = {
           @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
           @Index(name = "idx_audit_user_id", columnList = "userId"),
           @Index(name = "idx_audit_resource", columnList = "resourceType, resourceId"),
           @Index(name = "idx_audit_action", columnList = "action"),
           @Index(name = "idx_audit_service", columnList = "serviceName"),
           @Index(name = "idx_audit_correlation", columnList = "correlationId"),
           @Index(name = "idx_audit_session", columnList = "sessionId"),
           @Index(name = "idx_audit_severity", columnList = "severity")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEvent {
    
    /**
     * Unique identifier for the audit event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    /**
     * Timestamp when the audited action occurred.
     */
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Type of action being audited (CREATE, READ, UPDATE, DELETE, LOGIN, etc.).
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    /**
     * Name of the service that generated this audit event.
     */
    @NotBlank
    @Size(max = 100)
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;
    
    /**
     * Type of resource being audited (User, Product, Order, etc.).
     */
    @Size(max = 100)
    @Column(name = "resource_type", length = 100)
    private String resourceType;
    
    /**
     * Unique identifier of the resource being audited.
     */
    @Size(max = 255)
    @Column(name = "resource_id", length = 255)
    private String resourceId;
    
    /**
     * ID of the user who performed the action.
     */
    @Size(max = 255)
    @Column(name = "user_id", length = 255)
    private String userId;
    
    /**
     * Username of the user who performed the action.
     */
    @Size(max = 255)
    @Column(name = "username", length = 255)
    private String username;
    
    /**
     * Session ID for tracking user sessions.
     */
    @Size(max = 255)
    @Column(name = "session_id", length = 255)
    private String sessionId;
    
    /**
     * IP address from which the action was performed.
     */
    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * User agent string from the client.
     */
    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * HTTP method for web requests (GET, POST, PUT, DELETE, etc.).
     */
    @Size(max = 10)
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    /**
     * Request URI for web requests.
     */
    @Size(max = 1000)
    @Column(name = "request_uri", length = 1000)
    private String requestUri;
    
    /**
     * HTTP status code for web responses.
     */
    @Column(name = "http_status")
    private Integer httpStatus;
    
    /**
     * Duration of the operation in milliseconds.
     */
    @Column(name = "duration_ms")
    private Long durationMs;
    
    /**
     * Correlation ID for tracking related operations across services.
     */
    @Size(max = 255)
    @Column(name = "correlation_id", length = 255)
    private String correlationId;
    
    /**
     * Trace ID for distributed tracing.
     */
    @Size(max = 255)
    @Column(name = "trace_id", length = 255)
    private String traceId;
    
    /**
     * Tenant ID for multi-tenancy support.
     */
    @Size(max = 255)
    @Column(name = "tenant_id", length = 255)
    private String tenantId;
    
    /**
     * Severity level of the audit event.
     */
    @NotNull
    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuditSeverity severity = AuditSeverity.INFO;
    
    /**
     * Category of the audit event for classification.
     */
    @Size(max = 100)
    @Column(name = "category", length = 100)
    private String category;
    
    /**
     * Description of the audited action.
     */
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;
    
    /**
     * Success/failure status of the operation.
     */
    @NotNull
    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;
    
    /**
     * Error message if the operation failed.
     */
    @Size(max = 2000)
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
    
    /**
     * Exception type if the operation failed.
     */
    @Size(max = 255)
    @Column(name = "exception_type", length = 255)
    private String exceptionType;
    
    /**
     * Previous values before the change (for UPDATE operations).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;
    
    /**
     * New values after the change (for CREATE/UPDATE operations).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;
    
    /**
     * Additional metadata for the audit event.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    /**
     * Tags for categorizing and filtering audit events.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, String> tags;
    
    /**
     * Security context information.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "security_context", columnDefinition = "jsonb")
    private Map<String, Object> securityContext;
    
    /**
     * Compliance flags for regulatory requirements.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compliance_flags", columnDefinition = "jsonb")
    private Map<String, Boolean> complianceFlags;
    
    /**
     * Retention period for the audit event in days.
     */
    @Column(name = "retention_days")
    @Builder.Default
    private Integer retentionDays = 2555; // 7 years default
    
    /**
     * Audit action enumeration.
     */
    public enum AuditAction {
        CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, ACCESS, EXPORT, IMPORT, 
        APPROVE, REJECT, CANCEL, SUSPEND, ACTIVATE, DEACTIVATE, RESET,
        GRANT, REVOKE, AUTHENTICATE, AUTHORIZE, VALIDATE, PROCESS, EXECUTE,
        SYNC, BACKUP, RESTORE, MIGRATE, CONFIGURE, DEPLOY, SCALE
    }
    
    /**
     * Audit severity enumeration.
     */
    public enum AuditSeverity {
        TRACE, DEBUG, INFO, WARN, ERROR, CRITICAL
    }
    
    /**
     * Adds a tag to the audit event.
     * 
     * @param key Tag key
     * @param value Tag value
     */
    public void addTag(String key, String value) {
        if (this.tags == null) {
            this.tags = new java.util.HashMap<>();
        }
        this.tags.put(key, value);
    }
    
    /**
     * Adds metadata to the audit event.
     * 
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Adds security context information.
     * 
     * @param key Security context key
     * @param value Security context value
     */
    public void addSecurityContext(String key, Object value) {
        if (this.securityContext == null) {
            this.securityContext = new java.util.HashMap<>();
        }
        this.securityContext.put(key, value);
    }
    
    /**
     * Sets a compliance flag.
     * 
     * @param complianceType Type of compliance (GDPR, PCI_DSS, HIPAA, etc.)
     * @param isCompliant Whether the action is compliant
     */
    public void setComplianceFlag(String complianceType, boolean isCompliant) {
        if (this.complianceFlags == null) {
            this.complianceFlags = new java.util.HashMap<>();
        }
        this.complianceFlags.put(complianceType, isCompliant);
    }
    
    /**
     * Checks if this is a security-sensitive audit event.
     * 
     * @return true if security-sensitive
     */
    public boolean isSecuritySensitive() {
        return action == AuditAction.LOGIN || 
               action == AuditAction.LOGOUT || 
               action == AuditAction.AUTHENTICATE ||
               action == AuditAction.AUTHORIZE ||
               action == AuditAction.GRANT ||
               action == AuditAction.REVOKE ||
               severity == AuditSeverity.CRITICAL ||
               severity == AuditSeverity.ERROR;
    }
    
    /**
     * Checks if this audit event requires long-term retention.
     * 
     * @return true if long-term retention is required
     */
    public boolean requiresLongTermRetention() {
        return complianceFlags != null && (
            Boolean.TRUE.equals(complianceFlags.get("GDPR")) ||
            Boolean.TRUE.equals(complianceFlags.get("PCI_DSS")) ||
            Boolean.TRUE.equals(complianceFlags.get("SOX")) ||
            Boolean.TRUE.equals(complianceFlags.get("HIPAA"))
        );
    }
    
    /**
     * Gets a summary description of the audit event.
     * 
     * @return Summary description
     */
    public String getSummary() {
        return String.format("%s %s on %s%s by %s at %s", 
            action, 
            success ? "succeeded" : "failed",
            resourceType != null ? resourceType : "system",
            resourceId != null ? " (ID: " + resourceId + ")" : "",
            username != null ? username : "system",
            timestamp
        );
    }
    
    /**
     * Creates a builder for audit events with common fields pre-populated.
     * 
     * @param action Audit action
     * @param serviceName Service name
     * @return Builder instance
     */
    public static AuditEventBuilder standard(AuditAction action, String serviceName) {
        return AuditEvent.builder()
            .id(UUID.randomUUID())
            .action(action)
            .serviceName(serviceName)
            .timestamp(LocalDateTime.now())
            .severity(AuditSeverity.INFO)
            .success(true);
    }
    
    /**
     * Creates a builder for security-related audit events.
     * 
     * @param action Security action
     * @param serviceName Service name
     * @return Builder instance
     */
    public static AuditEventBuilder security(AuditAction action, String serviceName) {
        return standard(action, serviceName)
            .severity(AuditSeverity.WARN)
            .category("SECURITY");
    }
    
    /**
     * Creates a builder for compliance-related audit events.
     * 
     * @param action Compliance action
     * @param serviceName Service name
     * @param complianceType Type of compliance
     * @return Builder instance
     */
    public static AuditEventBuilder compliance(AuditAction action, String serviceName, String complianceType) {
        AuditEventBuilder builder = standard(action, serviceName)
            .category("COMPLIANCE")
            .retentionDays(2555); // 7 years
            
        return builder;
    }
}