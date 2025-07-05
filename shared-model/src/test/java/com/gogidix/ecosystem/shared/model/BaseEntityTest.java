package com.gogidix.ecosystem.shared.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for BaseEntity functionality.
 * Tests audit fields, soft delete, optimistic locking, and entity lifecycle.
 */
class BaseEntityTest {
    
    private TestEntity entity;
    
    @BeforeEach
    void setUp() {
        entity = new TestEntity();
    }
    
    @Test
    @DisplayName("Should generate UUID when entity is created")
    void shouldGenerateUuidOnCreate() {
        // Given - entity is new
        assertThat(entity.getId()).isNull();
        assertThat(entity.isNew()).isTrue();
        
        // When - onCreate is called
        entity.onCreate();
        
        // Then - ID should be generated
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.isNew()).isFalse();
    }
    
    @Test
    @DisplayName("Should not override existing UUID on create")
    void shouldNotOverrideExistingUuidOnCreate() {
        // Given - entity has existing ID
        UUID existingId = UUID.randomUUID();
        entity.setId(existingId);
        
        // When - onCreate is called
        entity.onCreate();
        
        // Then - ID should remain the same
        assertThat(entity.getId()).isEqualTo(existingId);
    }
    
    @Test
    @DisplayName("Should set creation and update timestamps on create")
    void shouldSetTimestampsOnCreate() {
        // Given - timestamps are null
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        
        // When - onCreate is called
        LocalDateTime beforeCreate = LocalDateTime.now();
        entity.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now();
        
        // Then - both timestamps should be set and equal
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
        assertThat(entity.getCreatedAt()).isBetween(beforeCreate, afterCreate);
    }
    
    @Test
    @DisplayName("Should update only updated timestamp on update")
    void shouldUpdateOnlyUpdatedTimestampOnUpdate() {
        // Given - entity with creation timestamps
        entity.onCreate();
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();
        
        // Wait a bit to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - onUpdate is called
        LocalDateTime beforeUpdate = LocalDateTime.now();
        entity.onUpdate();
        LocalDateTime afterUpdate = LocalDateTime.now();
        
        // Then - only updatedAt should change
        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getUpdatedAt()).isNotEqualTo(originalUpdatedAt);
        assertThat(entity.getUpdatedAt()).isBetween(beforeUpdate, afterUpdate);
    }
    
    @Test
    @DisplayName("Should mark entity as deleted with soft delete")
    void shouldMarkAsDeletedWithSoftDelete() {
        // Given - active entity
        String deletedBy = "test-user";
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
        
        // When - marking as deleted
        LocalDateTime beforeDelete = LocalDateTime.now();
        entity.markAsDeleted(deletedBy);
        LocalDateTime afterDelete = LocalDateTime.now();
        
        // Then - entity should be marked as deleted
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isBetween(beforeDelete, afterDelete);
        assertThat(entity.getDeletedBy()).isEqualTo(deletedBy);
    }
    
    @Test
    @DisplayName("Should restore soft-deleted entity")
    void shouldRestoreSoftDeletedEntity() {
        // Given - deleted entity
        entity.markAsDeleted("test-user");
        assertThat(entity.isDeleted()).isTrue();
        
        // When - restoring entity
        entity.restore();
        
        // Then - entity should be restored
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
    }
    
    @Test
    @DisplayName("Should handle equals correctly for entities with same ID")
    void shouldHandleEqualsCorrectlyForSameId() {
        // Given - two entities with same ID
        UUID sharedId = UUID.randomUUID();
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        entity1.setId(sharedId);
        entity2.setId(sharedId);
        
        // When/Then - entities should be equal
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode()); // Same class
    }
    
    @Test
    @DisplayName("Should handle equals correctly for entities with different IDs")
    void shouldHandleEqualsCorrectlyForDifferentIds() {
        // Given - two entities with different IDs
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        entity1.setId(UUID.randomUUID());
        entity2.setId(UUID.randomUUID());
        
        // When/Then - entities should not be equal
        assertThat(entity1).isNotEqualTo(entity2);
    }
    
    @Test
    @DisplayName("Should handle equals correctly for entities with null IDs")
    void shouldHandleEqualsCorrectlyForNullIds() {
        // Given - two entities with null IDs
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        // When/Then - entities should not be equal
        assertThat(entity1).isNotEqualTo(entity2);
    }
    
    @Test
    @DisplayName("Should handle equals correctly for entity compared to itself")
    void shouldHandleEqualsCorrectlyForSameReference() {
        // Given - same entity reference
        TestEntity entity1 = new TestEntity();
        
        // When/Then - entity should equal itself
        assertThat(entity1).isEqualTo(entity1);
    }
    
    @Test
    @DisplayName("Should handle equals correctly for entity compared to null")
    void shouldHandleEqualsCorrectlyForNull() {
        // Given - entity and null
        TestEntity entity1 = new TestEntity();
        
        // When/Then - entity should not equal null
        assertThat(entity1).isNotEqualTo(null);
    }
    
    @Test
    @DisplayName("Should handle equals correctly for entity compared to different type")
    void shouldHandleEqualsCorrectlyForDifferentType() {
        // Given - entity and string
        TestEntity entity1 = new TestEntity();
        String notAnEntity = "not an entity";
        
        // When/Then - entity should not equal different type
        assertThat(entity1).isNotEqualTo(notAnEntity);
    }
    
    @Test
    @DisplayName("Should return consistent hash code")
    void shouldReturnConsistentHashCode() {
        // Given - entity
        TestEntity entity1 = new TestEntity();
        int hashCode1 = entity1.hashCode();
        int hashCode2 = entity1.hashCode();
        
        // When/Then - hash codes should be consistent
        assertThat(hashCode1).isEqualTo(hashCode2);
    }
    
    @Test
    @DisplayName("Should support audit field operations")
    void shouldSupportAuditFieldOperations() {
        // Given - entity with audit fields
        String createdBy = "creator";
        String updatedBy = "updater";
        
        // When - setting audit fields
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(updatedBy);
        entity.setVersion(1L);
        
        // Then - fields should be set correctly
        assertThat(entity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(entity.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(entity.getVersion()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should support builder pattern")
    void shouldSupportBuilderPattern() {
        // Given/When - using builder
        UUID testId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        TestEntity builtEntity = TestEntity.builder()
            .id(testId)
            .version(1L)
            .createdAt(now)
            .createdBy("builder-user")
            .deleted(false)
            .build();
        
        // Then - entity should be built correctly
        assertThat(builtEntity.getId()).isEqualTo(testId);
        assertThat(builtEntity.getVersion()).isEqualTo(1L);
        assertThat(builtEntity.getCreatedAt()).isEqualTo(now);
        assertThat(builtEntity.getCreatedBy()).isEqualTo("builder-user");
        assertThat(builtEntity.isDeleted()).isFalse();
    }
    
    // Test entity implementation for testing
    @lombok.experimental.SuperBuilder
    @lombok.NoArgsConstructor
    private static class TestEntity extends BaseEntity {
        // No additional fields needed for base entity testing
    }
}