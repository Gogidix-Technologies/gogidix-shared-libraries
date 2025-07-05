package com.gogidix.ecosystem.shared.model.order;

/**
 * Enumeration of order statuses within the ecosystem.
 * Defines the various states an order can be in throughout its lifecycle.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public enum OrderStatus {
    
    /**
     * Order has been created but payment is pending.
     */
    PENDING("Pending", "Order created, payment pending", false),
    
    /**
     * Order is confirmed and being processed.
     */
    PROCESSING("Processing", "Order confirmed and being processed", true),
    
    /**
     * Order has been shipped to customer.
     */
    SHIPPED("Shipped", "Order has been shipped", true),
    
    /**
     * Order has been delivered to customer.
     */
    COMPLETED("Completed", "Order delivered successfully", true),
    
    /**
     * Order has been cancelled by customer or admin.
     */
    CANCELLED("Cancelled", "Order has been cancelled", false),
    
    /**
     * Order has been refunded.
     */
    REFUNDED("Refunded", "Order has been refunded", false),
    
    /**
     * Order is on hold awaiting manual review.
     */
    ON_HOLD("On Hold", "Order is on hold for review", false),
    
    /**
     * Order has failed processing.
     */
    FAILED("Failed", "Order processing failed", false),
    
    /**
     * Order is partially fulfilled.
     */
    PARTIALLY_FULFILLED("Partially Fulfilled", "Order is partially fulfilled", true),
    
    /**
     * Order is being returned by customer.
     */
    RETURNED("Returned", "Order is being returned", false),
    
    /**
     * Order return has been processed.
     */
    RETURN_PROCESSED("Return Processed", "Return has been processed", false);
    
    private final String displayName;
    private final String description;
    private final boolean fulfillable;
    
    /**
     * Constructor for OrderStatus enum.
     * 
     * @param displayName Human-readable status name
     * @param description Status description
     * @param fulfillable Whether the order can be fulfilled in this status
     */
    OrderStatus(String displayName, String description, boolean fulfillable) {
        this.displayName = displayName;
        this.description = description;
        this.fulfillable = fulfillable;
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
     * Checks if the order can be fulfilled in this status.
     * 
     * @return true if the order is fulfillable
     */
    public boolean isFulfillable() {
        return fulfillable;
    }
    
    /**
     * Checks if the status indicates a completed order.
     * 
     * @return true if the order is considered complete
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == REFUNDED || this == RETURN_PROCESSED;
    }
    
    /**
     * Checks if the status indicates an active order.
     * 
     * @return true if the order is active
     */
    public boolean isActive() {
        return this == PENDING || this == PROCESSING || this == SHIPPED || this == PARTIALLY_FULFILLED;
    }
    
    /**
     * Checks if the status indicates a cancelled order.
     * 
     * @return true if the order is cancelled
     */
    public boolean isCancelled() {
        return this == CANCELLED || this == FAILED;
    }
    
    /**
     * Gets valid transition statuses from the current status.
     * 
     * @return Array of statuses this status can transition to
     */
    public OrderStatus[] getValidTransitions() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{PROCESSING, CANCELLED, FAILED, ON_HOLD};
            
            case PROCESSING:
                return new OrderStatus[]{SHIPPED, PARTIALLY_FULFILLED, CANCELLED, ON_HOLD, FAILED};
            
            case SHIPPED:
                return new OrderStatus[]{COMPLETED, RETURNED, CANCELLED};
            
            case COMPLETED:
                return new OrderStatus[]{RETURNED, REFUNDED};
            
            case CANCELLED:
                return new OrderStatus[]{REFUNDED}; // Only refund possible from cancelled
            
            case REFUNDED:
                return new OrderStatus[]{}; // Terminal state
            
            case ON_HOLD:
                return new OrderStatus[]{PROCESSING, CANCELLED, FAILED};
            
            case FAILED:
                return new OrderStatus[]{PENDING, CANCELLED}; // Can retry or cancel
            
            case PARTIALLY_FULFILLED:
                return new OrderStatus[]{COMPLETED, CANCELLED, RETURNED};
            
            case RETURNED:
                return new OrderStatus[]{RETURN_PROCESSED, REFUNDED};
            
            case RETURN_PROCESSED:
                return new OrderStatus[]{}; // Terminal state
            
            default:
                return new OrderStatus[]{};
        }
    }
    
    /**
     * Checks if transition to another status is valid.
     * 
     * @param targetStatus The target status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        OrderStatus[] validTransitions = getValidTransitions();
        for (OrderStatus status : validTransitions) {
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
            case FAILED:
                return 10;
            case ON_HOLD:
                return 9;
            case RETURNED:
                return 8;
            case CANCELLED:
                return 7;
            case PENDING:
                return 6;
            case PROCESSING:
                return 5;
            case PARTIALLY_FULFILLED:
                return 4;
            case SHIPPED:
                return 3;
            case COMPLETED:
                return 1;
            case REFUNDED:
            case RETURN_PROCESSED:
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
            case PENDING:
                return "#ffc107"; // Yellow
            case PROCESSING:
                return "#17a2b8"; // Blue
            case SHIPPED:
                return "#007bff"; // Primary Blue
            case COMPLETED:
                return "#28a745"; // Green
            case CANCELLED:
                return "#dc3545"; // Red
            case REFUNDED:
                return "#6c757d"; // Gray
            case ON_HOLD:
                return "#fd7e14"; // Orange
            case FAILED:
                return "#721c24"; // Dark Red
            case PARTIALLY_FULFILLED:
                return "#20c997"; // Teal
            case RETURNED:
                return "#e83e8c"; // Pink
            case RETURN_PROCESSED:
                return "#6f42c1"; // Purple
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
            case PENDING:
                return "clock";
            case PROCESSING:
                return "cog";
            case SHIPPED:
                return "truck";
            case COMPLETED:
                return "check-circle";
            case CANCELLED:
                return "times-circle";
            case REFUNDED:
                return "money-bill-wave";
            case ON_HOLD:
                return "pause-circle";
            case FAILED:
                return "exclamation-triangle";
            case PARTIALLY_FULFILLED:
                return "check";
            case RETURNED:
                return "undo";
            case RETURN_PROCESSED:
                return "check-double";
            default:
                return "question-circle";
        }
    }
}