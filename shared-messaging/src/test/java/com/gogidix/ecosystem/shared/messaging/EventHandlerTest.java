package com.gogidix.ecosystem.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// Import the actual EventHandler classes
import static com.gogidix.ecosystem.shared.messaging.EventHandler.EventContext;
import static com.gogidix.ecosystem.shared.messaging.EventHandler.EventHandlingException;

/**
 * Comprehensive tests for EventHandler functionality.
 * Tests event handling, context operations, error scenarios, and handler lifecycle.
 */
@ExtendWith(MockitoExtension.class)
class EventHandlerTest {

    @Mock
    private EventHandler.EventContext mockContext;

    private TestEventHandler eventHandler;
    private TestDomainEvent testEvent;

    @BeforeEach
    void setUp() {
        eventHandler = new TestEventHandler();
        testEvent = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "test-service");
        testEvent.initializeEvent();
    }

    @Test
    @DisplayName("Should handle event successfully")
    void shouldHandleEventSuccessfully() throws EventHandlingException {
        // When
        eventHandler.handle(testEvent, mockContext);

        // Then
        assertThat(eventHandler.getHandledEvents()).hasSize(1);
        assertThat(eventHandler.getHandledEvents().get(0)).isEqualTo(testEvent);
        verify(mockContext).acknowledge();
    }

    @Test
    @DisplayName("Should determine event type correctly")
    void shouldDetermineEventTypeCorrectly() {
        // When
        Class<TestDomainEvent> eventType = eventHandler.getEventType();

        // Then
        assertThat(eventType).isEqualTo(TestDomainEvent.class);
    }

    @Test
    @DisplayName("Should check if can handle event class")
    void shouldCheckIfCanHandleEventClass() {
        // When/Then
        assertThat(eventHandler.canHandle(TestDomainEvent.class)).isTrue();
        assertThat(eventHandler.canHandle(AnotherTestEvent.class)).isFalse();
        assertThat(eventHandler.canHandle(DomainEvent.class)).isFalse();
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = eventHandler.getPriority();

        // Then
        assertThat(priority).isEqualTo(100);
    }

    @Test
    @DisplayName("Should handle context operations correctly")
    void shouldHandleContextOperationsCorrectly() throws EventHandlingException {
        // Given
        when(mockContext.getTopic()).thenReturn("test.topic");
        when(mockContext.getPartition()).thenReturn(0);
        when(mockContext.getOffset()).thenReturn(1234L);
        when(mockContext.getHeaders()).thenReturn(Map.of(
            "messageId", "msg123",
            "contentType", "application/json"
        ));

        ContextAwareEventHandler contextHandler = new ContextAwareEventHandler();

        // When
        contextHandler.handle(testEvent, mockContext);

        // Then
        verify(mockContext).getTopic();
        verify(mockContext).getPartition();
        verify(mockContext).getOffset();
        verify(mockContext).getHeaders();
        verify(mockContext).acknowledge();
    }

    @Test
    @DisplayName("Should handle event publishing from context")
    void shouldHandleEventPublishingFromContext() throws EventHandlingException {
        // Given
        TestDomainEvent newEvent = new TestDomainEvent(UUID.randomUUID(), "NewAggregate", "test-service");
        EventPublishingHandler publishingHandler = new EventPublishingHandler(newEvent);

        // When
        publishingHandler.handle(testEvent, mockContext);

        // Then
        verify(mockContext).publishEvent(newEvent);
        verify(mockContext).acknowledge();
    }

    @Test
    @DisplayName("Should create and close spans for tracing")
    void shouldCreateAndCloseSpansForTracing() throws EventHandlingException {
        // Given
        AutoCloseable mockSpan = mock(AutoCloseable.class);
        when(mockContext.createSpan("handleTestEvent")).thenReturn(mockSpan);

        TracingEventHandler tracingHandler = new TracingEventHandler();

        // When
        tracingHandler.handle(testEvent, mockContext);

        // Then
        verify(mockContext).createSpan("handleTestEvent");
        verify(mockContext).acknowledge();
    }

    @Test
    @DisplayName("Should handle errors and reject messages")
    void shouldHandleErrorsAndRejectMessages() {
        // Given
        ErrorEventHandler errorHandler = new ErrorEventHandler();

        // When/Then
        assertThatThrownBy(() -> errorHandler.handle(testEvent, mockContext))
            .isInstanceOf(EventHandlingException.class)
            .hasMessage("Simulated handling error");

        verify(mockContext).reject(true); // Should requeue on error
    }

    @Test
    @DisplayName("Should handle concurrent event processing")
    void shouldHandleConcurrentEventProcessing() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numberOfThreads);
        ConcurrentEventHandler concurrentHandler = new ConcurrentEventHandler();

        // When - Process events concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    TestDomainEvent event = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "test-service");
                    event.initializeEvent();
                    concurrentHandler.handle(event, mockContext);
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
        assertThat(concurrentHandler.getProcessedCount()).isEqualTo(numberOfThreads);
    }

    @Test
    @DisplayName("Should handle retry scenarios")
    void shouldHandleRetryScenarios() {
        // Given
        RetryEventHandler retryHandler = new RetryEventHandler();

        // First attempt - should fail
        assertThatThrownBy(() -> retryHandler.handle(testEvent, mockContext))
            .isInstanceOf(EventHandlingException.class);

        // Second attempt - should succeed
        assertThatCode(() -> retryHandler.handle(testEvent, mockContext))
            .doesNotThrowAnyException();

        // Then
        assertThat(retryHandler.getAttemptCount()).isEqualTo(2);
        verify(mockContext, times(1)).reject(true); // Only first attempt should reject
        verify(mockContext, times(1)).acknowledge(); // Second attempt should acknowledge
    }

    @Test
    @DisplayName("Should handle message acknowledgment correctly")
    void shouldHandleMessageAcknowledgmentCorrectly() throws EventHandlingException {
        // Given
        ManualAckEventHandler manualAckHandler = new ManualAckEventHandler();

        // When
        manualAckHandler.handle(testEvent, mockContext);

        // Then
        verify(mockContext).acknowledge();
        verify(mockContext, never()).reject(anyBoolean());
    }

    @Test
    @DisplayName("Should handle message rejection without requeue")
    void shouldHandleMessageRejectionWithoutRequeue() {
        // Given
        RejectEventHandler rejectHandler = new RejectEventHandler();

        // When/Then
        assertThatThrownBy(() -> rejectHandler.handle(testEvent, mockContext))
            .isInstanceOf(EventHandlingException.class);

        verify(mockContext).reject(false); // No requeue
        verify(mockContext, never()).acknowledge();
    }

    @Test
    @DisplayName("Should validate always handle flag")
    void shouldValidateAlwaysHandleFlag() {
        // Given
        AlwaysHandleEventHandler alwaysHandler = new AlwaysHandleEventHandler();

        // When/Then
        assertThat(alwaysHandler.isAlwaysHandle()).isTrue();
        assertThat(eventHandler.isAlwaysHandle()).isFalse();
    }

    @Test
    @DisplayName("Should handle null events gracefully")
    void shouldHandleNullEventsGracefully() {
        // When/Then
        assertThatThrownBy(() -> eventHandler.handle(null, mockContext))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Event cannot be null");
    }

    @Test
    @DisplayName("Should handle null context gracefully")
    void shouldHandleNullContextGracefully() {
        // When/Then
        assertThatThrownBy(() -> eventHandler.handle(testEvent, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Context cannot be null");
    }

    // Test EventHandler implementations

    private static class TestEventHandler implements EventHandler<TestDomainEvent> {
        private final java.util.List<TestDomainEvent> handledEvents = new java.util.ArrayList<>();

        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            if (event == null) throw new IllegalArgumentException("Event cannot be null");
            if (context == null) throw new IllegalArgumentException("Context cannot be null");
            
            handledEvents.add(event);
            context.acknowledge();
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }

        @Override
        public int getPriority() {
            return 100;
        }

        public java.util.List<TestDomainEvent> getHandledEvents() {
            return handledEvents;
        }
    }

    private static class ContextAwareEventHandler implements EventHandler<TestDomainEvent> {
        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            // Access all context information
            String topic = context.getTopic();
            Integer partition = context.getPartition();
            Long offset = context.getOffset();
            Map<String, Object> headers = context.getHeaders();
            
            // Acknowledge the message
            context.acknowledge();
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
    }

    private static class EventPublishingHandler implements EventHandler<TestDomainEvent> {
        private final DomainEvent eventToPublish;

        public EventPublishingHandler(DomainEvent eventToPublish) {
            this.eventToPublish = eventToPublish;
        }

        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            context.publishEvent(eventToPublish);
            context.acknowledge();
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
    }

    private static class TracingEventHandler implements EventHandler<TestDomainEvent> {
        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            try (AutoCloseable span = context.createSpan("handleTestEvent")) {
                // Process event with tracing
                Thread.sleep(1); // Simulate processing
                context.acknowledge();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new EventHandlingException("Error in tracing handler", "TestDomainEvent", "test-id", e);
            } catch (Exception e) {
                throw new EventHandlingException("Error in tracing handler", "TestDomainEvent", "test-id", e);
            }
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
    }

    private static class ErrorEventHandler implements EventHandler<TestDomainEvent> {
        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            context.reject(true); // Requeue on error
            throw new EventHandlingException("Simulated handling error", "TestDomainEvent", "test-id");
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
    }

    private static class ConcurrentEventHandler implements EventHandler<TestDomainEvent> {
        private final AtomicInteger processedCount = new AtomicInteger(0);

        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            processedCount.incrementAndGet();
            // Simulate processing time
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            context.acknowledge();
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }

        public int getProcessedCount() {
            return processedCount.get();
        }
    }

    private static class RetryEventHandler implements EventHandler<TestDomainEvent> {
        private final AtomicInteger attemptCount = new AtomicInteger(0);

        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            int attempt = attemptCount.incrementAndGet();
            
            if (attempt == 1) {
                context.reject(true); // First attempt fails
                throw new EventHandlingException("First attempt failed", "TestDomainEvent", "test-id");
            } else {
                context.acknowledge(); // Second attempt succeeds
            }
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }

        public int getAttemptCount() {
            return attemptCount.get();
        }
    }

    private static class ManualAckEventHandler implements EventHandler<TestDomainEvent> {
        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            // Process event
            context.acknowledge();
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
    }

    private static class RejectEventHandler implements EventHandler<TestDomainEvent> {
        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            context.reject(false); // Don't requeue
            throw new EventHandlingException("Message rejected", "TestDomainEvent", "test-id");
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
    }

    private static class AlwaysHandleEventHandler implements EventHandler<TestDomainEvent> {
        @Override
        public void handle(TestDomainEvent event, EventHandler.EventContext context) throws EventHandlingException {
            context.acknowledge();
        }

        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }

        @Override
        public boolean isAlwaysHandle() {
            return true;
        }
    }

    // Test event classes
    private static class TestDomainEvent extends DomainEvent {
        public TestDomainEvent(UUID aggregateId, String aggregateType, String sourceService) {
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

    private static class AnotherTestEvent extends DomainEvent {
        public AnotherTestEvent(UUID aggregateId, String aggregateType, String sourceService) {
            this.setAggregateId(aggregateId.toString());
            this.setAggregateType(aggregateType);
            this.setSourceService(sourceService);
            this.setEventType("AnotherTestEvent");
            this.setSequenceNumber(System.currentTimeMillis());
        }

        @Override
        public DomainEvent createCopy() {
            return new AnotherTestEvent(UUID.fromString(getAggregateId()), getAggregateType(), getSourceService());
        }
    }
}