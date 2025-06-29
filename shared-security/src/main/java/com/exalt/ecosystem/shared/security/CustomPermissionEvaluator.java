package com.exalt.ecosystem.shared.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * Custom permission evaluator for method-level security.
 * Implements domain-specific permission logic.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Component
public class CustomPermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {
    
    @Autowired
    private RoleHierarchy roleHierarchy;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String permissionName = permission.toString();
        UserDetailsImpl userDetails = getUserDetails(authentication);
        
        if (userDetails == null) {
            return false;
        }
        
        return evaluatePermission(userDetails, targetDomainObject, permissionName);
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String permissionName = permission.toString();
        UserDetailsImpl userDetails = getUserDetails(authentication);
        
        if (userDetails == null) {
            return false;
        }
        
        return evaluatePermission(userDetails, targetId, targetType, permissionName);
    }
    
    /**
     * Evaluates permission for a domain object.
     * 
     * @param userDetails User details
     * @param targetDomainObject Target domain object
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean evaluatePermission(UserDetailsImpl userDetails, Object targetDomainObject, String permission) {
        // Check system-level permissions
        if (hasSystemPermission(userDetails, permission)) {
            return true;
        }
        
        // Check object-specific permissions
        return hasObjectPermission(userDetails, targetDomainObject, permission);
    }
    
    /**
     * Evaluates permission for a specific target.
     * 
     * @param userDetails User details
     * @param targetId Target ID
     * @param targetType Target type
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean evaluatePermission(UserDetailsImpl userDetails, Serializable targetId, String targetType, String permission) {
        // Check system-level permissions
        if (hasSystemPermission(userDetails, permission)) {
            return true;
        }
        
        // Check type-specific permissions
        return hasTypePermission(userDetails, targetId, targetType, permission);
    }
    
    /**
     * Checks if user has system-level permission.
     * 
     * @param userDetails User details
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasSystemPermission(UserDetailsImpl userDetails, String permission) {
        // Super admin has all permissions
        if (userDetails.hasRole("SUPER_ADMIN")) {
            return true;
        }
        
        // Admin has most permissions
        if (userDetails.hasRole("ADMIN") && !isRestrictedPermission(permission)) {
            return true;
        }
        
        // Check specific permission mappings
        return hasSpecificPermission(userDetails, permission);
    }
    
    /**
     * Checks if user has object-specific permission.
     * 
     * @param userDetails User details
     * @param targetObject Target object
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasObjectPermission(UserDetailsImpl userDetails, Object targetObject, String permission) {
        if (targetObject == null) {
            return false;
        }
        
        // Check ownership
        if (isOwner(userDetails, targetObject)) {
            return hasOwnerPermission(permission);
        }
        
        // Check domain-specific permissions
        return hasDomainPermission(userDetails, targetObject, permission);
    }
    
    /**
     * Checks if user has type-specific permission.
     * 
     * @param userDetails User details
     * @param targetId Target ID
     * @param targetType Target type
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasTypePermission(UserDetailsImpl userDetails, Serializable targetId, String targetType, String permission) {
        switch (targetType.toLowerCase()) {
            case "user":
                return hasUserPermission(userDetails, targetId, permission);
            case "product":
                return hasProductPermission(userDetails, targetId, permission);
            case "order":
                return hasOrderPermission(userDetails, targetId, permission);
            case "warehouse":
                return hasWarehousePermission(userDetails, permission);
            case "courier":
                return hasCourierPermission(userDetails, permission);
            case "financial":
                return hasFinancialPermission(userDetails, permission);
            default:
                return false;
        }
    }
    
    /**
     * Checks user-specific permissions.
     * 
     * @param userDetails User details
     * @param targetId Target user ID
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasUserPermission(UserDetailsImpl userDetails, Serializable targetId, String permission) {
        // Users can manage their own data
        if (targetId instanceof UUID && userDetails.getId().equals(targetId)) {
            return hasOwnerPermission(permission);
        }
        
        // Admin roles can manage users
        if (userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN")) {
            return true;
        }
        
        // Support agents can view user data
        if (userDetails.hasRole("SUPPORT_AGENT") && permission.equals("read")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks product-specific permissions.
     * 
     * @param userDetails User details
     * @param targetId Target product ID
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasProductPermission(UserDetailsImpl userDetails, Serializable targetId, String permission) {
        // Vendors can manage their products
        if (userDetails.hasRole("VENDOR")) {
            return true; // TODO: Check if user owns the product
        }
        
        // Admin roles can manage all products
        if (userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN")) {
            return true;
        }
        
        // Content moderators can moderate products
        if (userDetails.hasRole("CONTENT_MODERATOR") && 
            (permission.equals("read") || permission.equals("moderate"))) {
            return true;
        }
        
        // Customers can view products
        if (userDetails.hasRole("CUSTOMER") && permission.equals("read")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks order-specific permissions.
     * 
     * @param userDetails User details
     * @param targetId Target order ID
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasOrderPermission(UserDetailsImpl userDetails, Serializable targetId, String permission) {
        // Customers can view their orders
        if (userDetails.hasRole("CUSTOMER")) {
            return permission.equals("read"); // TODO: Check if user owns the order
        }
        
        // Vendors can view orders for their products
        if (userDetails.hasRole("VENDOR")) {
            return permission.equals("read") || permission.equals("fulfill");
        }
        
        // Admin roles can manage all orders
        if (userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN")) {
            return true;
        }
        
        // Support agents can view and update orders
        if (userDetails.hasRole("SUPPORT_AGENT")) {
            return permission.equals("read") || permission.equals("update");
        }
        
        return false;
    }
    
    /**
     * Checks warehouse-specific permissions.
     * 
     * @param userDetails User details
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasWarehousePermission(UserDetailsImpl userDetails, String permission) {
        // Warehouse roles
        if (userDetails.hasAnyRole("WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR")) {
            return true;
        }
        
        // Admin roles
        if (userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks courier-specific permissions.
     * 
     * @param userDetails User details
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasCourierPermission(UserDetailsImpl userDetails, String permission) {
        // Courier roles
        if (userDetails.hasAnyRole("COURIER_MANAGER", "COURIER")) {
            return true;
        }
        
        // Admin roles
        if (userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks financial-specific permissions.
     * 
     * @param userDetails User details
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasFinancialPermission(UserDetailsImpl userDetails, String permission) {
        // Finance roles
        if (userDetails.hasRole("FINANCE_USER")) {
            return true;
        }
        
        // Admin roles
        if (userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN")) {
            return true;
        }
        
        // Analytics users can read financial data
        if (userDetails.hasRole("ANALYTICS_USER") && permission.equals("read")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if user has specific permission.
     * 
     * @param userDetails User details
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasSpecificPermission(UserDetailsImpl userDetails, String permission) {
        switch (permission) {
            case "create_user":
                return userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN", "REGIONAL_ADMIN");
            case "delete_user":
                return userDetails.hasAnyRole("ADMIN", "CORPORATE_ADMIN");
            case "view_analytics":
                return userDetails.hasAnyRole("ANALYTICS_USER", "FINANCE_USER", "MARKETING_USER");
            case "manage_content":
                return userDetails.hasRole("CONTENT_MODERATOR");
            case "process_payment":
                return userDetails.hasAnyRole("FINANCE_USER", "VENDOR");
            default:
                return false;
        }
    }
    
    /**
     * Checks if permission is restricted to super admin only.
     * 
     * @param permission Permission name
     * @return true if restricted
     */
    private boolean isRestrictedPermission(String permission) {
        return permission.equals("delete_system") || 
               permission.equals("modify_security") ||
               permission.equals("system_config");
    }
    
    /**
     * Checks if user is owner of the object.
     * 
     * @param userDetails User details
     * @param targetObject Target object
     * @return true if user is owner
     */
    private boolean isOwner(UserDetailsImpl userDetails, Object targetObject) {
        // TODO: Implement ownership checking logic based on object type
        return false;
    }
    
    /**
     * Checks if owner has permission.
     * 
     * @param permission Permission name
     * @return true if owner can perform action
     */
    private boolean hasOwnerPermission(String permission) {
        // Owners can read, update but not delete (depends on business rules)
        return permission.equals("read") || permission.equals("update");
    }
    
    /**
     * Checks domain-specific permissions.
     * 
     * @param userDetails User details
     * @param targetObject Target object
     * @param permission Permission name
     * @return true if permission is granted
     */
    private boolean hasDomainPermission(UserDetailsImpl userDetails, Object targetObject, String permission) {
        // TODO: Implement domain-specific permission logic
        return false;
    }
    
    /**
     * Gets user details from authentication.
     * 
     * @param authentication Authentication object
     * @return UserDetailsImpl or null
     */
    private UserDetailsImpl getUserDetails(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }
}