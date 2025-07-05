package com.gogidix.ecosystem.shared.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for DomainEvent functionality.
 * Tests event lifecycle, metadata management, relationships, and validation.
 */
class DomainEventTest {

    private TestDomainEvent testEvent;
    private UUID aggregateId;
    private String sourceService;

    @BeforeEach
    void setUp() {
        aggregateId = UUID.randomUUID();
        sourceService = "test-service";
        testEvent = new TestDomainEvent(aggregateId, "TestAggregate", sourceService);
    }

    @Test
    @DisplayName("Should initialize event with required fields")
    void shouldInitializeEventWithRequiredFields() {
        // When
        testEvent.initializeEvent();

        // Then
        assertThat(testEvent.getEventId()).isNotNull();
        assertThat(testEvent.getEventType()).isEqualTo("TestDomainEvent");
        assertThat(testEvent.getAggregateId()).isEqualTo(aggregateId.toString());
        assertThat(testEvent.getAggregateType()).isEqualTo("TestAggregate");
        assertThat(testEvent.getSourceService()).isEqualTo(sourceService);
        assertThat(testEvent.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(testEvent.getVersion()).isEqualTo("1.0");
        assertThat(testEvent.getSequenceNumber()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should generate unique event IDs")
    void shouldGenerateUniqueEventIds() {
        // Given
        TestDomainEvent event1 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service1");
        TestDomainEvent event2 = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", "service2");

        // When
        event1.initializeEvent();
        event2.initializeEvent();

        // Then
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event1.getSequenceNumber()).isNotEqualTo(event2.getSequenceNumber());
    }

    @ParameterizedTest
    @EnumSource(DomainEvent.EventPriority.class)
    @DisplayName("Should handle all event priorities")
    void shouldHandleAllEventPriorities(DomainEvent.EventPriority priority) {
        // When
        testEvent.setPriority(priority);

        // Then
        assertThat(testEvent.getPriority()).isEqualTo(priority);
    }

    @Test
    @DisplayName("Should manage tags correctly")
    void shouldManageTagsCorrectly() {
        // When
        testEvent.addTag("environment", "production");
        testEvent.addTag("region", "us-east-1");
        testEvent.addTag("version", "v2.0");

        // Then
        assertThat(testEvent.getTag("environment")).isEqualTo("production");
        assertThat(testEvent.getTag("region")).isEqualTo("us-east-1");
        assertThat(testEvent.getTag("version")).isEqualTo("v2.0");
        assertThat(testEvent.getTag("nonexistent")).isNull();
        assertThat(testEvent.getTags()).hasSize(3);
    }

    @Test
    @DisplayName("Should manage metadata correctly")
    void shouldManageMetadataCorrectly() {
        // When
        testEvent.addMetadata("requestId", UUID.randomUUID());
        testEvent.addMetadata("userAgent", "Test-Client/1.0");
        testEvent.addMetadata("payload", new TestPayload("test", 123));

        // Then
        assertThat(testEvent.getMetadata("requestId")).isInstanceOf(UUID.class);
        assertThat(testEvent.getMetadata("userAgent")).isEqualTo("Test-Client/1.0");
        assertThat(testEvent.getMetadata("payload")).isInstanceOf(TestPayload.class);
        assertThat(testEvent.getMetadata("nonexistent")).isNull();
        assertThat(testEvent.getMetadata()).hasSize(3);
    }

    @Test
    @DisplayName("Should establish event relationships correctly")
    void shouldEstablishEventRelationshipsCorrectly() {
        // Given
        TestDomainEvent causingEvent = new TestDomainEvent(aggregateId, "TestAggregate", sourceService);
        causingEvent.initializeEvent();
        causingEvent.setCorrelationId(UUID.randomUUID().toString());

        // When
        testEvent.setCausationEvent(causingEvent);
        testEvent.initializeEvent();

        // Then
        assertThat(testEvent.getCausationId()).isEqualTo(causingEvent.getEventId().toString());
        assertThat(testEvent.getCorrelationId()).isEqualTo(causingEvent.getCorrelationId());
        assertThat(testEvent.wasCausedBy(causingEvent)).isTrue();
        assertThat(testEvent.isRelatedTo(causingEvent)).isTrue();
    }

    @Test
    @DisplayName("Should detect unrelated events")
    void shouldDetectUnrelatedEvents() {
        // Given
        TestDomainEvent unrelatedEvent = new TestDomainEvent(UUID.randomUUID(), "DifferentAggregate", "other-service");
        unrelatedEvent.initializeEvent();
        testEvent.initializeEvent();

        // When/Then
        assertThat(testEvent.isRelatedTo(unrelatedEvent)).isFalse();
        assertThat(testEvent.wasCausedBy(unrelatedEvent)).isFalse();
    }

    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() {
        // Given - Event without initialization
        TestDomainEvent invalidEvent = new TestDomainEvent();

        // When/Then
        assertThatThrownBy(invalidEvent::validate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Event type cannot be null or empty");
    }

    @Test
    @DisplayName("Should validate aggregate information")
    void shouldValidateAggregateInformation() {
        // Given
        testEvent.initializeEvent();
        testEvent.setAggregateId(null);

        // When/Then
        assertThatThrownBy(testEvent::validate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Aggregate ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should validate source service")
    void shouldValidateSourceService() {
        // Given
        testEvent.initializeEvent();
        testEvent.setSourceService("");

        // When/Then
        assertThatThrownBy(testEvent::validate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Source service cannot be null or empty");
    }

    @Test
    @DisplayName("Should pass validation when all fields are valid")
    void shouldPassValidationWhenAllFieldsAreValid() {
        // Given
        testEvent.initializeEvent();
        testEvent.setUserId("user123");
        testEvent.setSessionId("session456");
        testEvent.setTenantId("tenant789");

        // When/Then
        assertThatCode(testEvent::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create proper copy of event")
    void shouldCreateProperCopyOfEvent() {
        // Given
        testEvent.initializeEvent();
        testEvent.setUserId("user123");
        testEvent.addTag("environment", "test");
        testEvent.addMetadata("requestId", UUID.randomUUID());

        // When
        DomainEvent copy = testEvent.createCopy();

        // Then
        assertThat(copy).isNotSameAs(testEvent);
        assertThat(copy.getEventId()).isNotEqualTo(testEvent.getEventId()); // New event ID
        assertThat(copy.getAggregateId()).isEqualTo(testEvent.getAggregateId());
        assertThat(copy.getAggregateType()).isEqualTo(testEvent.getAggregateType());
        assertThat(copy.getSourceService()).isEqualTo(testEvent.getSourceService());
        assertThat(copy.getUserId()).isEqualTo(testEvent.getUserId());
        assertThat(copy.getTags()).isEqualTo(testEvent.getTags());
        assertThat(copy.getMetadata()).isEqualTo(testEvent.getMetadata());
    }

    @Test
    @DisplayName("Should handle user context correctly")
    void shouldHandleUserContextCorrectly() {
        // When
        testEvent.setUserId("user123");
        testEvent.setSessionId("session456");
        testEvent.setTenantId("tenant789");

        // Then
        assertThat(testEvent.getUserId()).isEqualTo("user123");
        assertThat(testEvent.getSessionId()).isEqualTo("session456");
        assertThat(testEvent.getTenantId()).isEqualTo("tenant789");
    }

    @Test
    @DisplayName("Should handle event versioning")
    void shouldHandleEventVersioning() {
        // When
        testEvent.setVersion("2.1");

        // Then
        assertThat(testEvent.getVersion()).isEqualTo("2.1");
    }

    @Test
    @DisplayName("Should maintain timestamp ordering")
    void shouldMaintainTimestampOrdering() throws InterruptedException {
        // Given
        testEvent.initializeEvent();
        LocalDateTime firstTimestamp = testEvent.getTimestamp();
        
        Thread.sleep(1); // Ensure time difference
        
        TestDomainEvent laterEvent = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", sourceService);
        laterEvent.initializeEvent();

        // Then
        assertThat(laterEvent.getTimestamp()).isAfter(firstTimestamp);
    }

    @Test
    @DisplayName("Should compare events by sequence number")
    void shouldCompareEventsBySequenceNumber() {
        // Given
        testEvent.initializeEvent();
        TestDomainEvent laterEvent = new TestDomainEvent(UUID.randomUUID(), "TestAggregate", sourceService);
        laterEvent.initializeEvent();

        // Then
        assertThat(laterEvent.getSequenceNumber()).isGreaterThan(testEvent.getSequenceNumber());
    }

    @Test
    @DisplayName("Should handle null values gracefully in relationships")
    void shouldHandleNullValuesGracefullyInRelationships() {
        // When/Then
        assertThat(testEvent.isRelatedTo(null)).isFalse();
        assertThat(testEvent.wasCausedBy(null)).isFalse();
        
        assertThatCode(() -> testEvent.setCausationEvent(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should convert to string representation")
    void shouldConvertToStringRepresentation() {
        // Given
        testEvent.initializeEvent();
        testEvent.setUserId("user123");

        // When
        String stringRepresentation = testEvent.toString();

        // Then
        assertThat(stringRepresentation).contains("TestDomainEvent");
        assertThat(stringRepresentation).contains(testEvent.getEventId().toString());
        assertThat(stringRepresentation).contains(testEvent.getAggregateId());
        assertThat(stringRepresentation).contains(testEvent.getEventType());
    }

    // Test implementation of DomainEvent
    private static class TestDomainEvent extends DomainEvent {
        private static final java.util.concurrent.atomic.AtomicLong sequenceCounter = new java.util.concurrent.atomic.AtomicLong(0);
        
        public TestDomainEvent() {
            // No-args constructor for testing validation
        }
        
        public TestDomainEvent(UUID aggregateId, String aggregateType, String sourceService) {
            // Use the builder pattern as per lombok annotations
            this.setAggregateId(aggregateId.toString());
            this.setAggregateType(aggregateType);
            this.setSourceService(sourceService);
            this.setEventType("TestDomainEvent");
            this.setSequenceNumber(sequenceCounter.incrementAndGet()); // Use counter for unique sequence
        }

        @Override
        public DomainEvent createCopy() {
            TestDomainEvent copy = new TestDomainEvent(
                UUID.fromString(getAggregateId()), 
                getAggregateType(), 
                getSourceService()
            );
            copy.initializeEvent();
            copy.setUserId(getUserId());
            copy.setSessionId(getSessionId());
            copy.setTenantId(getTenantId());
            copy.setVersion(getVersion());
            copy.setPriority(getPriority());
            
            // Copy tags and metadata
            if (getTags() != null) {
                getTags().forEach(copy::addTag);
            }
            if (getMetadata() != null) {
                getMetadata().forEach(copy::addMetadata);
            }
            
            return copy;
        }
    }

    // Test payload class
    private static class TestPayload {
        private final String name;
        private final int value;

        public TestPayload(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }
}