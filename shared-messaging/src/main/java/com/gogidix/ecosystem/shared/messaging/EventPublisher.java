package com.gogidix.ecosystem.shared.messaging;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for publishing domain events in the Exalt Social E-commerce Ecosystem.
 * Supports both synchronous and asynchronous event publishing with various delivery guarantees.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public interface EventPublisher {
    
    /**
     * Publishes an event synchronously.
     * 
     * @param event Event to publish
     * @param <T> Event type
     * @throws PublishingException if publishing fails
     */
    <T extends DomainEvent> void publish(T event);
    
    /**
     * Publishes an event asynchronously.
     * 
     * @param event Event to publish
     * @param <T> Event type
     * @return Future representing the publishing operation
     */
    <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event);
    
    /**
     * Publishes an event to a specific topic/exchange.
     * 
     * @param topic Topic or exchange name
     * @param event Event to publish
     * @param <T> Event type
     */
    <T extends DomainEvent> void publishToTopic(String topic, T event);
    
    /**
     * Publishes an event asynchronously to a specific topic/exchange.
     * 
     * @param topic Topic or exchange name
     * @param event Event to publish
     * @param <T> Event type
     * @return Future representing the publishing operation
     */
    <T extends DomainEvent> CompletableFuture<Void> publishToTopicAsync(String topic, T event);
    
    /**
     * Publishes an event with specific delivery options.
     * 
     * @param event Event to publish
     * @param options Delivery options
     * @param <T> Event type
     */
    <T extends DomainEvent> void publish(T event, DeliveryOptions options);
    
    /**
     * Publishes an event asynchronously with specific delivery options.
     * 
     * @param event Event to publish
     * @param options Delivery options
     * @param <T> Event type
     * @return Future representing the publishing operation
     */
    <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event, DeliveryOptions options);
    
    /**
     * Publishes multiple events in a batch.
     * 
     * @param events Events to publish
     */
    void publishBatch(DomainEvent... events);
    
    /**
     * Publishes multiple events asynchronously in a batch.
     * 
     * @param events Events to publish
     * @return Future representing the publishing operation
     */
    CompletableFuture<Void> publishBatchAsync(DomainEvent... events);
    
    /**
     * Publishes an event with transactional support.
     * The event will only be published if the current transaction commits successfully.
     * 
     * @param event Event to publish
     * @param <T> Event type
     */
    <T extends DomainEvent> void publishTransactional(T event);
    
    /**
     * Publishes an event with delay.
     * 
     * @param event Event to publish
     * @param delaySeconds Delay in seconds
     * @param <T> Event type
     */
    <T extends DomainEvent> void publishDelayed(T event, long delaySeconds);
    
    /**
     * Schedules an event to be published at a specific time.
     * 
     * @param event Event to publish
     * @param scheduledTime Scheduled publishing time
     * @param <T> Event type
     */
    <T extends DomainEvent> void scheduleEvent(T event, java.time.LocalDateTime scheduledTime);
    
    /**
     * Delivery options for event publishing.
     */
    class DeliveryOptions {
        private boolean persistent = true;
        private boolean requiresAcknowledgment = true;
        private int retryAttempts = 3;
        private long retryDelayMs = 1000;
        private String routingKey;
        private java.util.Map<String, Object> headers;
        private DeliveryMode deliveryMode = DeliveryMode.AT_LEAST_ONCE;
        
        public enum DeliveryMode {
            AT_MOST_ONCE,   // Fire and forget
            AT_LEAST_ONCE,  // Guarantees delivery but may duplicate
            EXACTLY_ONCE    // Guarantees single delivery (if supported)
        }
        
        // Getters and setters
        public boolean isPersistent() { return persistent; }
        public DeliveryOptions setPersistent(boolean persistent) { 
            this.persistent = persistent; return this; 
        }
        
        public boolean isRequiresAcknowledgment() { return requiresAcknowledgment; }
        public DeliveryOptions setRequiresAcknowledgment(boolean requiresAcknowledgment) { 
            this.requiresAcknowledgment = requiresAcknowledgment; return this; 
        }
        
        public int getRetryAttempts() { return retryAttempts; }
        public DeliveryOptions setRetryAttempts(int retryAttempts) { 
            this.retryAttempts = retryAttempts; return this; 
        }
        
        public long getRetryDelayMs() { return retryDelayMs; }
        public DeliveryOptions setRetryDelayMs(long retryDelayMs) { 
            this.retryDelayMs = retryDelayMs; return this; 
        }
        
        public String getRoutingKey() { return routingKey; }
        public DeliveryOptions setRoutingKey(String routingKey) { 
            this.routingKey = routingKey; return this; 
        }
        
        public java.util.Map<String, Object> getHeaders() { return headers; }
        public DeliveryOptions setHeaders(java.util.Map<String, Object> headers) { 
            this.headers = headers; return this; 
        }
        
        public DeliveryMode getDeliveryMode() { return deliveryMode; }
        public DeliveryOptions setDeliveryMode(DeliveryMode deliveryMode) { 
            this.deliveryMode = deliveryMode; return this; 
        }
        
        /**
         * Creates default delivery options for reliable messaging.
         */
        public static DeliveryOptions reliable() {
            return new DeliveryOptions()
                .setPersistent(true)
                .setRequiresAcknowledgment(true)
                .setRetryAttempts(3)
                .setDeliveryMode(DeliveryMode.AT_LEAST_ONCE);
        }
        
        /**
         * Creates delivery options for fast, best-effort messaging.
         */
        public static DeliveryOptions fastAndLoose() {
            return new DeliveryOptions()
                .setPersistent(false)
                .setRequiresAcknowledgment(false)
                .setRetryAttempts(0)
                .setDeliveryMode(DeliveryMode.AT_MOST_ONCE);
        }
        
        /**
         * Creates delivery options for exactly-once delivery (where supported).
         */
        public static DeliveryOptions exactlyOnce() {
            return new DeliveryOptions()
                .setPersistent(true)
                .setRequiresAcknowledgment(true)
                .setRetryAttempts(5)
                .setDeliveryMode(DeliveryMode.EXACTLY_ONCE);
        }
    }
    
    /**
     * Exception thrown when event publishing fails.
     */
    class PublishingException extends RuntimeException {
        private final String eventType;
        private final String eventId;
        
        public PublishingException(String message, String eventType, String eventId) {
            super(message);
            this.eventType = eventType;
            this.eventId = eventId;
        }
        
        public PublishingException(String message, String eventType, String eventId, Throwable cause) {
            super(message, cause);
            this.eventType = eventType;
            this.eventId = eventId;
        }
        
        public String getEventType() { return eventType; }
        public String getEventId() { return eventId; }
    }
}