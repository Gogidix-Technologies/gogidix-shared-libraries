package com.gogidix.ecosystem.shared.model.order;

/**
 * Enumeration of payment statuses for orders.
 * Defines the various states of payment processing.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public enum PaymentStatus {
    
    /**
     * Payment is pending processing.
     */
    PENDING("Pending", "Payment is awaiting processing", false),
    
    /**
     * Payment is being processed.
     */
    PROCESSING("Processing", "Payment is being processed", false),
    
    /**
     * Payment has been successfully completed.
     */
    PAID("Paid", "Payment completed successfully", true),
    
    /**
     * Payment has failed.
     */
    FAILED("Failed", "Payment processing failed", false),
    
    /**
     * Payment was cancelled by customer or system.
     */
    CANCELLED("Cancelled", "Payment was cancelled", false),
    
    /**
     * Payment has been partially refunded.
     */
    PARTIALLY_REFUNDED("Partially Refunded", "Payment partially refunded", true),
    
    /**
     * Payment has been fully refunded.
     */
    REFUNDED("Refunded", "Payment fully refunded", false),
    
    /**
     * Payment is authorized but not captured.
     */
    AUTHORIZED("Authorized", "Payment authorized, awaiting capture", false),
    
    /**
     * Payment authorization has expired.
     */
    EXPIRED("Expired", "Payment authorization expired", false),
    
    /**
     * Payment is disputed/charged back.
     */
    DISPUTED("Disputed", "Payment is under dispute", false),
    
    /**
     * Payment dispute was resolved in merchant's favor.
     */
    DISPUTE_WON("Dispute Won", "Payment dispute resolved favorably", true),
    
    /**
     * Payment dispute was lost.
     */
    DISPUTE_LOST("Dispute Lost", "Payment dispute was lost", false),
    
    /**
     * Payment is on hold for review.
     */
    ON_HOLD("On Hold", "Payment is on hold for review", false),
    
    /**
     * Payment requires manual review.
     */
    REQUIRES_REVIEW("Requires Review", "Payment requires manual review", false);
    
    private final String displayName;
    private final String description;
    private final boolean consideredPaid;
    
    /**
     * Constructor for PaymentStatus enum.
     * 
     * @param displayName Human-readable status name
     * @param description Status description
     * @param consideredPaid Whether this status indicates payment was received
     */
    PaymentStatus(String displayName, String description, boolean consideredPaid) {
        this.displayName = displayName;
        this.description = description;
        this.consideredPaid = consideredPaid;
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
     * Checks if this status indicates payment was received.
     * 
     * @return true if payment is considered received
     */
    public boolean isConsideredPaid() {
        return consideredPaid;
    }
    
    /**
     * Checks if the status indicates a final state.
     * 
     * @return true if the status is final
     */
    public boolean isFinal() {
        return this == PAID || this == FAILED || this == CANCELLED || 
               this == REFUNDED || this == EXPIRED || this == DISPUTE_LOST;
    }
    
    /**
     * Checks if the status allows refunding.
     * 
     * @return true if refund is possible
     */
    public boolean canBeRefunded() {
        return this == PAID || this == PARTIALLY_REFUNDED || this == DISPUTE_WON;
    }
    
    /**
     * Checks if the status requires attention.
     * 
     * @return true if manual attention is needed
     */
    public boolean requiresAttention() {
        return this == FAILED || this == DISPUTED || this == ON_HOLD || 
               this == REQUIRES_REVIEW || this == DISPUTE_LOST;
    }
    
    /**
     * Gets valid transition statuses from the current status.
     * 
     * @return Array of statuses this status can transition to
     */
    public PaymentStatus[] getValidTransitions() {
        switch (this) {
            case PENDING:
                return new PaymentStatus[]{PROCESSING, AUTHORIZED, FAILED, CANCELLED, ON_HOLD, REQUIRES_REVIEW};
            
            case PROCESSING:
                return new PaymentStatus[]{PAID, FAILED, CANCELLED, ON_HOLD, REQUIRES_REVIEW};
            
            case PAID:
                return new PaymentStatus[]{PARTIALLY_REFUNDED, REFUNDED, DISPUTED};
            
            case FAILED:
                return new PaymentStatus[]{PENDING}; // Can retry
            
            case CANCELLED:
                return new PaymentStatus[]{}; // Terminal state
            
            case PARTIALLY_REFUNDED:
                return new PaymentStatus[]{REFUNDED, DISPUTED};
            
            case REFUNDED:
                return new PaymentStatus[]{}; // Terminal state
            
            case AUTHORIZED:
                return new PaymentStatus[]{PAID, EXPIRED, CANCELLED};
            
            case EXPIRED:
                return new PaymentStatus[]{PENDING}; // Can retry
            
            case DISPUTED:
                return new PaymentStatus[]{DISPUTE_WON, DISPUTE_LOST};
            
            case DISPUTE_WON:
                return new PaymentStatus[]{PARTIALLY_REFUNDED, REFUNDED};
            
            case DISPUTE_LOST:
                return new PaymentStatus[]{}; // Terminal state
            
            case ON_HOLD:
                return new PaymentStatus[]{PROCESSING, PAID, FAILED, CANCELLED, REQUIRES_REVIEW};
            
            case REQUIRES_REVIEW:
                return new PaymentStatus[]{PROCESSING, PAID, FAILED, CANCELLED, ON_HOLD};
            
            default:
                return new PaymentStatus[]{};
        }
    }
    
    /**
     * Checks if transition to another status is valid.
     * 
     * @param targetStatus The target status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(PaymentStatus targetStatus) {
        PaymentStatus[] validTransitions = getValidTransitions();
        for (PaymentStatus status : validTransitions) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the severity level of the status (higher = more problematic).
     * 
     * @return Severity level (1-10)
     */
    public int getSeverityLevel() {
        switch (this) {
            case DISPUTE_LOST:
                return 10;
            case DISPUTED:
                return 9;
            case FAILED:
                return 8;
            case REQUIRES_REVIEW:
                return 7;
            case ON_HOLD:
                return 6;
            case EXPIRED:
                return 5;
            case CANCELLED:
                return 4;
            case PENDING:
                return 3;
            case PROCESSING:
                return 2;
            case AUTHORIZED:
                return 1;
            case PAID:
            case PARTIALLY_REFUNDED:
            case REFUNDED:
            case DISPUTE_WON:
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
            case PAID:
                return "#28a745"; // Green
            case FAILED:
                return "#dc3545"; // Red
            case CANCELLED:
                return "#6c757d"; // Gray
            case PARTIALLY_REFUNDED:
                return "#20c997"; // Teal
            case REFUNDED:
                return "#6f42c1"; // Purple
            case AUTHORIZED:
                return "#007bff"; // Primary Blue
            case EXPIRED:
                return "#fd7e14"; // Orange
            case DISPUTED:
                return "#e83e8c"; // Pink
            case DISPUTE_WON:
                return "#28a745"; // Green
            case DISPUTE_LOST:
                return "#721c24"; // Dark Red
            case ON_HOLD:
                return "#fd7e14"; // Orange
            case REQUIRES_REVIEW:
                return "#ffc107"; // Yellow
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
                return "spinner";
            case PAID:
                return "check-circle";
            case FAILED:
                return "times-circle";
            case CANCELLED:
                return "ban";
            case PARTIALLY_REFUNDED:
                return "undo-alt";
            case REFUNDED:
                return "money-bill-wave";
            case AUTHORIZED:
                return "shield-alt";
            case EXPIRED:
                return "hourglass-end";
            case DISPUTED:
                return "exclamation-triangle";
            case DISPUTE_WON:
                return "trophy";
            case DISPUTE_LOST:
                return "times";
            case ON_HOLD:
                return "pause-circle";
            case REQUIRES_REVIEW:
                return "eye";
            default:
                return "question-circle";
        }
    }
}