package com.gogidix.ecosystem.shared.model.user;

/**
 * Enumeration of user account statuses.
 * Defines the various states a user account can be in.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public enum UserStatus {
    
    /**
     * Account is active and fully functional.
     */
    ACTIVE("Active", "Account is active and fully functional", true),
    
    /**
     * Account is pending activation (e.g., email verification).
     */
    PENDING("Pending", "Account is pending activation", false),
    
    /**
     * Account is temporarily suspended.
     */
    SUSPENDED("Suspended", "Account is temporarily suspended", false),
    
    /**
     * Account is temporarily locked due to security reasons.
     */
    LOCKED("Locked", "Account is locked for security reasons", false),
    
    /**
     * Account is deactivated by the user.
     */
    DEACTIVATED("Deactivated", "Account has been deactivated", false),
    
    /**
     * Account is banned from the platform.
     */
    BANNED("Banned", "Account has been banned", false),
    
    /**
     * Account is under review by administrators.
     */
    UNDER_REVIEW("Under Review", "Account is being reviewed", false),
    
    /**
     * Account registration is incomplete.
     */
    INCOMPLETE("Incomplete", "Account registration is incomplete", false),
    
    /**
     * Account is in dormant state (inactive for extended period).
     */
    DORMANT("Dormant", "Account is dormant due to inactivity", false),
    
    /**
     * Account has been permanently deleted.
     */
    DELETED("Deleted", "Account has been permanently deleted", false);
    
    private final String displayName;
    private final String description;
    private final boolean isActive;
    
    /**
     * Constructor for UserStatus enum.
     * 
     * @param displayName Human-readable status name
     * @param description Status description
     * @param isActive Whether the status allows platform access
     */
    UserStatus(String displayName, String description, boolean isActive) {
        this.displayName = displayName;
        this.description = description;
        this.isActive = isActive;
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
     * Checks if this status allows platform access.
     * 
     * @return true if the user can access the platform
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Checks if the status is temporary (can be changed back to active).
     * 
     * @return true if the status is temporary
     */
    public boolean isTemporary() {
        return this == PENDING || this == SUSPENDED || this == LOCKED || 
               this == DEACTIVATED || this == UNDER_REVIEW || 
               this == INCOMPLETE || this == DORMANT;
    }
    
    /**
     * Checks if the status is permanent (cannot be easily reversed).
     * 
     * @return true if the status is permanent
     */
    public boolean isPermanent() {
        return this == BANNED || this == DELETED;
    }
    
    /**
     * Checks if the status requires administrative intervention.
     * 
     * @return true if admin action is needed to change status
     */
    public boolean requiresAdminIntervention() {
        return this == BANNED || this == UNDER_REVIEW || this == LOCKED;
    }
    
    /**
     * Checks if the status allows login attempts.
     * 
     * @return true if login is allowed
     */
    public boolean allowsLogin() {
        return this == ACTIVE || this == PENDING;
    }
    
    /**
     * Gets valid transition statuses from the current status.
     * 
     * @return Array of statuses this status can transition to
     */
    public UserStatus[] getValidTransitions() {
        switch (this) {
            case ACTIVE:
                return new UserStatus[]{SUSPENDED, LOCKED, DEACTIVATED, BANNED, UNDER_REVIEW, DORMANT, DELETED};
            
            case PENDING:
                return new UserStatus[]{ACTIVE, SUSPENDED, BANNED, DELETED};
            
            case SUSPENDED:
                return new UserStatus[]{ACTIVE, BANNED, UNDER_REVIEW, DELETED};
            
            case LOCKED:
                return new UserStatus[]{ACTIVE, SUSPENDED, BANNED, UNDER_REVIEW, DELETED};
            
            case DEACTIVATED:
                return new UserStatus[]{ACTIVE, SUSPENDED, BANNED, DELETED};
            
            case BANNED:
                return new UserStatus[]{DELETED}; // Only deletion allowed from banned
            
            case UNDER_REVIEW:
                return new UserStatus[]{ACTIVE, SUSPENDED, BANNED, DELETED};
            
            case INCOMPLETE:
                return new UserStatus[]{ACTIVE, PENDING, SUSPENDED, BANNED, DELETED};
            
            case DORMANT:
                return new UserStatus[]{ACTIVE, SUSPENDED, DEACTIVATED, DELETED};
            
            case DELETED:
                return new UserStatus[]{}; // No transitions from deleted
            
            default:
                return new UserStatus[]{};
        }
    }
    
    /**
     * Checks if transition to another status is valid.
     * 
     * @param targetStatus The target status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(UserStatus targetStatus) {
        UserStatus[] validTransitions = getValidTransitions();
        for (UserStatus status : validTransitions) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the severity level of the status (higher = more severe).
     * 
     * @return Severity level (1-10)
     */
    public int getSeverityLevel() {
        switch (this) {
            case DELETED:
                return 10;
            case BANNED:
                return 9;
            case SUSPENDED:
                return 7;
            case LOCKED:
                return 6;
            case UNDER_REVIEW:
                return 5;
            case DEACTIVATED:
                return 4;
            case DORMANT:
                return 3;
            case INCOMPLETE:
                return 2;
            case PENDING:
                return 1;
            case ACTIVE:
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
            case PENDING:
                return "#ffc107"; // Yellow
            case SUSPENDED:
                return "#fd7e14"; // Orange
            case LOCKED:
                return "#dc3545"; // Red
            case DEACTIVATED:
                return "#6c757d"; // Gray
            case BANNED:
                return "#721c24"; // Dark Red
            case UNDER_REVIEW:
                return "#17a2b8"; // Blue
            case INCOMPLETE:
                return "#e83e8c"; // Pink
            case DORMANT:
                return "#343a40"; // Dark Gray
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
            case PENDING:
                return "clock";
            case SUSPENDED:
                return "pause-circle";
            case LOCKED:
                return "lock";
            case DEACTIVATED:
                return "user-slash";
            case BANNED:
                return "ban";
            case UNDER_REVIEW:
                return "eye";
            case INCOMPLETE:
                return "exclamation-circle";
            case DORMANT:
                return "moon";
            case DELETED:
                return "trash";
            default:
                return "question-circle";
        }
    }
}