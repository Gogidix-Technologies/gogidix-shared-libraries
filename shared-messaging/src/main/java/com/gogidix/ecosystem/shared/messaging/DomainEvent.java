package com.gogidix.ecosystem.shared.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all domain events in the Exalt Social E-commerce Ecosystem.
 * Provides common event properties and structure for event-driven communication.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class DomainEvent {
    
    /**
     * Unique identifier for this event instance.
     */
    private UUID eventId;
    
    /**
     * Type of the event (e.g., "ORDER_CREATED", "USER_REGISTERED").
     */
    private String eventType;
    
    /**
     * Source service that generated this event.
     */
    private String sourceService;
    
    /**
     * Version of the event schema for backward compatibility.
     */
    private String version;
    
    /**
     * Timestamp when the event was created.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * ID of the aggregate root that this event relates to.
     */
    private String aggregateId;
    
    /**
     * Type of the aggregate (e.g., "Order", "User", "Product").
     */
    private String aggregateType;
    
    /**
     * Sequence number for ordering events related to the same aggregate.
     */
    private Long sequenceNumber;
    
    /**
     * Correlation ID for tracking related events across services.
     */
    private String correlationId;
    
    /**
     * Causation ID linking this event to the command/event that caused it.
     */
    private String causationId;
    
    /**
     * User ID of the person who triggered this event (if applicable).
     */
    private String userId;
    
    /**
     * Session ID for tracking user sessions (if applicable).
     */
    private String sessionId;
    
    /**
     * Tenant ID for multi-tenancy support.
     */
    private String tenantId;
    
    /**
     * Priority level of the event (HIGH, MEDIUM, LOW).
     */
    private EventPriority priority;
    
    /**
     * Tags for categorizing and filtering events.
     */
    private Map<String, String> tags;
    
    /**
     * Additional metadata for the event.
     */
    private Map<String, Object> metadata;
    
    /**
     * Event priority enumeration.
     */
    public enum EventPriority {
        HIGH, MEDIUM, LOW
    }
    
    /**
     * Initializes the event with default values.
     */
    public void initializeEvent() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID();
        }
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
        if (this.priority == null) {
            this.priority = EventPriority.MEDIUM;
        }
        if (this.version == null) {
            this.version = "1.0";
        }
    }
    
    /**
     * Gets a user-friendly event description.
     * 
     * @return Event description
     */
    public String getEventDescription() {
        return String.format("%s event for %s (ID: %s)", 
            this.eventType, this.aggregateType, this.aggregateId);
    }
    
    /**
     * Checks if this event is related to another event by correlation ID.
     * 
     * @param other Other event to compare
     * @return true if events are related
     */
    public boolean isRelatedTo(DomainEvent other) {
        return other != null && 
               this.correlationId != null && 
               this.correlationId.equals(other.getCorrelationId());
    }
    
    /**
     * Checks if this event was caused by another event.
     * 
     * @param other Potential causation event
     * @return true if this event was caused by the other
     */
    public boolean wasCausedBy(DomainEvent other) {
        return other != null && 
               this.causationId != null && 
               this.causationId.equals(other.getEventId().toString());
    }
    
    /**
     * Creates a causation relationship with another event.
     * 
     * @param causationEvent Event that caused this event
     */
    public void setCausationEvent(DomainEvent causationEvent) {
        if (causationEvent != null) {
            this.causationId = causationEvent.getEventId().toString();
            this.correlationId = causationEvent.getCorrelationId();
        }
    }
    
    /**
     * Adds or updates a tag.
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
     * Adds or updates metadata.
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
     * Gets a tag value.
     * 
     * @param key Tag key
     * @return Tag value or null if not found
     */
    public String getTag(String key) {
        return this.tags != null ? this.tags.get(key) : null;
    }
    
    /**
     * Gets a metadata value.
     * 
     * @param key Metadata key
     * @return Metadata value or null if not found
     */
    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
    
    /**
     * Checks if the event has a specific tag.
     * 
     * @param key Tag key
     * @return true if tag exists
     */
    public boolean hasTag(String key) {
        return this.tags != null && this.tags.containsKey(key);
    }
    
    /**
     * Checks if the event has specific metadata.
     * 
     * @param key Metadata key
     * @return true if metadata exists
     */
    public boolean hasMetadata(String key) {
        return this.metadata != null && this.metadata.containsKey(key);
    }
    
    /**
     * Validates the event structure.
     * 
     * @throws IllegalStateException if event is invalid
     */
    public void validate() {
        if (this.eventType == null || this.eventType.trim().isEmpty()) {
            throw new IllegalStateException("Event type cannot be null or empty");
        }
        if (this.sourceService == null || this.sourceService.trim().isEmpty()) {
            throw new IllegalStateException("Source service cannot be null or empty");
        }
        if (this.aggregateId == null || this.aggregateId.trim().isEmpty()) {
            throw new IllegalStateException("Aggregate ID cannot be null or empty");
        }
        if (this.aggregateType == null || this.aggregateType.trim().isEmpty()) {
            throw new IllegalStateException("Aggregate type cannot be null or empty");
        }
        if (this.eventId == null) {
            throw new IllegalStateException("Event ID cannot be null");
        }
        if (this.timestamp == null) {
            throw new IllegalStateException("Timestamp cannot be null");
        }
    }
    
    /**
     * Creates a copy of this event with a new event ID and timestamp.
     * 
     * @return New event instance
     */
    public abstract DomainEvent createCopy();
    
    @Override
    public String toString() {
        return String.format("%s{eventId=%s, eventType='%s', aggregateId='%s', timestamp=%s}", 
            this.getClass().getSimpleName(), eventId, eventType, aggregateId, timestamp);
    }
}