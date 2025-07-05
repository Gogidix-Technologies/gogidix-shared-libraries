package com.gogidix.ecosystem.shared.model.product;

/**
 * Enumeration of product statuses within the ecosystem.
 * Defines the various states a product can be in throughout its lifecycle.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public enum ProductStatus {
    
    /**
     * Product is active and available for purchase.
     */
    ACTIVE("Active", "Product is active and available for purchase", true, true),
    
    /**
     * Product is inactive and not available for purchase.
     */
    INACTIVE("Inactive", "Product is inactive and not available", false, true),
    
    /**
     * Product is in draft state and not published.
     */
    DRAFT("Draft", "Product is in draft state", false, false),
    
    /**
     * Product is pending approval from administrators.
     */
    PENDING_APPROVAL("Pending Approval", "Product is awaiting approval", false, false),
    
    /**
     * Product has been rejected by administrators.
     */
    REJECTED("Rejected", "Product has been rejected", false, false),
    
    /**
     * Product is archived and no longer sold.
     */
    ARCHIVED("Archived", "Product has been archived", false, true),
    
    /**
     * Product is out of stock but will be restocked.
     */
    OUT_OF_STOCK("Out of Stock", "Product is temporarily out of stock", false, true),
    
    /**
     * Product has been discontinued.
     */
    DISCONTINUED("Discontinued", "Product has been discontinued", false, true),
    
    /**
     * Product is under review for policy violations.
     */
    UNDER_REVIEW("Under Review", "Product is under review", false, false),
    
    /**
     * Product is temporarily suspended.
     */
    SUSPENDED("Suspended", "Product has been suspended", false, false),
    
    /**
     * Product has been recalled due to safety issues.
     */
    RECALLED("Recalled", "Product has been recalled", false, false),
    
    /**
     * Product is scheduled to be published at a future date.
     */
    SCHEDULED("Scheduled", "Product is scheduled for future publication", false, false),
    
    /**
     * Product is in pre-order state.
     */
    PRE_ORDER("Pre-order", "Product is available for pre-order", true, true),
    
    /**
     * Product is a backorder item.
     */
    BACKORDER("Backorder", "Product is available on backorder", true, true),
    
    /**
     * Product is being imported/synchronized.
     */
    IMPORTING("Importing", "Product is being imported", false, false),
    
    /**
     * Product has been deleted (soft delete).
     */
    DELETED("Deleted", "Product has been deleted", false, false);
    
    private final String displayName;
    private final String description;
    private final boolean purchasable;
    private final boolean searchable;
    
    /**
     * Constructor for ProductStatus enum.
     * 
     * @param displayName Human-readable status name
     * @param description Status description
     * @param purchasable Whether the product can be purchased in this status
     * @param searchable Whether the product appears in search results
     */
    ProductStatus(String displayName, String description, boolean purchasable, boolean searchable) {
        this.displayName = displayName;
        this.description = description;
        this.purchasable = purchasable;
        this.searchable = searchable;
    }
    
    /**
     * Gets the human-readable display name for the status.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the status.
     * 
     * @return The status description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the product can be purchased in this status.
     * 
     * @return true if the product is purchasable
     */
    public boolean isPurchasable() {
        return purchasable;
    }
    
    /**
     * Checks if the product appears in search results.
     * 
     * @return true if the product is searchable
     */
    public boolean isSearchable() {
        return searchable;
    }
    
    /**
     * Checks if the status is a published state.
     * 
     * @return true if the product is considered published
     */
    public boolean isPublished() {
        return this == ACTIVE || this == INACTIVE || this == OUT_OF_STOCK || 
               this == DISCONTINUED || this == PRE_ORDER || this == BACKORDER;
    }
    
    /**
     * Checks if the status requires administrative attention.
     * 
     * @return true if admin intervention is needed
     */
    public boolean requiresAdminAttention() {
        return this == PENDING_APPROVAL || this == UNDER_REVIEW || 
               this == RECALLED || this == REJECTED;
    }
    
    /**
     * Checks if the status is temporary.
     * 
     * @return true if the status is temporary and can change automatically
     */
    public boolean isTemporary() {
        return this == OUT_OF_STOCK || this == SUSPENDED || this == IMPORTING || 
               this == SCHEDULED || this == UNDER_REVIEW;
    }
    
    /**
     * Checks if the status allows inventory management.
     * 
     * @return true if inventory can be managed
     */
    public boolean allowsInventoryManagement() {
        return this != DELETED && this != ARCHIVED && this != DISCONTINUED;
    }
    
    /**
     * Gets valid transition statuses from the current status.
     * 
     * @return Array of statuses this status can transition to
     */
    public ProductStatus[] getValidTransitions() {
        switch (this) {
            case DRAFT:
                return new ProductStatus[]{PENDING_APPROVAL, ACTIVE, INACTIVE, DELETED};
            
            case PENDING_APPROVAL:
                return new ProductStatus[]{ACTIVE, REJECTED, UNDER_REVIEW, DELETED};
            
            case REJECTED:
                return new ProductStatus[]{DRAFT, PENDING_APPROVAL, DELETED};
            
            case ACTIVE:
                return new ProductStatus[]{INACTIVE, OUT_OF_STOCK, SUSPENDED, UNDER_REVIEW, 
                                         DISCONTINUED, ARCHIVED, RECALLED, DELETED};
            
            case INACTIVE:
                return new ProductStatus[]{ACTIVE, ARCHIVED, DISCONTINUED, DELETED};
            
            case OUT_OF_STOCK:
                return new ProductStatus[]{ACTIVE, INACTIVE, BACKORDER, DISCONTINUED, DELETED};
            
            case SUSPENDED:
                return new ProductStatus[]{ACTIVE, INACTIVE, UNDER_REVIEW, DELETED};
            
            case UNDER_REVIEW:
                return new ProductStatus[]{ACTIVE, INACTIVE, SUSPENDED, REJECTED, RECALLED, DELETED};
            
            case RECALLED:
                return new ProductStatus[]{DISCONTINUED, DELETED};
            
            case SCHEDULED:
                return new ProductStatus[]{ACTIVE, INACTIVE, DRAFT, DELETED};
            
            case PRE_ORDER:
                return new ProductStatus[]{ACTIVE, OUT_OF_STOCK, INACTIVE, DELETED};
            
            case BACKORDER:
                return new ProductStatus[]{ACTIVE, OUT_OF_STOCK, INACTIVE, DELETED};
            
            case IMPORTING:
                return new ProductStatus[]{ACTIVE, INACTIVE, DRAFT, REJECTED, DELETED};
            
            case DISCONTINUED:
                return new ProductStatus[]{ARCHIVED, DELETED};
            
            case ARCHIVED:
                return new ProductStatus[]{DELETED};
            
            case DELETED:
                return new ProductStatus[]{}; // No transitions from deleted
            
            default:
                return new ProductStatus[]{};
        }
    }
    
    /**
     * Checks if transition to another status is valid.
     * 
     * @param targetStatus The target status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(ProductStatus targetStatus) {
        ProductStatus[] validTransitions = getValidTransitions();
        for (ProductStatus status : validTransitions) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the priority level of the status (higher = more urgent attention needed).
     * 
     * @return Priority level (1-10)
     */
    public int getPriorityLevel() {
        switch (this) {
            case RECALLED:
                return 10;
            case UNDER_REVIEW:
                return 9;
            case PENDING_APPROVAL:
                return 8;
            case REJECTED:
                return 7;
            case SUSPENDED:
                return 6;
            case OUT_OF_STOCK:
                return 5;
            case IMPORTING:
                return 4;
            case DRAFT:
                return 3;
            case SCHEDULED:
                return 2;
            case ACTIVE:
            case INACTIVE:
            case PRE_ORDER:
            case BACKORDER:
                return 1;
            case DISCONTINUED:
            case ARCHIVED:
            case DELETED:
                return 0;
            default:
                return 0;
        }
    }
    
    /**
     * Gets the color code associated with the status for UI display.
     * 
     * @return Color code (hex format)
     */
    public String getColorCode() {
        switch (this) {
            case ACTIVE:
                return "#28a745"; // Green
            case INACTIVE:
                return "#6c757d"; // Gray
            case DRAFT:
                return "#17a2b8"; // Blue
            case PENDING_APPROVAL:
                return "#ffc107"; // Yellow
            case REJECTED:
                return "#dc3545"; // Red
            case ARCHIVED:
                return "#343a40"; // Dark Gray
            case OUT_OF_STOCK:
                return "#fd7e14"; // Orange
            case DISCONTINUED:
                return "#6f42c1"; // Purple
            case UNDER_REVIEW:
                return "#20c997"; // Teal
            case SUSPENDED:
                return "#e83e8c"; // Pink
            case RECALLED:
                return "#721c24"; // Dark Red
            case SCHEDULED:
                return "#007bff"; // Primary Blue
            case PRE_ORDER:
                return "#17a2b8"; // Info Blue
            case BACKORDER:
                return "#fd7e14"; // Warning Orange
            case IMPORTING:
                return "#6c757d"; // Secondary Gray
            case DELETED:
                return "#000000"; // Black
            default:
                return "#6c757d"; // Default Gray
        }
    }
    
    /**
     * Gets the icon name associated with the status for UI display.
     * 
     * @return Icon name (FontAwesome compatible)
     */
    public String getIconName() {
        switch (this) {
            case ACTIVE:
                return "check-circle";
            case INACTIVE:
                return "pause-circle";
            case DRAFT:
                return "edit";
            case PENDING_APPROVAL:
                return "clock";
            case REJECTED:
                return "times-circle";
            case ARCHIVED:
                return "archive";
            case OUT_OF_STOCK:
                return "exclamation-triangle";
            case DISCONTINUED:
                return "stop-circle";
            case UNDER_REVIEW:
                return "eye";
            case SUSPENDED:
                return "ban";
            case RECALLED:
                return "exclamation-circle";
            case SCHEDULED:
                return "calendar";
            case PRE_ORDER:
                return "shopping-cart";
            case BACKORDER:
                return "redo";
            case IMPORTING:
                return "download";
            case DELETED:
                return "trash";
            default:
                return "question-circle";
        }
    }
}