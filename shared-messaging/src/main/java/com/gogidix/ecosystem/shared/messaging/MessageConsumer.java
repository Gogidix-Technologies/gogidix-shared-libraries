package com.gogidix.ecosystem.shared.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interface for consuming messages in the Exalt Social E-commerce Ecosystem.
 * Provides a unified API for receiving messages from different messaging systems.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public interface MessageConsumer {
    
    /**
     * Starts consuming messages from a destination.
     * 
     * @param destination Source destination (topic, queue, exchange)
     * @param messageHandler Handler for received messages
     * @param messageType Expected message type
     * @param <T> Message type
     * @return Subscription handle for managing the consumer
     */
    <T> Subscription subscribe(String destination, Consumer<T> messageHandler, Class<T> messageType);
    
    /**
     * Starts consuming messages with full options.
     * 
     * @param options Consumption options
     * @param messageHandler Handler for received messages
     * @param messageType Expected message type
     * @param <T> Message type
     * @return Subscription handle for managing the consumer
     */
    <T> Subscription subscribe(ConsumerOptions options, Consumer<T> messageHandler, Class<T> messageType);
    
    /**
     * Starts consuming messages with full context.
     * 
     * @param destination Source destination
     * @param messageHandler Handler with message context
     * @param messageType Expected message type
     * @param <T> Message type
     * @return Subscription handle for managing the consumer
     */
    <T> Subscription subscribeWithContext(String destination, MessageHandler<T> messageHandler, Class<T> messageType);
    
    /**
     * Polls for messages synchronously (for pull-based messaging).
     * 
     * @param destination Source destination
     * @param messageType Expected message type
     * @param timeoutMs Timeout in milliseconds
     * @param <T> Message type
     * @return Received message or null if timeout
     */
    <T> ReceivedMessage<T> poll(String destination, Class<T> messageType, long timeoutMs);
    
    /**
     * Polls for multiple messages synchronously.
     * 
     * @param destination Source destination
     * @param messageType Expected message type
     * @param maxMessages Maximum number of messages to receive
     * @param timeoutMs Timeout in milliseconds
     * @param <T> Message type
     * @return List of received messages
     */
    <T> java.util.List<ReceivedMessage<T>> pollBatch(String destination, Class<T> messageType, int maxMessages, long timeoutMs);
    
    /**
     * Receives a single message and processes it (for request-reply scenarios).
     * 
     * @param destination Source destination
     * @param processor Message processor
     * @param requestType Expected request type
     * @param responseType Response type
     * @param <Req> Request type
     * @param <Res> Response type
     * @return Future for the processing operation
     */
    <Req, Res> CompletableFuture<Void> receiveAndReply(String destination, 
                                                        RequestProcessor<Req, Res> processor,
                                                        Class<Req> requestType, 
                                                        Class<Res> responseType);
    
    /**
     * Creates a temporary queue for receiving responses.
     * 
     * @return Temporary queue name
     */
    String createTemporaryQueue();
    
    /**
     * Consumer options for message consumption.
     */
    class ConsumerOptions {
        private String destination;
        private String consumerGroup;
        private String clientId;
        private boolean autoAcknowledge = false;
        private int prefetchCount = 10;
        private long sessionTimeoutMs = 30000;
        private long heartbeatIntervalMs = 3000;
        private boolean autoReconnect = true;
        private int maxRetries = 3;
        private long retryDelayMs = 1000;
        private String deadLetterQueue;
        private OffsetResetStrategy offsetResetStrategy = OffsetResetStrategy.LATEST;
        private boolean concurrent = true;
        private int maxConcurrency = 10;
        private String filterExpression;
        private Map<String, Object> consumerProperties;
        
        public enum OffsetResetStrategy {
            EARLIEST, LATEST, NONE
        }
        
        // Builder pattern
        public static ConsumerOptions forDestination(String destination) {
            return new ConsumerOptions().setDestination(destination);
        }
        
        // Getters and setters with fluent interface
        public String getDestination() { return destination; }
        public ConsumerOptions setDestination(String destination) { 
            this.destination = destination; return this; 
        }
        
        public String getConsumerGroup() { return consumerGroup; }
        public ConsumerOptions setConsumerGroup(String consumerGroup) { 
            this.consumerGroup = consumerGroup; return this; 
        }
        
        public String getClientId() { return clientId; }
        public ConsumerOptions setClientId(String clientId) { 
            this.clientId = clientId; return this; 
        }
        
        public boolean isAutoAcknowledge() { return autoAcknowledge; }
        public ConsumerOptions setAutoAcknowledge(boolean autoAcknowledge) { 
            this.autoAcknowledge = autoAcknowledge; return this; 
        }
        
        public int getPrefetchCount() { return prefetchCount; }
        public ConsumerOptions setPrefetchCount(int prefetchCount) { 
            this.prefetchCount = prefetchCount; return this; 
        }
        
        public long getSessionTimeoutMs() { return sessionTimeoutMs; }
        public ConsumerOptions setSessionTimeoutMs(long sessionTimeoutMs) { 
            this.sessionTimeoutMs = sessionTimeoutMs; return this; 
        }
        
        public long getHeartbeatIntervalMs() { return heartbeatIntervalMs; }
        public ConsumerOptions setHeartbeatIntervalMs(long heartbeatIntervalMs) { 
            this.heartbeatIntervalMs = heartbeatIntervalMs; return this; 
        }
        
        public boolean isAutoReconnect() { return autoReconnect; }
        public ConsumerOptions setAutoReconnect(boolean autoReconnect) { 
            this.autoReconnect = autoReconnect; return this; 
        }
        
        public int getMaxRetries() { return maxRetries; }
        public ConsumerOptions setMaxRetries(int maxRetries) { 
            this.maxRetries = maxRetries; return this; 
        }
        
        public long getRetryDelayMs() { return retryDelayMs; }
        public ConsumerOptions setRetryDelayMs(long retryDelayMs) { 
            this.retryDelayMs = retryDelayMs; return this; 
        }
        
        public String getDeadLetterQueue() { return deadLetterQueue; }
        public ConsumerOptions setDeadLetterQueue(String deadLetterQueue) { 
            this.deadLetterQueue = deadLetterQueue; return this; 
        }
        
        public OffsetResetStrategy getOffsetResetStrategy() { return offsetResetStrategy; }
        public ConsumerOptions setOffsetResetStrategy(OffsetResetStrategy offsetResetStrategy) { 
            this.offsetResetStrategy = offsetResetStrategy; return this; 
        }
        
        public boolean isConcurrent() { return concurrent; }
        public ConsumerOptions setConcurrent(boolean concurrent) { 
            this.concurrent = concurrent; return this; 
        }
        
        public int getMaxConcurrency() { return maxConcurrency; }
        public ConsumerOptions setMaxConcurrency(int maxConcurrency) { 
            this.maxConcurrency = maxConcurrency; return this; 
        }
        
        public String getFilterExpression() { return filterExpression; }
        public ConsumerOptions setFilterExpression(String filterExpression) { 
            this.filterExpression = filterExpression; return this; 
        }
        
        public Map<String, Object> getConsumerProperties() { return consumerProperties; }
        public ConsumerOptions setConsumerProperties(Map<String, Object> consumerProperties) { 
            this.consumerProperties = consumerProperties; return this; 
        }
        
        // Convenience methods
        public ConsumerOptions withGroup(String consumerGroup) { return setConsumerGroup(consumerGroup); }
        public ConsumerOptions withClient(String clientId) { return setClientId(clientId); }
        public ConsumerOptions withAutoAck() { return setAutoAcknowledge(true); }
        public ConsumerOptions withManualAck() { return setAutoAcknowledge(false); }
        public ConsumerOptions withPrefetch(int prefetchCount) { return setPrefetchCount(prefetchCount); }
        public ConsumerOptions withDeadLetter(String deadLetterQueue) { return setDeadLetterQueue(deadLetterQueue); }
        public ConsumerOptions fromEarliest() { return setOffsetResetStrategy(OffsetResetStrategy.EARLIEST); }
        public ConsumerOptions fromLatest() { return setOffsetResetStrategy(OffsetResetStrategy.LATEST); }
        public ConsumerOptions sequential() { return setConcurrent(false); }
        public ConsumerOptions concurrent(int maxConcurrency) { return setConcurrent(true).setMaxConcurrency(maxConcurrency); }
        public ConsumerOptions withFilter(String filterExpression) { return setFilterExpression(filterExpression); }
    }
    
    /**
     * Message handler with full context.
     */
    @FunctionalInterface
    interface MessageHandler<T> {
        void handle(T message, MessageContext context);
    }
    
    /**
     * Request processor for request-reply pattern.
     */
    @FunctionalInterface
    interface RequestProcessor<Req, Res> {
        Res process(Req request, MessageContext context);
    }
    
    /**
     * Context for message processing.
     */
    interface MessageContext {
        String getMessageId();
        String getDestination();
        Map<String, Object> getHeaders();
        Object getHeader(String key);
        Integer getPartition();
        Long getOffset();
        LocalDateTime getReceivedAt();
        String getConsumerGroup();
        int getRetryAttempt();
        
        void acknowledge();
        void reject();
        void reject(boolean requeue);
        void reply(Object response);
        void replyAndCorrelate(Object response, String correlationId);
        
        void addProcessingMetadata(String key, Object value);
        Object getProcessingMetadata(String key);
        
        AutoCloseable createSpan(String operationName);
        void log(String level, String message, Object... args);
    }
    
    /**
     * Received message wrapper.
     */
    interface ReceivedMessage<T> {
        T getPayload();
        MessageContext getContext();
        String getMessageId();
        Map<String, Object> getHeaders();
        LocalDateTime getReceivedAt();
        
        void acknowledge();
        void reject();
        void reject(boolean requeue);
    }
    
    /**
     * Subscription handle for managing message consumption.
     */
    interface Subscription {
        String getId();
        String getDestination();
        boolean isActive();
        LocalDateTime getStartedAt();
        long getMessagesProcessed();
        long getMessagesErrored();
        
        void pause();
        void resume();
        void stop();
        
        CompletableFuture<Void> stopAsync();
        
        void addStatisticsListener(StatisticsListener listener);
        void removeStatisticsListener(StatisticsListener listener);
    }
    
    /**
     * Statistics listener for monitoring consumption.
     */
    interface StatisticsListener {
        void onMessageProcessed(String subscriptionId, long totalProcessed);
        void onMessageErrored(String subscriptionId, Throwable error);
        void onSubscriptionPaused(String subscriptionId);
        void onSubscriptionResumed(String subscriptionId);
        void onSubscriptionStopped(String subscriptionId);
    }
    
    /**
     * Annotation for marking message listener methods.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Listener {
        
        /**
         * Destination to listen to.
         */
        String destination();
        
        /**
         * Consumer group (for load balancing).
         */
        String consumerGroup() default "";
        
        /**
         * Whether to auto-acknowledge messages.
         */
        boolean autoAcknowledge() default false;
        
        /**
         * Prefetch count for performance optimization.
         */
        int prefetchCount() default 10;
        
        /**
         * Maximum retry attempts for failed processing.
         */
        int maxRetries() default 3;
        
        /**
         * Retry delay in milliseconds.
         */
        long retryDelay() default 1000;
        
        /**
         * Dead letter queue for failed messages.
         */
        String deadLetterQueue() default "";
        
        /**
         * Whether to process messages concurrently.
         */
        boolean concurrent() default true;
        
        /**
         * Maximum concurrent consumers.
         */
        int maxConcurrency() default 10;
        
        /**
         * Filter expression for selective message processing.
         */
        String filter() default "";
        
        /**
         * Offset reset strategy for Kafka.
         */
        String offsetResetStrategy() default "latest";
    }
}