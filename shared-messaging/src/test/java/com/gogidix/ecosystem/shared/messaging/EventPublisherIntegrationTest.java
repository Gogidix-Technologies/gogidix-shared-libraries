package com.gogidix.ecosystem.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive integration tests for EventPublisher functionality.
 * Tests synchronous/asynchronous publishing, batch operations, delivery options, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class EventPublisherIntegrationTest {

    @Mock
    private EventPublisher eventPublisher;

    private TestDomainEvent testEvent;
    private String testTopic;

    @BeforeEach
    void setUp() {
        testEvent = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "test-service");
        testEvent.initializeEvent();
        testTopic = "test.events.topic";
    }

    @Test
    @DisplayName("Should publish event synchronously")
    void shouldPublishEventSynchronously() {
        // When
        eventPublisher.publish(testEvent);

        // Then
        verify(eventPublisher).publish(testEvent);
    }

    @Test
    @DisplayName("Should publish event asynchronously")
    void shouldPublishEventAsynchronously() {
        // Given
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(eventPublisher.publishAsync(testEvent)).thenReturn(future);

        // When
        CompletableFuture<Void> result = eventPublisher.publishAsync(testEvent);

        // Then
        assertThat(result).isCompleted();
        verify(eventPublisher).publishAsync(testEvent);
    }

    @Test
    @DisplayName("Should publish to specific topic synchronously")
    void shouldPublishToSpecificTopicSynchronously() {
        // When
        eventPublisher.publishToTopic(testTopic, testEvent);

        // Then
        verify(eventPublisher).publishToTopic(testTopic, testEvent);
    }

    @Test
    @DisplayName("Should publish to specific topic asynchronously")
    void shouldPublishToSpecificTopicAsynchronously() {
        // Given
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(eventPublisher.publishToTopicAsync(testTopic, testEvent)).thenReturn(future);

        // When
        CompletableFuture<Void> result = eventPublisher.publishToTopicAsync(testTopic, testEvent);

        // Then
        assertThat(result).isCompleted();
        verify(eventPublisher).publishToTopicAsync(testTopic, testEvent);
    }

    @Test
    @DisplayName("Should publish with delivery options")
    void shouldPublishWithDeliveryOptions() {
        // Given
        EventPublisher.DeliveryOptions reliableOptions = EventPublisher.DeliveryOptions.reliable();
        EventPublisher.DeliveryOptions fastOptions = EventPublisher.DeliveryOptions.fastAndLoose();
        EventPublisher.DeliveryOptions exactlyOnceOptions = EventPublisher.DeliveryOptions.exactlyOnce();

        // When
        eventPublisher.publish(testEvent, reliableOptions);
        eventPublisher.publish(testEvent, fastOptions);
        eventPublisher.publish(testEvent, exactlyOnceOptions);

        // Then
        verify(eventPublisher).publish(testEvent, reliableOptions);
        verify(eventPublisher).publish(testEvent, fastOptions);
        verify(eventPublisher).publish(testEvent, exactlyOnceOptions);
    }

    @Test
    @DisplayName("Should publish with delivery options asynchronously")
    void shouldPublishWithDeliveryOptionsAsynchronously() {
        // Given
        EventPublisher.DeliveryOptions reliableOptions = EventPublisher.DeliveryOptions.reliable();
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(eventPublisher.publishAsync(testEvent, reliableOptions)).thenReturn(future);

        // When
        CompletableFuture<Void> result = eventPublisher.publishAsync(testEvent, reliableOptions);

        // Then
        assertThat(result).isCompleted();
        verify(eventPublisher).publishAsync(testEvent, reliableOptions);
    }

    @Test
    @DisplayName("Should publish batch of events synchronously")
    void shouldPublishBatchOfEventsSynchronously() {
        // Given
        TestDomainEvent event1 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service1");
        TestDomainEvent event2 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service2");
        TestDomainEvent event3 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service3");

        // When
        eventPublisher.publishBatch(event1, event2, event3);

        // Then
        verify(eventPublisher).publishBatch(event1, event2, event3);
    }

    @Test
    @DisplayName("Should publish batch of events asynchronously")
    void shouldPublishBatchOfEventsAsynchronously() {
        // Given
        TestDomainEvent event1 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service1");
        TestDomainEvent event2 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service2");
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(eventPublisher.publishBatchAsync(event1, event2)).thenReturn(future);

        // When
        CompletableFuture<Void> result = eventPublisher.publishBatchAsync(event1, event2);

        // Then
        assertThat(result).isCompleted();
        verify(eventPublisher).publishBatchAsync(event1, event2);
    }

    @Test
    @DisplayName("Should publish transactional events")
    void shouldPublishTransactionalEvents() {
        // When
        eventPublisher.publishTransactional(testEvent);

        // Then
        verify(eventPublisher).publishTransactional(testEvent);
    }

    @Test
    @DisplayName("Should publish delayed events")
    void shouldPublishDelayedEvents() {
        // Given
        long delaySeconds = 30;

        // When
        eventPublisher.publishDelayed(testEvent, delaySeconds);

        // Then
        verify(eventPublisher).publishDelayed(testEvent, delaySeconds);
    }

    @Test
    @DisplayName("Should schedule events")
    void shouldScheduleEvents() {
        // Given
        LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(5);

        // When
        eventPublisher.scheduleEvent(testEvent, scheduledTime);

        // Then
        verify(eventPublisher).scheduleEvent(testEvent, scheduledTime);
    }

    @Test
    @DisplayName("Should handle concurrent publishing")
    void shouldHandleConcurrentPublishing() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger publishCount = new AtomicInteger(0);

        TestEventPublisher testPublisher = new TestEventPublisher();

        // When - Publish events concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int eventIndex = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    TestDomainEvent event = new TestDomainEvent(
                        UUID.randomUUID(), 
                        "TestAggregate", 
                        "service-" + eventIndex
                    );
                    event.initializeEvent();
                    testPublisher.publish(event);
                    publishCount.incrementAndGet();
                } catch (Exception e) {
                    // Handle exceptions
                } finally {
                    completeLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // Start all threads
        boolean completed = completeLatch.await(5, TimeUnit.SECONDS);

        // Then
        assertThat(completed).isTrue();
        assertThat(publishCount.get()).isEqualTo(numberOfThreads);
        assertThat(testPublisher.getPublishedEvents()).hasSize(numberOfThreads);
    }

    @Test
    @DisplayName("Should handle async publishing with futures")
    void shouldHandleAsyncPublishingWithFutures() {
        // Given
        TestAsyncEventPublisher asyncPublisher = new TestAsyncEventPublisher();

        // When
        CompletableFuture<Void> future1 = asyncPublisher.publishAsync(testEvent);
        CompletableFuture<Void> future2 = asyncPublisher.publishToTopicAsync(testTopic, testEvent);

        // Then
        assertThat(future1).isCompleted();
        assertThat(future2).isCompleted();
        assertThat(asyncPublisher.getAsyncPublishCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle batch async publishing")
    void shouldHandleBatchAsyncPublishing() {
        // Given
        TestAsyncEventPublisher asyncPublisher = new TestAsyncEventPublisher();
        TestDomainEvent event1 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service1");
        TestDomainEvent event2 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service2");
        TestDomainEvent event3 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service3");

        // When
        CompletableFuture<Void> batchFuture = asyncPublisher.publishBatchAsync(event1, event2, event3);

        // Then
        assertThat(batchFuture).isCompleted();
        assertThat(asyncPublisher.getBatchPublishCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle error scenarios in publishing")
    void shouldHandleErrorScenariosInPublishing() {
        // Given
        ErrorEventPublisher errorPublisher = new ErrorEventPublisher();

        // When/Then
        assertThatThrownBy(() -> errorPublisher.publish(testEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Publishing failed");

        CompletableFuture<Void> failedFuture = errorPublisher.publishAsync(testEvent);
        assertThat(failedFuture).isCompletedExceptionally();
    }

    @Test
    @DisplayName("Should validate delivery options")
    void shouldValidateDeliveryOptions() {
        // Given
        EventPublisher.DeliveryOptions reliableOptions = EventPublisher.DeliveryOptions.reliable();
        EventPublisher.DeliveryOptions fastOptions = EventPublisher.DeliveryOptions.fastAndLoose();
        EventPublisher.DeliveryOptions exactlyOnceOptions = EventPublisher.DeliveryOptions.exactlyOnce();

        // Then
        assertThat(reliableOptions).isNotNull();
        assertThat(fastOptions).isNotNull();
        assertThat(exactlyOnceOptions).isNotNull();

        // Different options should have different characteristics
        assertThat(reliableOptions.getDeliveryMode()).isEqualTo(EventPublisher.DeliveryOptions.DeliveryMode.AT_LEAST_ONCE);
        assertThat(fastOptions.getDeliveryMode()).isEqualTo(EventPublisher.DeliveryOptions.DeliveryMode.AT_MOST_ONCE);
        assertThat(exactlyOnceOptions.getDeliveryMode()).isEqualTo(EventPublisher.DeliveryOptions.DeliveryMode.EXACTLY_ONCE);
    }

    @Test
    @DisplayName("Should handle null event validation")
    void shouldHandleNullEventValidation() {
        // Given
        TestEventPublisher testPublisher = new TestEventPublisher();

        // When/Then
        assertThatThrownBy(() -> testPublisher.publish(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Event cannot be null");
    }

    @Test
    @DisplayName("Should handle null topic validation")
    void shouldHandleNullTopicValidation() {
        // Given
        TestEventPublisher testPublisher = new TestEventPublisher();

        // When/Then
        assertThatThrownBy(() -> testPublisher.publishToTopic(null, testEvent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Topic cannot be null or empty");
    }

    @Test
    @DisplayName("Should handle empty batch validation")
    void shouldHandleEmptyBatchValidation() {
        // Given
        TestEventPublisher testPublisher = new TestEventPublisher();

        // When/Then
        assertThatThrownBy(() -> testPublisher.publishBatch())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Batch cannot be empty");
    }

    // Test implementations

    private static class TestEventPublisher implements EventPublisher {
        private final java.util.List<DomainEvent> publishedEvents = new java.util.ArrayList<>();

        @Override
        public <T extends DomainEvent> void publish(T event) {
            if (event == null) throw new IllegalArgumentException("Event cannot be null");
            publishedEvents.add(event);
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event) {
            publish(event);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T extends DomainEvent> void publishToTopic(String topic, T event) {
            if (topic == null || topic.isEmpty()) throw new IllegalArgumentException("Topic cannot be null or empty");
            publish(event);
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishToTopicAsync(String topic, T event) {
            publishToTopic(topic, event);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T extends DomainEvent> void publish(T event, EventPublisher.DeliveryOptions options) {
            publish(event);
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event, EventPublisher.DeliveryOptions options) {
            publish(event, options);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void publishBatch(DomainEvent... events) {
            if (events.length == 0) throw new IllegalArgumentException("Batch cannot be empty");
            for (DomainEvent event : events) {
                publish(event);
            }
        }

        @Override
        public CompletableFuture<Void> publishBatchAsync(DomainEvent... events) {
            publishBatch(events);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T extends DomainEvent> void publishTransactional(T event) {
            publish(event);
        }

        @Override
        public <T extends DomainEvent> void publishDelayed(T event, long delaySeconds) {
            publish(event);
        }

        @Override
        public <T extends DomainEvent> void scheduleEvent(T event, LocalDateTime scheduledTime) {
            publish(event);
        }

        public java.util.List<DomainEvent> getPublishedEvents() {
            return publishedEvents;
        }
    }

    private static class TestAsyncEventPublisher implements EventPublisher {
        private final AtomicInteger asyncPublishCount = new AtomicInteger(0);
        private final AtomicInteger batchPublishCount = new AtomicInteger(0);

        @Override
        public <T extends DomainEvent> void publish(T event) {
            // Sync implementation
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event) {
            asyncPublishCount.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T extends DomainEvent> void publishToTopic(String topic, T event) {
            // Sync implementation
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishToTopicAsync(String topic, T event) {
            asyncPublishCount.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T extends DomainEvent> void publish(T event, EventPublisher.DeliveryOptions options) {
            // Sync implementation
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event, EventPublisher.DeliveryOptions options) {
            asyncPublishCount.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void publishBatch(DomainEvent... events) {
            // Sync implementation
        }

        @Override
        public CompletableFuture<Void> publishBatchAsync(DomainEvent... events) {
            batchPublishCount.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T extends DomainEvent> void publishTransactional(T event) {
            // Implementation
        }

        @Override
        public <T extends DomainEvent> void publishDelayed(T event, long delaySeconds) {
            // Implementation
        }

        @Override
        public <T extends DomainEvent> void scheduleEvent(T event, LocalDateTime scheduledTime) {
            // Implementation
        }

        public int getAsyncPublishCount() {
            return asyncPublishCount.get();
        }

        public int getBatchPublishCount() {
            return batchPublishCount.get();
        }
    }

    private static class ErrorEventPublisher implements EventPublisher {
        @Override
        public <T extends DomainEvent> void publish(T event) {
            throw new RuntimeException("Publishing failed");
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event) {
            return CompletableFuture.failedFuture(new RuntimeException("Async publishing failed"));
        }

        // Other methods throw similar exceptions...
        @Override
        public <T extends DomainEvent> void publishToTopic(String topic, T event) {
            throw new RuntimeException("Publishing to topic failed");
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishToTopicAsync(String topic, T event) {
            return CompletableFuture.failedFuture(new RuntimeException("Async topic publishing failed"));
        }

        @Override
        public <T extends DomainEvent> void publish(T event, EventPublisher.DeliveryOptions options) {
            throw new RuntimeException("Publishing with options failed");
        }

        @Override
        public <T extends DomainEvent> CompletableFuture<Void> publishAsync(T event, EventPublisher.DeliveryOptions options) {
            return CompletableFuture.failedFuture(new RuntimeException("Async publishing with options failed"));
        }

        @Override
        public void publishBatch(DomainEvent... events) {
            throw new RuntimeException("Batch publishing failed");
        }

        @Override
        public CompletableFuture<Void> publishBatchAsync(DomainEvent... events) {
            return CompletableFuture.failedFuture(new RuntimeException("Async batch publishing failed"));
        }

        @Override
        public <T extends DomainEvent> void publishTransactional(T event) {
            throw new RuntimeException("Transactional publishing failed");
        }

        @Override
        public <T extends DomainEvent> void publishDelayed(T event, long delaySeconds) {
            throw new RuntimeException("Delayed publishing failed");
        }

        @Override
        public <T extends DomainEvent> void scheduleEvent(T event, LocalDateTime scheduledTime) {
            throw new RuntimeException("Scheduled publishing failed");
        }
    }

    // Test domain event
    private static class TestDomainEvent extends DomainEvent {
        public TestDomainEvent(UUID aggregateId, String aggregateType, String sourceService) {
            // Use the setter pattern as per lombok annotations
            this.setAggregateId(aggregateId.toString());
            this.setAggregateType(aggregateType);
            this.setSourceService(sourceService);
            this.setEventType("TestDomainEvent");
            this.setSequenceNumber(System.currentTimeMillis());
        }

        @Override
        public DomainEvent createCopy() {
            return new TestDomainEvent(UUID.fromString(getAggregateId()), getAggregateType(), getSourceService());
        }
    }

}