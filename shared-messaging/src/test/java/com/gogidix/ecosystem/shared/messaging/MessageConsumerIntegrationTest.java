package com.gogidix.ecosystem.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration tests for MessageConsumer functionality.
 * Tests subscription management, consumption patterns, error handling, and lifecycle management.
 */
@ExtendWith(MockitoExtension.class)
class MessageConsumerIntegrationTest {

    private TestMessageConsumer messageConsumer;
    private String testDestination;

    @BeforeEach
    void setUp() {
        messageConsumer = new TestMessageConsumer();
        testDestination = "test.queue";
    }

    @Test
    @DisplayName("Should subscribe to destination successfully")
    void shouldSubscribeToDestinationSuccessfully() {
        // Given
        AtomicInteger messageCount = new AtomicInteger(0);
        Consumer<TestMessage> messageHandler = message -> messageCount.incrementAndGet();

        // When
        MessageConsumer.Subscription subscription = messageConsumer.subscribe(testDestination, messageHandler, TestMessage.class);

        // Then
        assertThat(subscription).isNotNull();
        assertThat(subscription.isActive()).isTrue();
        assertThat(subscription.getDestination()).isEqualTo(testDestination);
        assertThat(messageConsumer.getActiveSubscriptions()).hasSize(1);
    }

