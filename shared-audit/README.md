# Exalt Shared Audit Library

[![Build Status](https://github.com/exalt-application/social-ecommerce-ecosystem/workflows/Shared%20Libraries%20CI/badge.svg)](https://github.com/exalt-application/social-ecommerce-ecosystem/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=exalt-application_shared-audit&metric=alert_status)](https://sonarcloud.io/dashboard?id=exalt-application_shared-audit)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=exalt-application_shared-audit&metric=coverage)](https://sonarcloud.io/dashboard?id=exalt-application_shared-audit)
[![Maven Central](https://img.shields.io/maven-central/v/com.exalt.ecosystem/shared-audit.svg)](https://search.maven.org/artifact/com.exalt.ecosystem/shared-audit)

## Overview

The **Exalt Shared Audit Library** provides comprehensive audit logging and tracking capabilities for the Exalt Social E-commerce Ecosystem. It enables standardized audit trail management across all microservices with support for compliance, security monitoring, and operational transparency.

## Key Features

### 🔍 Comprehensive Audit Tracking
- **User Actions**: Track user logins, permissions changes, and critical operations
- **Data Changes**: Monitor create, update, delete operations with before/after states
- **System Events**: Log system-level events, errors, and configuration changes
- **Business Events**: Track business-critical operations like orders, payments, and inventory

### 🎯 Advanced Capabilities
- **Real-time Streaming**: Kafka/RabbitMQ integration for real-time audit event processing
- **Structured Logging**: JSON-formatted audit logs with consistent schema
- **Contextual Information**: Automatic capture of user context, session info, and request metadata
- **Compliance Ready**: GDPR, SOX, PCI DSS compliant audit trail generation

### 🔧 Integration Features
- **Spring Boot Auto-Configuration**: Zero-configuration setup with sensible defaults
- **Annotation-based**: Simple `@Auditable` annotation for method-level auditing
- **Aspect-Oriented**: AOP integration for transparent audit logging
- **Database Agnostic**: Support for PostgreSQL, MySQL, MongoDB audit storage

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.exalt.ecosystem</groupId>
    <artifactId>shared-audit</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Enable Audit Configuration

```java
@SpringBootApplication
@EnableExaltAudit
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. Configure Properties

```yaml
exalt:
  audit:
    enabled: true
    async: true
    storage:
      type: database # database, kafka, rabbitmq
    retention:
      days: 2555 # 7 years for compliance
    include-request-body: true
    include-response-body: false
```

### 4. Use Audit Annotations

```java
@Service
public class UserService {
    
    @Auditable(
        action = "USER_LOGIN",
        resource = "User",
        description = "User authentication attempt"
    )
    public AuthResult authenticateUser(String username, String password) {
        // Business logic
        return authResult;
    }
    
    @Auditable(
        action = "USER_UPDATE",
        resource = "User",
        includeReturnValue = true
    )
    public User updateUser(Long userId, UserUpdateRequest request) {
        // Business logic
        return updatedUser;
    }
}
```

## Core Components

### AuditEvent
Central audit event model with comprehensive metadata:

```java
@Entity
@Table(name = "audit_events")
public class AuditEvent {
    private String eventId;
    private String eventType;
    private String action;
    private String resource;
    private String resourceId;
    private String userId;
    private String sessionId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private Object beforeState;
    private Object afterState;
    private Map<String, Object> metadata;
    private AuditStatus status;
    private String description;
}
```

### AuditService
Core service for manual audit logging:

```java
@Component
public class AuditService {
    
    public void logEvent(AuditEvent event) {
        // Log audit event
    }
    
    public void logUserAction(String action, String resource, Object data) {
        // Log user action with context
    }
    
    public void logDataChange(String resource, String resourceId, 
                             Object before, Object after) {
        // Log data changes
    }
    
    public List<AuditEvent> findEventsByUser(String userId, 
                                           LocalDateTime from, 
                                           LocalDateTime to) {
        // Query audit events
    }
}
```

### AuditableAnnotation
Method-level annotation for declarative auditing:

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
    String resource() default "";
    String description() default "";
    boolean includeParameters() default true;
    boolean includeReturnValue() default false;
    String[] excludeFields() default {};
}
```

## Configuration Options

### Database Storage Configuration

```yaml
exalt:
  audit:
    storage:
      type: database
      datasource:
        url: jdbc:postgresql://localhost:5432/audit_db
        username: audit_user
        password: ${AUDIT_DB_PASSWORD}
        driver-class-name: org.postgresql.Driver
      jpa:
        hibernate:
          ddl-auto: validate
        properties:
          hibernate:
            dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Kafka Integration

```yaml
exalt:
  audit:
    storage:
      type: kafka
      kafka:
        bootstrap-servers: localhost:9092
        topic: audit-events
        producer:
          key-serializer: org.apache.kafka.common.serialization.StringSerializer
          value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### RabbitMQ Integration

```yaml
exalt:
  audit:
    storage:
      type: rabbitmq
      rabbitmq:
        host: localhost
        port: 5672
        username: guest
        password: guest
        exchange: audit.exchange
        routing-key: audit.events
```

## Advanced Usage

### Custom Audit Event Types

```java
public enum AuditEventType {
    USER_LOGIN,
    USER_LOGOUT,
    USER_REGISTRATION,
    PASSWORD_CHANGE,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_CANCELLED,
    PAYMENT_PROCESSED,
    INVENTORY_ADJUSTED,
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    SYSTEM_STARTUP,
    SYSTEM_SHUTDOWN,
    CONFIGURATION_CHANGED,
    DATA_EXPORT,
    DATA_IMPORT
}
```

### Custom Audit Context

```java
@Component
public class CustomAuditContext implements AuditContextProvider {
    
    @Override
    public AuditContext getCurrentContext() {
        return AuditContext.builder()
            .userId(getCurrentUserId())
            .sessionId(getCurrentSessionId())
            .ipAddress(getCurrentIpAddress())
            .userAgent(getCurrentUserAgent())
            .organizationId(getCurrentOrganizationId())
            .tenantId(getCurrentTenantId())
            .build();
    }
}
```

### Audit Event Filtering

```java
@Configuration
public class AuditConfiguration {
    
    @Bean
    public AuditEventFilter auditEventFilter() {
        return AuditEventFilter.builder()
            .excludeActions("HEALTH_CHECK", "METRICS_REQUEST")
            .excludeUsers("system", "monitoring")
            .excludeResources("actuator/**")
            .includeOnlyFailures(false)
            .sensitiveDataMasking(true)
            .build();
    }
}
```

## Compliance Features

### GDPR Compliance
- **Data Anonymization**: Automatic PII masking in audit logs
- **Right to Erasure**: Soft delete with data retention policies
- **Data Portability**: Export audit data in standard formats

### SOX Compliance
- **Immutable Audit Trail**: Cryptographic integrity verification
- **Access Controls**: Role-based access to audit data
- **Retention Policies**: Configurable retention periods

### PCI DSS Compliance
- **Secure Storage**: Encrypted audit data storage
- **Access Monitoring**: Track access to sensitive audit information
- **Log Protection**: Tamper-evident audit logs

## Monitoring and Alerting

### Metrics
- Audit events per second
- Storage utilization
- Event processing latency
- Failed audit attempts

### Health Checks
- Database connectivity
- Message queue health
- Storage capacity
- Event processing status

### Alerts
- Audit storage failures
- Unusual activity patterns
- Compliance violations
- System intrusions

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Performance Tests
```bash
mvn test -Pperformance
```

## API Documentation

Full API documentation is available at:
- **OpenAPI/Swagger**: `/swagger-ui.html` (when included in web application)
- **JavaDoc**: Available in the `target/site/apidocs` directory after building

## Contributing

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/audit-enhancement`)
3. **Commit** your changes (`git commit -am 'Add audit enhancement'`)
4. **Push** to the branch (`git push origin feature/audit-enhancement`)
5. **Create** a Pull Request

## Support

### Documentation
- [Architecture Guide](docs/architecture/audit-architecture.md)
- [Integration Guide](docs/operations/integration-guide.md)
- [Troubleshooting](docs/operations/troubleshooting.md)

### Community
- **Issues**: [GitHub Issues](https://github.com/exalt-application/social-ecommerce-ecosystem/issues)
- **Discussions**: [GitHub Discussions](https://github.com/exalt-application/social-ecommerce-ecosystem/discussions)
- **Support**: [support@exaltapplication.com](mailto:support@exaltapplication.com)

## License

This project is licensed under the **MIT License** - see the [LICENSE](../../LICENSE) file for details.

## Changelog

### v1.0.0-SNAPSHOT
- Initial release
- Core audit functionality
- Database and messaging integration
- Spring Boot auto-configuration
- Compliance features

---

**Exalt Application Limited** - Building the future of social e-commerce  
**Website**: [https://exaltapplication.com](https://exaltapplication.com)