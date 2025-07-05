package com.gogidix.ecosystem.shared.messaging;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for producing messages in the Exalt Social E-commerce Ecosystem.
 * Provides a unified API for sending messages across different messaging systems.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public interface MessageProducer {
    
    /**
     * Sends a message synchronously.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param message Message to send
     * @throws MessagingException if sending fails
     */
    void send(String destination, Object message);
    
    /**
     * Sends a message asynchronously.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param message Message to send
     * @return Future representing the sending operation
     */
    CompletableFuture<SendResult> sendAsync(String destination, Object message);
    
    /**
     * Sends a message with headers.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param message Message to send
     * @param headers Message headers
     */
    void send(String destination, Object message, Map<String, Object> headers);
    
    /**
     * Sends a message asynchronously with headers.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param message Message to send
     * @param headers Message headers
     * @return Future representing the sending operation
     */
    CompletableFuture<SendResult> sendAsync(String destination, Object message, Map<String, Object> headers);
    
    /**
     * Sends a message with full options.
     * 
     * @param options Send options
     * @return Future representing the sending operation
     */
    CompletableFuture<SendResult> send(SendOptions options);
    
    /**
     * Sends multiple messages in a batch.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param messages Messages to send
     * @return Future representing the batch sending operation
     */
    CompletableFuture<BatchSendResult> sendBatch(String destination, Object... messages);
    
    /**
     * Sends a message to a partitioned destination.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param partitionKey Key for partition selection
     * @param message Message to send
     * @return Future representing the sending operation
     */
    CompletableFuture<SendResult> sendToPartition(String destination, String partitionKey, Object message);
    
    /**
     * Sends a request message and waits for a response (Request-Reply pattern).
     * 
     * @param destination Destination for the request
     * @param request Request message
     * @param responseType Expected response type
     * @param timeoutMs Timeout in milliseconds
     * @param <T> Response type
     * @return Response message
     * @throws MessagingException if request fails or times out
     */
    <T> T sendAndReceive(String destination, Object request, Class<T> responseType, long timeoutMs);
    
    /**
     * Sends a request message asynchronously and returns a future for the response.
     * 
     * @param destination Destination for the request
     * @param request Request message
     * @param responseType Expected response type
     * @param timeoutMs Timeout in milliseconds
     * @param <T> Response type
     * @return Future containing the response
     */
    <T> CompletableFuture<T> sendAndReceiveAsync(String destination, Object request, Class<T> responseType, long timeoutMs);
    
    /**
     * Sends a message with delay.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param message Message to send
     * @param delayMs Delay in milliseconds
     * @return Future representing the sending operation
     */
    CompletableFuture<SendResult> sendDelayed(String destination, Object message, long delayMs);
    
    /**
     * Schedules a message to be sent at a specific time.
     * 
     * @param destination Destination (topic, queue, exchange)
     * @param message Message to send
     * @param scheduledTime When to send the message
     * @return Future representing the scheduling operation
     */
    CompletableFuture<SendResult> scheduleMessage(String destination, Object message, java.time.LocalDateTime scheduledTime);
    
    /**
     * Options for message sending.
     */
    class SendOptions {
        private String destination;
        private Object message;
        private Map<String, Object> headers;
        private String partitionKey;
        private String routingKey;
        private boolean persistent = true;
        private int priority = 4; // 0-9, where 9 is highest
        private long ttlMs = 0; // 0 means no expiration
        private long delayMs = 0;
        private java.time.LocalDateTime scheduledTime;
        private String correlationId;
        private String replyTo;
        private MessageType messageType = MessageType.DEFAULT;
        
        public enum MessageType {
            DEFAULT,
            FIRE_AND_FORGET,
            REQUEST_REPLY,
            BROADCAST,
            TRANSACTIONAL
        }
        
        // Builder pattern
        public static SendOptions to(String destination) {
            return new SendOptions().setDestination(destination);
        }
        
        // Getters and setters with fluent interface
        public String getDestination() { return destination; }
        public SendOptions setDestination(String destination) { 
            this.destination = destination; return this; 
        }
        
        public Object getMessage() { return message; }
        public SendOptions setMessage(Object message) { 
            this.message = message; return this; 
        }
        
        public Map<String, Object> getHeaders() { return headers; }
        public SendOptions setHeaders(Map<String, Object> headers) { 
            this.headers = headers; return this; 
        }
        
        public SendOptions addHeader(String key, Object value) {
            if (this.headers == null) {
                this.headers = new java.util.HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }
        
        public String getPartitionKey() { return partitionKey; }
        public SendOptions setPartitionKey(String partitionKey) { 
            this.partitionKey = partitionKey; return this; 
        }
        
        public String getRoutingKey() { return routingKey; }
        public SendOptions setRoutingKey(String routingKey) { 
            this.routingKey = routingKey; return this; 
        }
        
        public boolean isPersistent() { return persistent; }
        public SendOptions setPersistent(boolean persistent) { 
            this.persistent = persistent; return this; 
        }
        
        public int getPriority() { return priority; }
        public SendOptions setPriority(int priority) { 
            this.priority = Math.max(0, Math.min(9, priority)); return this; 
        }
        
        public long getTtlMs() { return ttlMs; }
        public SendOptions setTtlMs(long ttlMs) { 
            this.ttlMs = ttlMs; return this; 
        }
        
        public long getDelayMs() { return delayMs; }
        public SendOptions setDelayMs(long delayMs) { 
            this.delayMs = delayMs; return this; 
        }
        
        public java.time.LocalDateTime getScheduledTime() { return scheduledTime; }
        public SendOptions setScheduledTime(java.time.LocalDateTime scheduledTime) { 
            this.scheduledTime = scheduledTime; return this; 
        }
        
        public String getCorrelationId() { return correlationId; }
        public SendOptions setCorrelationId(String correlationId) { 
            this.correlationId = correlationId; return this; 
        }
        
        public String getReplyTo() { return replyTo; }
        public SendOptions setReplyTo(String replyTo) { 
            this.replyTo = replyTo; return this; 
        }
        
        public MessageType getMessageType() { return messageType; }
        public SendOptions setMessageType(MessageType messageType) { 
            this.messageType = messageType; return this; 
        }
        
        // Convenience methods
        public SendOptions withBody(Object message) { return setMessage(message); }
        public SendOptions withPartition(String partitionKey) { return setPartitionKey(partitionKey); }
        public SendOptions withRouting(String routingKey) { return setRoutingKey(routingKey); }
        public SendOptions withPriority(int priority) { return setPriority(priority); }
        public SendOptions withTtl(long ttlMs) { return setTtlMs(ttlMs); }
        public SendOptions withDelay(long delayMs) { return setDelayMs(delayMs); }
        public SendOptions scheduled(java.time.LocalDateTime scheduledTime) { return setScheduledTime(scheduledTime); }
        public SendOptions fireAndForget() { return setMessageType(MessageType.FIRE_AND_FORGET); }
        public SendOptions requestReply(String replyTo) { return setMessageType(MessageType.REQUEST_REPLY).setReplyTo(replyTo); }
        public SendOptions broadcast() { return setMessageType(MessageType.BROADCAST); }
        public SendOptions transactional() { return setMessageType(MessageType.TRANSACTIONAL); }
    }
    
    /**
     * Result of a message send operation.
     */
    interface SendResult {
        String getMessageId();
        String getDestination();
        Integer getPartition();
        Long getOffset();
        java.time.LocalDateTime getSentAt();
        boolean isSuccessful();
        String getErrorMessage();
        Throwable getError();
        Map<String, Object> getMetadata();
    }
    
    /**
     * Result of a batch send operation.
     */
    interface BatchSendResult {
        int getTotalMessages();
        int getSuccessfulMessages();
        int getFailedMessages();
        java.util.List<SendResult> getResults();
        boolean isAllSuccessful();
        java.util.List<SendResult> getFailedResults();
    }
    
    /**
     * Exception thrown when messaging operations fail.
     */
    class MessagingException extends RuntimeException {
        private final String destination;
        private final String messageId;
        private final ErrorCode errorCode;
        
        public enum ErrorCode {
            CONNECTION_FAILED,
            SERIALIZATION_FAILED,
            DESTINATION_NOT_FOUND,
            TIMEOUT,
            QUOTA_EXCEEDED,
            AUTHENTICATION_FAILED,
            AUTHORIZATION_FAILED,
            MESSAGE_TOO_LARGE,
            INVALID_MESSAGE,
            BROKER_ERROR,
            UNKNOWN_ERROR
        }
        
        public MessagingException(String message, String destination, ErrorCode errorCode) {
            this(message, destination, null, errorCode, null);
        }
        
        public MessagingException(String message, String destination, String messageId, ErrorCode errorCode) {
            this(message, destination, messageId, errorCode, null);
        }
        
        public MessagingException(String message, String destination, String messageId, ErrorCode errorCode, Throwable cause) {
            super(message, cause);
            this.destination = destination;
            this.messageId = messageId;
            this.errorCode = errorCode;
        }
        
        public String getDestination() { return destination; }
        public String getMessageId() { return messageId; }
        public ErrorCode getErrorCode() { return errorCode; }
        
        public boolean isRetryable() {
            return errorCode == ErrorCode.CONNECTION_FAILED ||
                   errorCode == ErrorCode.TIMEOUT ||
                   errorCode == ErrorCode.BROKER_ERROR;
        }
    }
}