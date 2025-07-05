package com.gogidix.ecosystem.shared.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface for handling domain events in the Exalt Social E-commerce Ecosystem.
 * Implementations of this interface will receive and process domain events.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventHandler<T extends DomainEvent> {
    
    /**
     * Handles a domain event.
     * 
     * @param event Event to handle
     * @param context Event handling context
     * @throws EventHandlingException if handling fails
     */
    void handle(T event, EventContext context) throws EventHandlingException;
    
    /**
     * Gets the event type this handler can process.
     * Default implementation extracts from generic type.
     * 
     * @return Event class type
     */
    default Class<T> getEventType() {
        return (Class<T>) ((java.lang.reflect.ParameterizedType) getClass()
            .getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
    
    /**
     * Indicates whether this handler can process the given event type.
     * 
     * @param eventClass Event class to check
     * @return true if this handler can process the event
     */
    default boolean canHandle(Class<? extends DomainEvent> eventClass) {
        return getEventType().isAssignableFrom(eventClass);
    }
    
    /**
     * Gets the priority of this handler when multiple handlers exist for the same event.
     * Lower numbers indicate higher priority.
     * 
     * @return Handler priority (default: 100)
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Indicates whether this handler should be called even if other handlers fail.
     * 
     * @return true if this handler should always be called
     */
    default boolean isAlwaysHandle() {
        return false;
    }
    
    /**
     * Annotation for marking event handler methods.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Handler {
        
        /**
         * Event types this handler can process.
         * If empty, will be derived from method parameter.
         */
        Class<? extends DomainEvent>[] value() default {};
        
        /**
         * Topics/queues to listen to.
         * If empty, will use default topic for event type.
         */
        String[] topics() default {};
        
        /**
         * Handler priority (lower = higher priority).
         */
        int priority() default 100;
        
        /**
         * Whether to handle events even if other handlers fail.
         */
        boolean alwaysHandle() default false;
        
        /**
         * Maximum retry attempts for failed handling.
         */
        int maxRetries() default 3;
        
        /**
         * Retry delay in milliseconds.
         */
        long retryDelay() default 1000;
        
        /**
         * Whether this handler requires transactional context.
         */
        boolean transactional() default false;
        
        /**
         * Dead letter queue for failed events.
         */
        String deadLetterQueue() default "";
        
        /**
         * Consumer group for load balancing (Kafka).
         */
        String consumerGroup() default "";
        
        /**
         * Whether to process events concurrently.
         */
        boolean concurrent() default true;
        
        /**
         * Maximum concurrent consumers.
         */
        int maxConcurrency() default 10;
        
        /**
         * Filter expression for selective event processing.
         * Uses SpEL (Spring Expression Language).
         */
        String filter() default "";
    }
    
    /**
     * Event handling context containing metadata and utilities.
     */
    interface EventContext {
        
        /**
         * Gets the original message/record that contained the event.
         */
        Object getOriginalMessage();
        
        /**
         * Gets message headers.
         */
        java.util.Map<String, Object> getHeaders();
        
        /**
         * Gets a header value.
         */
        Object getHeader(String key);
        
        /**
         * Gets the topic/queue the event was received from.
         */
        String getTopic();
        
        /**
         * Gets the partition (for partitioned topics like Kafka).
         */
        Integer getPartition();
        
        /**
         * Gets the offset (for offset-based topics like Kafka).
         */
        Long getOffset();
        
        /**
         * Gets the timestamp when the message was received.
         */
        java.time.LocalDateTime getReceivedAt();
        
        /**
         * Gets the consumer group (if applicable).
         */
        String getConsumerGroup();
        
        /**
         * Gets retry attempt number (0 for first attempt).
         */
        int getRetryAttempt();
        
        /**
         * Acknowledges successful processing.
         */
        void acknowledge();
        
        /**
         * Rejects the message and sends to dead letter queue.
         */
        void reject();
        
        /**
         * Rejects the message and requests redelivery.
         */
        void reject(boolean requeue);
        
        /**
         * Publishes a new event as part of handling this event.
         */
        void publishEvent(DomainEvent event);
        
        /**
         * Adds processing metadata.
         */
        void addMetadata(String key, Object value);
        
        /**
         * Gets processing metadata.
         */
        Object getMetadata(String key);
        
        /**
         * Creates a child span for distributed tracing.
         */
        AutoCloseable createSpan(String operationName);
        
        /**
         * Logs handling progress.
         */
        void log(String level, String message, Object... args);
    }
    
    /**
     * Exception thrown when event handling fails.
     */
    class EventHandlingException extends Exception {
        private final String eventType;
        private final String eventId;
        private final boolean retryable;
        
        public EventHandlingException(String message, String eventType, String eventId) {
            this(message, eventType, eventId, true);
        }
        
        public EventHandlingException(String message, String eventType, String eventId, boolean retryable) {
            super(message);
            this.eventType = eventType;
            this.eventId = eventId;
            this.retryable = retryable;
        }
        
        public EventHandlingException(String message, String eventType, String eventId, Throwable cause) {
            this(message, eventType, eventId, true, cause);
        }
        
        public EventHandlingException(String message, String eventType, String eventId, boolean retryable, Throwable cause) {
            super(message, cause);
            this.eventType = eventType;
            this.eventId = eventId;
            this.retryable = retryable;
        }
        
        public String getEventType() { return eventType; }
        public String getEventId() { return eventId; }
        public boolean isRetryable() { return retryable; }
        
        /**
         * Creates a non-retryable exception.
         */
        public static EventHandlingException nonRetryable(String message, String eventType, String eventId) {
            return new EventHandlingException(message, eventType, eventId, false);
        }
        
        /**
         * Creates a non-retryable exception with cause.
         */
        public static EventHandlingException nonRetryable(String message, String eventType, String eventId, Throwable cause) {
            return new EventHandlingException(message, eventType, eventId, false, cause);
        }
    }
}