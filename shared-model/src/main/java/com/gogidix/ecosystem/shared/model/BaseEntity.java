package com.gogidix.ecosystem.shared.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity class providing common fields for all entities.
 * Implements optimistic locking and audit fields.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for the entity.
     * Uses UUID for distributed system compatibility.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    /**
     * Version field for optimistic locking.
     */
    @Version
    @Column(name = "version")
    private Long version;
    
    /**
     * Timestamp when the entity was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * User who created the entity.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    /**
     * Timestamp when the entity was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * User who last updated the entity.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    /**
     * Soft delete flag.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
    
    /**
     * Timestamp when the entity was deleted (soft delete).
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * User who deleted the entity.
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;
    
    /**
     * Sets creation audit fields before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    
    /**
     * Updates audit fields before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marks the entity as deleted (soft delete).
     * 
     * @param deletedBy User performing the deletion
     */
    public void markAsDeleted(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    /**
     * Restores a soft-deleted entity.
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
    
    /**
     * Checks if the entity is new (not yet persisted).
     * 
     * @return true if the entity has no ID
     */
    @Transient
    public boolean isNew() {
        return id == null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}