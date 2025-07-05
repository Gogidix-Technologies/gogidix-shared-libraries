package com.gogidix.ecosystem.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception should be used when attempting to access a resource
 * that doesn't exist in the system.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class ResourceNotFoundException extends BaseException {
    
    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";
    private final String resourceType;
    private final String resourceId;
    
    /**
     * Constructs a new ResourceNotFoundException with a message.
     * Uses default error code and HTTP 404 Not Found status.
     * 
     * @param message The error message
     */
    public ResourceNotFoundException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.NOT_FOUND);
        this.resourceType = null;
        this.resourceId = null;
    }
    
    /**
     * Constructs a new ResourceNotFoundException with a message and cause.
     * Uses default error code and HTTP 404 Not Found status.
     * 
     * @param message The error message
     * @param cause The cause of this exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.NOT_FOUND, cause);
        this.resourceType = null;
        this.resourceId = null;
    }
    
    /**
     * Constructs a new ResourceNotFoundException for a specific resource.
     * 
     * @param resourceType Type of the resource (e.g., "User", "Product")
     * @param resourceId ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(DEFAULT_ERROR_CODE, 
              String.format("%s with ID '%s' not found", resourceType, resourceId), 
              HttpStatus.NOT_FOUND);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Constructs a new ResourceNotFoundException with resource details and cause.
     * 
     * @param resourceType Type of the resource
     * @param resourceId ID of the resource
     * @param cause The cause of this exception
     */
    public ResourceNotFoundException(String resourceType, String resourceId, Throwable cause) {
        super(DEFAULT_ERROR_CODE, 
              String.format("%s with ID '%s' not found", resourceType, resourceId), 
              HttpStatus.NOT_FOUND, cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Constructs a new ResourceNotFoundException with custom error code.
     * 
     * @param errorCode Specific error code
     * @param resourceType Type of the resource
     * @param resourceId ID of the resource
     */
    public ResourceNotFoundException(String errorCode, String resourceType, String resourceId) {
        super(errorCode, 
              String.format("%s with ID '%s' not found", resourceType, resourceId), 
              HttpStatus.NOT_FOUND);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Gets the type of resource that was not found.
     * 
     * @return The resource type, or null if not specified
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Gets the ID of the resource that was not found.
     * 
     * @return The resource ID, or null if not specified
     */
    public String getResourceId() {
        return resourceId;
    }
    
    /**
     * Factory method for user not found exception.
     * 
     * @param userId The user ID that was not found
     * @return ResourceNotFoundException for user
     */
    public static ResourceNotFoundException userNotFound(String userId) {
        return new ResourceNotFoundException("User", userId);
    }
    
    /**
     * Factory method for product not found exception.
     * 
     * @param productId The product ID that was not found
     * @return ResourceNotFoundException for product
     */
    public static ResourceNotFoundException productNotFound(String productId) {
        return new ResourceNotFoundException("Product", productId);
    }
    
    /**
     * Factory method for order not found exception.
     * 
     * @param orderId The order ID that was not found
     * @return ResourceNotFoundException for order
     */
    public static ResourceNotFoundException orderNotFound(String orderId) {
        return new ResourceNotFoundException("Order", orderId);
    }
    
    /**
     * Factory method for vendor not found exception.
     * 
     * @param vendorId The vendor ID that was not found
     * @return ResourceNotFoundException for vendor
     */
    public static ResourceNotFoundException vendorNotFound(String vendorId) {
        return new ResourceNotFoundException("Vendor", vendorId);
    }
    
    /**
     * Factory method for warehouse not found exception.
     * 
     * @param warehouseId The warehouse ID that was not found
     * @return ResourceNotFoundException for warehouse
     */
    public static ResourceNotFoundException warehouseNotFound(String warehouseId) {
        return new ResourceNotFoundException("Warehouse", warehouseId);
    }
}