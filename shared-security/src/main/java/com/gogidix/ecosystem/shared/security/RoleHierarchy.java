package com.gogidix.ecosystem.shared.security;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Role hierarchy management for the security system.
 * Defines role relationships and inheritance rules.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Component
public class RoleHierarchy {
    
    // Role hierarchy levels (higher number = more privileges)
    private static final Map<String, Integer> ROLE_LEVELS = new HashMap<>();
    
    // Role inheritance map (child -> parents)
    private static final Map<String, Set<String>> ROLE_INHERITANCE = new HashMap<>();
    
    static {
        initializeRoleLevels();
        initializeRoleInheritance();
    }
    
    /**
     * Initializes role hierarchy levels.
     */
    private static void initializeRoleLevels() {
        // System roles (highest level)
        ROLE_LEVELS.put("SUPER_ADMIN", 100);
        ROLE_LEVELS.put("ADMIN", 90);
        
        // Management roles
        ROLE_LEVELS.put("CORPORATE_ADMIN", 80);
        ROLE_LEVELS.put("REGIONAL_ADMIN", 70);
        
        // Domain-specific management
        ROLE_LEVELS.put("WAREHOUSE_MANAGER", 60);
        ROLE_LEVELS.put("COURIER_MANAGER", 60);
        
        // Finance and analytics
        ROLE_LEVELS.put("FINANCE_USER", 50);
        ROLE_LEVELS.put("ANALYTICS_USER", 45);
        ROLE_LEVELS.put("MARKETING_USER", 40);
        
        // Business users
        ROLE_LEVELS.put("VENDOR", 35);
        
        // Operational roles
        ROLE_LEVELS.put("WAREHOUSE_OPERATOR", 30);
        ROLE_LEVELS.put("COURIER", 30);
        ROLE_LEVELS.put("SUPPORT_AGENT", 30);
        ROLE_LEVELS.put("CONTENT_MODERATOR", 30);
        
        // Standard users
        ROLE_LEVELS.put("CUSTOMER", 20);
        ROLE_LEVELS.put("API_USER", 15);
        ROLE_LEVELS.put("GUEST", 10);
    }
    
    /**
     * Initializes role inheritance relationships.
     */
    private static void initializeRoleInheritance() {
        // Super admin inherits all roles
        ROLE_INHERITANCE.put("SUPER_ADMIN", Set.of(
            "ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN",
            "WAREHOUSE_MANAGER", "COURIER_MANAGER", "FINANCE_USER",
            "ANALYTICS_USER", "MARKETING_USER", "VENDOR",
            "WAREHOUSE_OPERATOR", "COURIER", "SUPPORT_AGENT",
            "CONTENT_MODERATOR", "CUSTOMER", "API_USER"
        ));
        
        // Admin inherits management roles
        ROLE_INHERITANCE.put("ADMIN", Set.of(
            "CORPORATE_ADMIN", "REGIONAL_ADMIN",
            "WAREHOUSE_MANAGER", "COURIER_MANAGER", "FINANCE_USER",
            "ANALYTICS_USER", "MARKETING_USER", "SUPPORT_AGENT",
            "CONTENT_MODERATOR"
        ));
        
        // Corporate admin inherits regional roles
        ROLE_INHERITANCE.put("CORPORATE_ADMIN", Set.of(
            "REGIONAL_ADMIN", "FINANCE_USER", "ANALYTICS_USER",
            "MARKETING_USER", "SUPPORT_AGENT"
        ));
        
        // Regional admin inherits local management
        ROLE_INHERITANCE.put("REGIONAL_ADMIN", Set.of(
            "WAREHOUSE_MANAGER", "COURIER_MANAGER", "SUPPORT_AGENT"
        ));
        
        // Warehouse manager inherits warehouse operations
        ROLE_INHERITANCE.put("WAREHOUSE_MANAGER", Set.of("WAREHOUSE_OPERATOR"));
        
        // Courier manager inherits courier operations
        ROLE_INHERITANCE.put("COURIER_MANAGER", Set.of("COURIER"));
        
        // Finance user has analytics access
        ROLE_INHERITANCE.put("FINANCE_USER", Set.of("ANALYTICS_USER"));
    }
    
    /**
     * Gets the hierarchy level of a role.
     * 
     * @param role Role name
     * @return Hierarchy level (higher = more privileges)
     */
    public int getRoleLevel(String role) {
        return ROLE_LEVELS.getOrDefault(role, 0);
    }
    
    /**
     * Checks if a role has higher or equal level than another.
     * 
     * @param role1 First role
     * @param role2 Second role
     * @return true if role1 >= role2 in hierarchy
     */
    public boolean hasHigherOrEqualLevel(String role1, String role2) {
        return getRoleLevel(role1) >= getRoleLevel(role2);
    }
    