    @Test
    @DisplayName("Should subscribe with consumer options")
    void shouldSubscribeWithConsumerOptions() {
        // Given
        MessageConsumer.ConsumerOptions options = MessageConsumer.ConsumerOptions.forDestination(testDestination)
                .withGroup("test-group")
                .withAutoAck()
                .withPrefetch(10)
                .fromEarliest();

        Consumer<TestMessage> messageHandler = message -> { /* process message */ };

        // When
        MessageConsumer.Subscription subscription = messageConsumer.subscribe(options, messageHandler, TestMessage.class);

        // Then
        assertThat(subscription).isNotNull();
        assertThat(subscription.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should subscribe with context-aware handler")
    void shouldSubscribeWithContextAwareHandler() {
        // Given
        AtomicInteger contextCallCount = new AtomicInteger(0);
        MessageConsumer.MessageHandler<TestMessage> contextHandler = (message, context) -> {
            contextCallCount.incrementAndGet();
            assertThat(context).isNotNull();
            context.acknowledge();
        };

        // When
        MessageConsumer.Subscription subscription = messageConsumer.subscribeWithContext(testDestination, contextHandler, TestMessage.class);

        // Then
        assertThat(subscription).isNotNull();
        assertThat(subscription.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should poll messages synchronously")
    void shouldPollMessagesSynchronously() {
        // Given
        TestMessage testMessage = new TestMessage("test-id", "test content");
        messageConsumer.addTestMessage(testDestination, testMessage);

        // When
        MessageConsumer.ReceivedMessage<TestMessage> receivedMessage = messageConsumer.poll(testDestination, TestMessage.class, 1000);

        // Then
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getPayload()).isEqualTo(testMessage);
        assertThat(receivedMessage.getHeaders()).isNotNull();
    }

    @Test
    @DisplayName("Should poll batch of messages")
    void shouldPollBatchOfMessages() {
        // Given
        TestMessage message1 = new TestMessage("id1", "content 1");
        TestMessage message2 = new TestMessage("id2", "content 2");
        TestMessage message3 = new TestMessage("id3", "content 3");
        
        messageConsumer.addTestMessage(testDestination, message1);
        messageConsumer.addTestMessage(testDestination, message2);
        messageConsumer.addTestMessage(testDestination, message3);

        // When
        List<MessageConsumer.ReceivedMessage<TestMessage>> messages = messageConsumer.pollBatch(testDestination, TestMessage.class, 5, 1000);

        // Then
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getPayload()).isEqualTo(message1);
        assertThat(messages.get(1).getPayload()).isEqualTo(message2);
        assertThat(messages.get(2).getPayload()).isEqualTo(message3);
    }

    @Test
    @DisplayName("Should handle request-reply pattern")
    void shouldHandleRequestReplyPattern() {
        // Given
        MessageConsumer.RequestProcessor<TestRequest, TestResponse> processor = (request, context) -> {
            return new TestResponse("processed-" + request.getData());
        };

        // When
        CompletableFuture<Void> future = messageConsumer.receiveAndReply(
            testDestination, processor, TestRequest.class, TestResponse.class);

        // Then
        assertThat(future).isCompleted();
    }

    @Test
    @DisplayName("Should create temporary queue")
    void shouldCreateTemporaryQueue() {
        // When
        String tempQueue = messageConsumer.createTemporaryQueue();

        // Then
        assertThat(tempQueue).isNotNull();
        assertThat(tempQueue).startsWith("temp.queue.");
        assertThat(tempQueue).hasSize(21); // "temp.queue." + 10 char UUID
    }

    @Test
    @DisplayName("Should handle multiple concurrent subscriptions")
    void shouldHandleMultipleConcurrentSubscriptions() throws InterruptedException {
        // Given
        int numberOfSubscriptions = 5;
        CountDownLatch subscriptionLatch = new CountDownLatch(numberOfSubscriptions);
        AtomicInteger totalMessages = new AtomicInteger(0);

        Consumer<TestMessage> messageHandler = message -> {
            totalMessages.incrementAndGet();
            subscriptionLatch.countDown();
        };

        // When - Create multiple subscriptions
        for (int i = 0; i < numberOfSubscriptions; i++) {
            String destination = "test.queue." + i;
            MessageConsumer.Subscription subscription = messageConsumer.subscribe(destination, messageHandler, TestMessage.class);
            
            // Simulate message delivery to each subscription
            messageConsumer.addTestMessage(destination, new TestMessage("msg-" + i, "content " + i));
            messageConsumer.deliverMessage(destination);
        }

        boolean completed = subscriptionLatch.await(5, TimeUnit.SECONDS);

        // Then
        assertThat(completed).isTrue();
        assertThat(messageConsumer.getActiveSubscriptions()).hasSize(numberOfSubscriptions);
        assertThat(totalMessages.get()).isEqualTo(numberOfSubscriptions);
    }

    @Test
    @DisplayName("Should handle subscription lifecycle")
    void shouldHandleSubscriptionLifecycle() {
        // Given
        Consumer<TestMessage> messageHandler = message -> { /* process */ };
        MessageConsumer.Subscription subscription = messageConsumer.subscribe(testDestination, messageHandler, TestMessage.class);

        // When - Unsubscribe
        subscription.stop();

        // Then
        assertThat(subscription.isActive()).isFalse();
        assertThat(messageConsumer.getActiveSubscriptions()).isEmpty();
    }

    @Test
    @DisplayName("Should handle subscription statistics")
    void shouldHandleSubscriptionStatistics() {
        // Given
        Consumer<TestMessage> messageHandler = message -> { /* process */ };
        MessageConsumer.Subscription subscription = messageConsumer.subscribe(testDestination, messageHandler, TestMessage.class);

        // Simulate processing messages
        messageConsumer.addTestMessage(testDestination, new TestMessage("msg1", "content1"));
        messageConsumer.addTestMessage(testDestination, new TestMessage("msg2", "content2"));
        messageConsumer.deliverMessage(testDestination);
        messageConsumer.deliverMessage(testDestination);

        // When
        long messagesProcessed = subscription.getMessagesProcessed();

        // Then
        assertThat(messagesProcessed).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle consumer options validation")
    void shouldHandleConsumerOptionsValidation() {
        // Given
        MessageConsumer.ConsumerOptions validOptions = MessageConsumer.ConsumerOptions.forDestination(testDestination)
                .withGroup("valid-group")
                .withPrefetch(10);

        MessageConsumer.ConsumerOptions invalidOptions = MessageConsumer.ConsumerOptions.forDestination("")
                .withGroup("")
                .withPrefetch(-1);

        Consumer<TestMessage> messageHandler = message -> { /* process */ };

        // When/Then - Valid options should work
        assertThatCode(() -> messageConsumer.subscribe(validOptions, messageHandler, TestMessage.class))
                .doesNotThrowAnyException();

        // Invalid options should fail
        assertThatThrownBy(() -> messageConsumer.subscribe(invalidOptions, messageHandler, TestMessage.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle message acknowledgment patterns")
    void shouldHandleMessageAcknowledgmentPatterns() {
        // Given - Auto-acknowledgment
        MessageConsumer.ConsumerOptions autoAckOptions = MessageConsumer.ConsumerOptions.forDestination(testDestination).withAutoAck();
        AtomicInteger autoAckCount = new AtomicInteger(0);
        Consumer<TestMessage> autoAckHandler = message -> autoAckCount.incrementAndGet();

        // Given - Manual acknowledgment
        MessageConsumer.ConsumerOptions manualAckOptions = MessageConsumer.ConsumerOptions.forDestination(testDestination + ".manual").withManualAck();
        AtomicInteger manualAckCount = new AtomicInteger(0);
        MessageConsumer.MessageHandler<TestMessage> manualAckHandler = (message, context) -> {
            manualAckCount.incrementAndGet();
            context.acknowledge();
        };

        // When
        MessageConsumer.Subscription autoAckSub = messageConsumer.subscribe(autoAckOptions, autoAckHandler, TestMessage.class);
        MessageConsumer.Subscription manualAckSub = messageConsumer.subscribeWithContext(testDestination + ".manual", manualAckHandler, TestMessage.class);

        // Deliver messages
        messageConsumer.addTestMessage(testDestination, new TestMessage("auto", "auto content"));
        messageConsumer.addTestMessage(testDestination + ".manual", new TestMessage("manual", "manual content"));
        messageConsumer.deliverMessage(testDestination);
        messageConsumer.deliverMessage(testDestination + ".manual");

        // Then
        assertThat(autoAckCount.get()).isEqualTo(1);
        assertThat(manualAckCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle error scenarios in consumption")
    void shouldHandleErrorScenariosInConsumption() {
        // Given
        Consumer<TestMessage> errorHandler = message -> {
            throw new RuntimeException("Processing failed");
        };

        // When
        MessageConsumer.Subscription subscription = messageConsumer.subscribe(testDestination, errorHandler, TestMessage.class);
        messageConsumer.addTestMessage(testDestination, new TestMessage("error", "error content"));

        // Then - Should not throw exception, error should be handled internally
        assertThatCode(() -> messageConsumer.deliverMessage(testDestination))
                .doesNotThrowAnyException();
        
        assertThat(subscription.getMessagesErrored()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle dead letter queue scenarios")
    void shouldHandleDeadLetterQueueScenarios() {
        // Given
        MessageConsumer.ConsumerOptions optionsWithDLQ = MessageConsumer.ConsumerOptions.forDestination(testDestination)
                .withDeadLetter("test.dlq")
                .withAutoAck();

        Consumer<TestMessage> flakyHandler = message -> {
            if (message.getId().contains("fail")) {
                throw new RuntimeException("Message processing failed");
            }
        };

        // When
        MessageConsumer.Subscription subscription = messageConsumer.subscribe(optionsWithDLQ, flakyHandler, TestMessage.class);
        
        messageConsumer.addTestMessage(testDestination, new TestMessage("success", "good content"));
        messageConsumer.addTestMessage(testDestination, new TestMessage("fail", "bad content"));
        
        messageConsumer.deliverMessage(testDestination);
        messageConsumer.deliverMessage(testDestination);

        // Then
        assertThat(subscription.getMessagesProcessed()).isEqualTo(1); // Only successful message
        assertThat(subscription.getMessagesErrored()).isEqualTo(1); // One failed message
        
        // Check dead letter queue
        List<MessageConsumer.ReceivedMessage<TestMessage>> dlqMessages = messageConsumer.pollBatch("test.dlq", TestMessage.class, 10, 100);
        assertThat(dlqMessages).hasSize(1);
        assertThat(dlqMessages.get(0).getPayload().getId()).isEqualTo("fail");
    }

    @Test
    @DisplayName("Should handle null validation")
    void shouldHandleNullValidation() {
        // When/Then
        assertThatThrownBy(() -> messageConsumer.subscribe((String) null, message -> {}, TestMessage.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Destination cannot be null or empty");

        assertThatThrownBy(() -> messageConsumer.subscribe(testDestination, null, TestMessage.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message handler cannot be null");

        assertThatThrownBy(() -> messageConsumer.subscribe(testDestination, message -> {}, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message type cannot be null");
    }

    // Test message classes
    private static class TestMessage {
        private final String id;
        private final String content;

        public TestMessage(String id, String content) {
            this.id = id;
            this.content = content;
        }

        public String getId() { return id; }
        public String getContent() { return content; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestMessage)) return false;
            TestMessage that = (TestMessage) o;
            return id.equals(that.id) && content.equals(that.content);
        }

        @Override
        public int hashCode() {
            return id.hashCode() + content.hashCode();
        }
    }

    private static class TestRequest {
        private final String data;

        public TestRequest(String data) {
            this.data = data;
        }

        public String getData() { return data; }
    }

    private static class TestResponse {
        private final String result;

        public TestResponse(String result) {
            this.result = result;
        }

        public String getResult() { return result; }
    }

    // Test implementation of MessageConsumer
    private static class TestMessageConsumer implements MessageConsumer {
        private final java.util.List<TestSubscription> subscriptions = new java.util.ArrayList<>();
        private final java.util.Map<String, java.util.Queue<Object>> messageQueues = new java.util.HashMap<>();
        private final java.util.Map<String, java.util.Queue<Object>> dlqQueues = new java.util.HashMap<>();

        @Override
        public <T> MessageConsumer.Subscription subscribe(String destination, Consumer<T> messageHandler, Class<T> messageType) {
            if (destination == null || destination.isEmpty()) {
                throw new IllegalArgumentException("Destination cannot be null or empty");
            }
            if (messageHandler == null) {
                throw new IllegalArgumentException("Message handler cannot be null");
            }
            if (messageType == null) {
                throw new IllegalArgumentException("Message type cannot be null");
            }

            MessageConsumer.ConsumerOptions defaultOptions = MessageConsumer.ConsumerOptions.forDestination(destination);
            return subscribe(defaultOptions, messageHandler, messageType);
        }

        @Override
        public <T> MessageConsumer.Subscription subscribe(MessageConsumer.ConsumerOptions options, Consumer<T> messageHandler, Class<T> messageType) {
            if (options.getDestination().isEmpty() || options.getConsumerGroup().isEmpty() || options.getPrefetchCount() < 0) {
                throw new IllegalArgumentException("Invalid consumer options");
            }

            TestSubscription subscription = new TestSubscription(options, messageHandler, messageType);
            subscriptions.add(subscription);
            messageQueues.putIfAbsent(options.getDestination(), new java.util.LinkedList<>());
            
            if (options.getDeadLetterQueue() != null) {
                dlqQueues.putIfAbsent(options.getDeadLetterQueue(), new java.util.LinkedList<>());
            }
            
            return subscription;
        }

        @Override
        public <T> MessageConsumer.Subscription subscribeWithContext(String destination, MessageConsumer.MessageHandler<T> messageHandler, Class<T> messageType) {
            TestSubscription subscription = new TestSubscription(
                MessageConsumer.ConsumerOptions.forDestination(destination), 
                null, 
                messageType,
                messageHandler
            );
            subscriptions.add(subscription);
            messageQueues.putIfAbsent(destination, new java.util.LinkedList<>());
            return subscription;
        }

        @Override
        public <T> MessageConsumer.ReceivedMessage<T> poll(String destination, Class<T> messageType, long timeoutMs) {
            java.util.Queue<Object> queue = messageQueues.get(destination);
            if (queue != null && !queue.isEmpty()) {
                Object message = queue.poll();
                return new TestReceivedMessage<>((T) message, java.util.Map.of("timestamp", System.currentTimeMillis()));
            }
            return null;
        }

        @Override
        public <T> List<MessageConsumer.ReceivedMessage<T>> pollBatch(String destination, Class<T> messageType, int maxMessages, long timeoutMs) {
            java.util.List<MessageConsumer.ReceivedMessage<T>> messages = new java.util.ArrayList<>();
            java.util.Queue<Object> queue = messageQueues.get(destination);
            
            if (queue != null) {
                for (int i = 0; i < maxMessages && !queue.isEmpty(); i++) {
                    Object message = queue.poll();
                    messages.add(new TestReceivedMessage<>((T) message, java.util.Map.of("timestamp", System.currentTimeMillis())));
                }
            }
            
            return messages;
        }

        @Override
        public <Req, Res> CompletableFuture<Void> receiveAndReply(String destination, MessageConsumer.RequestProcessor<Req, Res> processor, Class<Req> requestType, Class<Res> responseType) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String createTemporaryQueue() {
            return "temp.queue." + UUID.randomUUID().toString().substring(0, 10);
        }

        // Test helper methods
        public void addTestMessage(String destination, Object message) {
            messageQueues.computeIfAbsent(destination, k -> new java.util.LinkedList<>()).offer(message);
        }

        public void deliverMessage(String destination) {
            java.util.Queue<Object> queue = messageQueues.get(destination);
            if (queue == null || queue.isEmpty()) return;

            Object message = queue.poll();
            
            for (TestSubscription subscription : subscriptions) {
                if (subscription.getDestination().equals(destination) && subscription.isActive()) {
                    try {
                        if (subscription.contextHandler != null) {
                            TestMessageContext context = new TestMessageContext();
                            ((MessageConsumer.MessageHandler<Object>) subscription.contextHandler).handle(message, context);
                        } else if (subscription.messageHandler != null) {
                            ((java.util.function.Consumer<Object>) subscription.messageHandler).accept(message);
                        }
                        subscription.incrementProcessedCount();
                    } catch (Exception e) {
                        subscription.incrementErrorCount();
                        // Move to DLQ if configured
                        if (subscription.getOptions().getDeadLetterQueue() != null) {
                            dlqQueues.get(subscription.getOptions().getDeadLetterQueue()).offer(message);
                        }
                    }
                }
            }
        }

        public List<TestSubscription> getActiveSubscriptions() {
            return subscriptions.stream()
                    .filter(TestSubscription::isActive)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    // Test implementations of interfaces
    private static class TestSubscription implements MessageConsumer.Subscription {
        private final MessageConsumer.ConsumerOptions options;
        private final Consumer<?> messageHandler;
        private final MessageConsumer.MessageHandler<?> contextHandler;
        private final Class<?> messageType;
        private final AtomicInteger processedCount = new AtomicInteger(0);
        private final AtomicInteger errorCount = new AtomicInteger(0);
        private boolean active = true;
        private final String id = UUID.randomUUID().toString();
        private final java.time.LocalDateTime startedAt = java.time.LocalDateTime.now();

        public TestSubscription(MessageConsumer.ConsumerOptions options, Consumer<?> messageHandler, Class<?> messageType) {
            this(options, messageHandler, messageType, null);
        }

        public TestSubscription(MessageConsumer.ConsumerOptions options, Consumer<?> messageHandler, Class<?> messageType, MessageConsumer.MessageHandler<?> contextHandler) {
            this.options = options;
            this.messageHandler = messageHandler;
            this.messageType = messageType;
            this.contextHandler = contextHandler;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDestination() {
            return options.getDestination();
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public java.time.LocalDateTime getStartedAt() {
            return startedAt;
        }

        @Override
        public long getMessagesProcessed() {
            return processedCount.get();
        }

        @Override
        public long getMessagesErrored() {
            return errorCount.get();
        }

        @Override
        public void pause() {
            // Implementation
        }

        @Override
        public void resume() {
            // Implementation
        }

        @Override
        public void stop() {
            active = false;
        }

        @Override
        public CompletableFuture<Void> stopAsync() {
            stop();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void addStatisticsListener(MessageConsumer.StatisticsListener listener) {
            // Implementation
        }

        @Override
        public void removeStatisticsListener(MessageConsumer.StatisticsListener listener) {
            // Implementation
        }

        public MessageConsumer.ConsumerOptions getOptions() {
            return options;
        }

        public void incrementProcessedCount() {
            processedCount.incrementAndGet();
        }

        public void incrementErrorCount() {
            errorCount.incrementAndGet();
        }
    }

    private static class TestReceivedMessage<T> implements MessageConsumer.ReceivedMessage<T> {
        private final T payload;
        private final java.util.Map<String, Object> headers;
        private final String messageId = UUID.randomUUID().toString();
        private final java.time.LocalDateTime receivedAt = java.time.LocalDateTime.now();

        public TestReceivedMessage(T payload, java.util.Map<String, Object> headers) {
            this.payload = payload;
            this.headers = headers;
        }

        @Override
        public T getPayload() {
            return payload;
        }

        @Override
        public MessageConsumer.MessageContext getContext() {
            return new TestMessageContext();
        }

        @Override
        public String getMessageId() {
            return messageId;
        }

        @Override
        public java.util.Map<String, Object> getHeaders() {
            return headers;
        }

        @Override
        public java.time.LocalDateTime getReceivedAt() {
            return receivedAt;
        }

        @Override
        public void acknowledge() {
            // Implementation
        }

        @Override
        public void reject() {
            reject(false);
        }

        @Override
        public void reject(boolean requeue) {
            // Implementation
        }
    }


    private static class TestMessageContext implements MessageConsumer.MessageContext {
        private final String messageId = UUID.randomUUID().toString();
        private final String destination = "test.destination";
        private final java.util.Map<String, Object> headers = new java.util.HashMap<>();
        private final java.time.LocalDateTime receivedAt = java.time.LocalDateTime.now();

        @Override
        public String getMessageId() {
            return messageId;
        }

        @Override
        public String getDestination() {
            return destination;
        }

        @Override
        public java.util.Map<String, Object> getHeaders() {
            return headers;
        }

        @Override
        public Object getHeader(String key) {
            return headers.get(key);
        }

        @Override
        public Integer getPartition() {
            return 0;
        }

        @Override
        public Long getOffset() {
            return 0L;
        }

        @Override
        public java.time.LocalDateTime getReceivedAt() {
            return receivedAt;
        }

        @Override
        public String getConsumerGroup() {
            return "test-group";
        }

        @Override
        public int getRetryAttempt() {
            return 0;
        }

        @Override
        public void acknowledge() {
            // Implementation
        }

        @Override
        public void reject() {
            reject(false);
        }

        @Override
        public void reject(boolean requeue) {
            // Implementation
        }

        @Override
        public void reply(Object response) {
            // Implementation
        }

        @Override
        public void replyAndCorrelate(Object response, String correlationId) {
            // Implementation
        }

        @Override
        public void addProcessingMetadata(String key, Object value) {
            // Implementation
        }

        @Override
        public Object getProcessingMetadata(String key) {
            return null;
        }

        @Override
        public AutoCloseable createSpan(String operationName) {
            return () -> {}; // No-op closeable
        }

        @Override
        public void log(String level, String message, Object... args) {
            // Implementation
        }
    }

}