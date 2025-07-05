package com.gogidix.ecosystem.shared.model.user;

/**
 * Enumeration of user roles within the ecosystem.
 * Defines different permission levels and access rights.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public enum UserRole {
    
    /**
     * System administrator with full access.
     */
    ADMIN("Admin", "Full system administrative access"),
    
    /**
     * Super administrator with unrestricted access.
     */
    SUPER_ADMIN("Super Admin", "Unrestricted system access"),
    
    /**
     * Regular customer user.
     */
    CUSTOMER("Customer", "Standard customer access"),
    
    /**
     * Vendor selling products on the platform.
     */
    VENDOR("Vendor", "Product vendor access"),
    
    /**
     * Warehouse operator managing inventory.
     */
    WAREHOUSE_OPERATOR("Warehouse Operator", "Warehouse operations access"),
    
    /**
     * Warehouse manager with broader warehouse access.
     */
    WAREHOUSE_MANAGER("Warehouse Manager", "Warehouse management access"),
    
    /**
     * Courier delivery person.
     */
    COURIER("Courier", "Delivery operations access"),
    
    /**
     * Courier manager overseeing delivery operations.
     */
    COURIER_MANAGER("Courier Manager", "Courier management access"),
    
    /**
     * Regional administrator for specific geographic areas.
     */
    REGIONAL_ADMIN("Regional Admin", "Regional administrative access"),
    
    /**
     * Corporate administrator for company-wide operations.
     */
    CORPORATE_ADMIN("Corporate Admin", "Corporate administrative access"),
    
    /**
     * Support agent helping customers.
     */
    SUPPORT_AGENT("Support Agent", "Customer support access"),
    
    /**
     * Finance team member handling payments and billing.
     */
    FINANCE_USER("Finance User", "Financial operations access"),
    
    /**
     * Analytics user with reporting access.
     */
    ANALYTICS_USER("Analytics User", "Analytics and reporting access"),
    
    /**
     * Marketing team member managing campaigns.
     */
    MARKETING_USER("Marketing User", "Marketing operations access"),
    
    /**
     * Content moderator reviewing platform content.
     */
    CONTENT_MODERATOR("Content Moderator", "Content moderation access"),
    
    /**
     * API user for system integrations.
     */
    API_USER("API User", "API integration access"),
    
    /**
     * Guest user with limited access.
     */
    GUEST("Guest", "Limited guest access");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructor for UserRole enum.
     * 
     * @param displayName Human-readable role name
     * @param description Role description
     */
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name for the role.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the role.
     * 
     * @return The role description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this role has administrative privileges.
     * 
     * @return true if the role is an admin role
     */
    public boolean isAdmin() {
        return this == ADMIN || this == SUPER_ADMIN || 
               this == REGIONAL_ADMIN || this == CORPORATE_ADMIN;
    }
    
    /**
     * Checks if this role is a management role.
     * 
     * @return true if the role is a management role
     */
    public boolean isManager() {
        return this == WAREHOUSE_MANAGER || this == COURIER_MANAGER || isAdmin();
    }
    
    /**
     * Checks if this role is an operational role.
     * 
     * @return true if the role is operational
     */
    public boolean isOperational() {
        return this == WAREHOUSE_OPERATOR || this == COURIER || 
               this == SUPPORT_AGENT || this == CONTENT_MODERATOR;
    }
    
    /**
     * Checks if this role is business-related.
     * 
     * @return true if the role is business-related
     */
    public boolean isBusiness() {
        return this == VENDOR || this == FINANCE_USER || 
               this == MARKETING_USER || this == ANALYTICS_USER;
    }
    
    /**
     * Checks if this role has customer-facing responsibilities.
     * 
     * @return true if the role interacts with customers
     */
    public boolean isCustomerFacing() {
        return this == SUPPORT_AGENT || this == COURIER || 
               this == VENDOR || this == CONTENT_MODERATOR;
    }
    
    /**
     * Gets the hierarchy level of the role (higher number = more privileges).
     * 
     * @return Hierarchy level (1-10)
     */
    public int getHierarchyLevel() {
        switch (this) {
            case SUPER_ADMIN:
                return 10;
            case ADMIN:
                return 9;
            case CORPORATE_ADMIN:
                return 8;
            case REGIONAL_ADMIN:
                return 7;
            case WAREHOUSE_MANAGER:
            case COURIER_MANAGER:
                return 6;
            case FINANCE_USER:
                return 5;
            case VENDOR:
            case ANALYTICS_USER:
            case MARKETING_USER:
                return 4;
            case WAREHOUSE_OPERATOR:
            case COURIER:
            case SUPPORT_AGENT:
            case CONTENT_MODERATOR:
                return 3;
            case CUSTOMER:
            case API_USER:
                return 2;
            case GUEST:
                return 1;
            default:
                return 1;
        }
    }
    
    /**
     * Checks if this role can assign another role.
     * 
     * @param targetRole The role to check assignment permission for
     * @return true if this role can assign the target role
     */
    public boolean canAssignRole(UserRole targetRole) {
        return this.getHierarchyLevel() > targetRole.getHierarchyLevel();
    }
    
    /**
     * Gets roles that can be assigned by this role.
     * 
     * @return Array of assignable roles
     */
    public UserRole[] getAssignableRoles() {
        return java.util.Arrays.stream(UserRole.values())
                .filter(role -> this.canAssignRole(role))
                .toArray(UserRole[]::new);
    }
}