    /**
     * Checks if a role can manage another role.
     * 
     * @param managerRole Manager role
     * @param targetRole Target role
     * @return true if manager can manage target
     */
    public boolean canManageRole(String managerRole, String targetRole) {
        return getRoleLevel(managerRole) > getRoleLevel(targetRole);
    }
    
    /**
     * Gets all roles that a user with given role can access.
     * 
     * @param userRole User's role
     * @return Set of accessible roles
     */
    public Set<String> getAccessibleRoles(String userRole) {
        Set<String> accessible = new HashSet<>();
        accessible.add(userRole); // Can always access own role
        
        // Add inherited roles
        Set<String> inherited = ROLE_INHERITANCE.get(userRole);
        if (inherited != null) {
            accessible.addAll(inherited);
        }
        
        return accessible;
    }
    
    /**
     * Gets all roles that can be assigned by a user with given role.
     * 
     * @param assignerRole Assigner's role
     * @return Set of assignable roles
     */
    public Set<String> getAssignableRoles(String assignerRole) {
        Set<String> assignable = new HashSet<>();
        int assignerLevel = getRoleLevel(assignerRole);
        
        for (Map.Entry<String, Integer> entry : ROLE_LEVELS.entrySet()) {
            String role = entry.getKey();
            int level = entry.getValue();
            
            // Can assign roles with lower level
            if (assignerLevel > level) {
                assignable.add(role);
            }
        }
        
        return assignable;
    }
    
    /**
     * Checks if a role inherits from another role.
     * 
     * @param childRole Child role
     * @param parentRole Parent role
     * @return true if child inherits from parent
     */
    public boolean inheritsFrom(String childRole, String parentRole) {
        Set<String> inherited = ROLE_INHERITANCE.get(childRole);
        return inherited != null && inherited.contains(parentRole);
    }
    
    /**
     * Gets all parent roles for a given role.
     * 
     * @param role Role name
     * @return Set of parent roles
     */
    public Set<String> getParentRoles(String role) {
        return ROLE_INHERITANCE.getOrDefault(role, Collections.emptySet());
    }
    
    /**
     * Gets all child roles for a given role.
     * 
     * @param role Role name
     * @return Set of child roles
     */
    public Set<String> getChildRoles(String role) {
        Set<String> children = new HashSet<>();
        
        for (Map.Entry<String, Set<String>> entry : ROLE_INHERITANCE.entrySet()) {
            if (entry.getValue().contains(role)) {
                children.add(entry.getKey());
            }
        }
        
        return children;
    }
    
    /**
     * Gets all roles in the system.
     * 
     * @return Set of all role names
     */
    public Set<String> getAllRoles() {
        return new HashSet<>(ROLE_LEVELS.keySet());
    }
    
    /**
     * Gets roles grouped by category.
     * 
     * @return Map of category to roles
     */
    public Map<String, Set<String>> getRolesByCategory() {
        Map<String, Set<String>> categories = new HashMap<>();
        
        categories.put("System", Set.of("SUPER_ADMIN", "ADMIN"));
        categories.put("Management", Set.of("CORPORATE_ADMIN", "REGIONAL_ADMIN"));
        categories.put("Domain Management", Set.of("WAREHOUSE_MANAGER", "COURIER_MANAGER"));
        categories.put("Business", Set.of("FINANCE_USER", "ANALYTICS_USER", "MARKETING_USER", "VENDOR"));
        categories.put("Operations", Set.of("WAREHOUSE_OPERATOR", "COURIER", "SUPPORT_AGENT", "CONTENT_MODERATOR"));
        categories.put("Users", Set.of("CUSTOMER", "API_USER", "GUEST"));
        
        return categories;
    }
    
    /**
     * Validates role transition is allowed.
     * 
     * @param currentRole Current role
     * @param newRole New role
     * @param assignerRole Role of user making the change
     * @return true if transition is allowed
     */
    public boolean isValidRoleTransition(String currentRole, String newRole, String assignerRole) {
        // Must be able to assign both current and new roles
        Set<String> assignableRoles = getAssignableRoles(assignerRole);
        return assignableRoles.contains(currentRole) && assignableRoles.contains(newRole);
    }
    
    /**
     * Gets minimum role required to access a resource.
     * 
     * @param resourceType Resource type
     * @return Minimum required role
     */
    public String getMinimumRoleForResource(String resourceType) {
        switch (resourceType.toLowerCase()) {
            case "user_management":
                return "ADMIN";
            case "financial_data":
                return "FINANCE_USER";
            case "analytics":
                return "ANALYTICS_USER";
            case "warehouse_operations":
                return "WAREHOUSE_OPERATOR";
            case "courier_operations":
                return "COURIER";
            case "vendor_data":
                return "VENDOR";
            case "customer_support":
                return "SUPPORT_AGENT";
            case "content_moderation":
                return "CONTENT_MODERATOR";
            default:
                return "CUSTOMER";
        }
    }
}