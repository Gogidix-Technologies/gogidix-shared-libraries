package com.gogidix.ecosystem.shared.model.order;

/**
 * Enumeration of fulfillment statuses for orders.
 * Defines the various states of order fulfillment and shipping.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public enum FulfillmentStatus {
    
    /**
     * Fulfillment is pending - order not yet processed.
     */
    PENDING("Pending", "Order fulfillment is pending", false, false),
    
    /**
     * Order is being prepared for shipment.
     */
    FULFILLING("Fulfilling", "Order is being prepared", false, false),
    
    /**
     * Order has been shipped.
     */
    SHIPPED("Shipped", "Order has been shipped", true, false),
    
    /**
     * Order has been delivered to customer.
     */
    DELIVERED("Delivered", "Order has been delivered", true, true),
    
    /**
     * Order fulfillment was cancelled.
     */
    CANCELLED("Cancelled", "Order fulfillment cancelled", false, false),
    
    /**
     * Order is partially fulfilled.
     */
    PARTIALLY_FULFILLED("Partially Fulfilled", "Order is partially fulfilled", false, false),
    
    /**
     * Order is ready for pickup.
     */
    READY_FOR_PICKUP("Ready for Pickup", "Order is ready for customer pickup", false, false),
    
    /**
     * Order was picked up by customer.
     */
    PICKED_UP("Picked Up", "Order was picked up by customer", false, true),
    
    /**
     * Order is out for delivery.
     */
    OUT_FOR_DELIVERY("Out for Delivery", "Order is out for delivery", true, false),
    
    /**
     * Delivery attempt failed.
     */
    DELIVERY_FAILED("Delivery Failed", "Delivery attempt failed", true, false),
    
    /**
     * Order is being returned.
     */
    RETURNING("Returning", "Order is being returned", false, false),
    
    /**
     * Order return has been completed.
     */
    RETURNED("Returned", "Order has been returned", false, false),
    
    /**
     * Order is in transit to customer.
     */
    IN_TRANSIT("In Transit", "Order is in transit", true, false),
    
    /**
     * Order delivery is on hold.
     */
    ON_HOLD("On Hold", "Order fulfillment is on hold", false, false),
    
    /**
     * Order fulfillment failed.
     */
    FAILED("Failed", "Order fulfillment failed", false, false);
    
    private final String displayName;
    private final String description;
    private final boolean shipped;
    private final boolean completed;
    
    /**
     * Constructor for FulfillmentStatus enum.
     * 
     * @param displayName Human-readable status name
     * @param description Status description
     * @param shipped Whether the order has been shipped
     * @param completed Whether fulfillment is completed
     */
    FulfillmentStatus(String displayName, String description, boolean shipped, boolean completed) {
        this.displayName = displayName;
        this.description = description;
        this.shipped = shipped;
        this.completed = completed;
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
     * Checks if the order has been shipped.
     * 
     * @return true if order is shipped
     */
    public boolean isShipped() {
        return shipped;
    }
    
    /**
     * Checks if fulfillment is completed.
     * 
     * @return true if fulfillment is complete
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Checks if the status indicates an active fulfillment process.
     * 
     * @return true if fulfillment is active
     */
    public boolean isActive() {
        return this == FULFILLING || this == SHIPPED || this == IN_TRANSIT || 
               this == OUT_FOR_DELIVERY || this == PARTIALLY_FULFILLED ||
               this == READY_FOR_PICKUP;
    }
    
    /**
     * Checks if the status requires attention.
     * 
     * @return true if manual attention is needed
     */
    public boolean requiresAttention() {
        return this == FAILED || this == DELIVERY_FAILED || this == ON_HOLD || 
               this == RETURNING;
    }
    
    /**
     * Gets valid transition statuses from the current status.
     * 
     * @return Array of statuses this status can transition to
     */
    public FulfillmentStatus[] getValidTransitions() {
        switch (this) {
            case PENDING:
                return new FulfillmentStatus[]{FULFILLING, CANCELLED, ON_HOLD, FAILED};
            
            case FULFILLING:
                return new FulfillmentStatus[]{SHIPPED, READY_FOR_PICKUP, PARTIALLY_FULFILLED, 
                                              CANCELLED, ON_HOLD, FAILED};
            
            case SHIPPED:
                return new FulfillmentStatus[]{IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, 
                                              DELIVERY_FAILED, RETURNING, ON_HOLD};
            
            case DELIVERED:
                return new FulfillmentStatus[]{RETURNING}; // Only return possible
            
            case CANCELLED:
                return new FulfillmentStatus[]{}; // Terminal state
            
            case PARTIALLY_FULFILLED:
                return new FulfillmentStatus[]{DELIVERED, SHIPPED, CANCELLED, RETURNING};
            
            case READY_FOR_PICKUP:
                return new FulfillmentStatus[]{PICKED_UP, CANCELLED, ON_HOLD};
            
            case PICKED_UP:
                return new FulfillmentStatus[]{RETURNING}; // Only return possible
            
            case OUT_FOR_DELIVERY:
                return new FulfillmentStatus[]{DELIVERED, DELIVERY_FAILED, RETURNING};
            
            case DELIVERY_FAILED:
                return new FulfillmentStatus[]{OUT_FOR_DELIVERY, RETURNING, READY_FOR_PICKUP, ON_HOLD};
            
            case RETURNING:
                return new FulfillmentStatus[]{RETURNED, DELIVERED}; // Return complete or re-deliver
            
            case RETURNED:
                return new FulfillmentStatus[]{}; // Terminal state
            
            case IN_TRANSIT:
                return new FulfillmentStatus[]{OUT_FOR_DELIVERY, DELIVERED, DELIVERY_FAILED, ON_HOLD};
            
            case ON_HOLD:
                return new FulfillmentStatus[]{FULFILLING, SHIPPED, OUT_FOR_DELIVERY, CANCELLED, FAILED};
            
            case FAILED:
                return new FulfillmentStatus[]{PENDING, CANCELLED}; // Can retry or cancel
            
            default:
                return new FulfillmentStatus[]{};
        }
    }
    
    /**
     * Checks if transition to another status is valid.
     * 
     * @param targetStatus The target status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(FulfillmentStatus targetStatus) {
        FulfillmentStatus[] validTransitions = getValidTransitions();
        for (FulfillmentStatus status : validTransitions) {
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
            case DELIVERY_FAILED:
                return 9;
            case ON_HOLD:
                return 8;
            case RETURNING:
                return 7;
            case OUT_FOR_DELIVERY:
                return 6;
            case READY_FOR_PICKUP:
                return 5;
            case FULFILLING:
                return 4;
            case PARTIALLY_FULFILLED:
                return 3;
            case IN_TRANSIT:
            case SHIPPED:
                return 2;
            case PENDING:
                return 1;
            case DELIVERED:
            case PICKED_UP:
            case RETURNED:
            case CANCELLED:
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
            case FULFILLING:
                return "#17a2b8"; // Blue
            case SHIPPED:
                return "#007bff"; // Primary Blue
            case DELIVERED:
                return "#28a745"; // Green
            case CANCELLED:
                return "#6c757d"; // Gray
            case PARTIALLY_FULFILLED:
                return "#20c997"; // Teal
            case READY_FOR_PICKUP:
                return "#fd7e14"; // Orange
            case PICKED_UP:
                return "#28a745"; // Green
            case OUT_FOR_DELIVERY:
                return "#007bff"; // Primary Blue
            case DELIVERY_FAILED:
                return "#dc3545"; // Red
            case RETURNING:
                return "#e83e8c"; // Pink
            case RETURNED:
                return "#6f42c1"; // Purple
            case IN_TRANSIT:
                return "#17a2b8"; // Blue
            case ON_HOLD:
                return "#fd7e14"; // Orange
            case FAILED:
                return "#721c24"; // Dark Red
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
            case FULFILLING:
                return "cog";
            case SHIPPED:
                return "truck";
            case DELIVERED:
                return "check-circle";
            case CANCELLED:
                return "times-circle";
            case PARTIALLY_FULFILLED:
                return "check";
            case READY_FOR_PICKUP:
                return "store";
            case PICKED_UP:
                return "hand-holding";
            case OUT_FOR_DELIVERY:
                return "shipping-fast";
            case DELIVERY_FAILED:
                return "exclamation-triangle";
            case RETURNING:
                return "undo";
            case RETURNED:
                return "undo-alt";
            case IN_TRANSIT:
                return "route";
            case ON_HOLD:
                return "pause-circle";
            case FAILED:
                return "times";
            default:
                return "question-circle";
        }
    }
}