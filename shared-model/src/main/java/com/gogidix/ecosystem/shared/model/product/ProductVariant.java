package com.gogidix.ecosystem.shared.model.product;

import com.gogidix.ecosystem.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Product variant entity representing different variations of a product.
 * Examples: different sizes, colors, materials, etc.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_variant_product_id", columnList = "product_id"),
    @Index(name = "idx_variant_sku", columnList = "sku", unique = true),
    @Index(name = "idx_variant_barcode", columnList = "barcode"),
    @Index(name = "idx_variant_available", columnList = "available"),
    @Index(name = "idx_variant_price", columnList = "price"),
    @Index(name = "idx_variant_position", columnList = "position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {
    
    /**
     * The parent product this variant belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_variant_product"))
    private Product product;
    
    /**
     * Variant-specific SKU.
     */
    @Column(name = "sku", length = 50, unique = true)
    private String sku;
    
    /**
     * Variant title/name.
     */
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    /**
     * Variant price (overrides product price if set).
     */
    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;
    
    /**
     * Compare at price for this variant.
     */
    @Column(name = "compare_at_price", precision = 19, scale = 2)
    private BigDecimal compareAtPrice;
    
    /**
     * Cost price for this variant.
     */
    @Column(name = "cost_price", precision = 19, scale = 2)
    private BigDecimal costPrice;
    
    /**
     * Variant barcode/UPC.
     */
    @Column(name = "barcode", length = 20)
    private String barcode;
    
    /**
     * Variant weight in grams (overrides product weight if set).
     */
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;
    
    /**
     * Variant inventory quantity.
     */
    @Column(name = "inventory_quantity")
    private Integer inventoryQuantity = 0;
    
    /**
     * Inventory policy for this variant.
     */
    @Column(name = "inventory_policy", length = 20)
    private String inventoryPolicy = "deny"; // deny, continue
    
    /**
     * Fulfillment service for this variant.
     */
    @Column(name = "fulfillment_service", length = 50)
    private String fulfillmentService = "manual";
    
    /**
     * Inventory management service.
     */
    @Column(name = "inventory_management", length = 50)
    private String inventoryManagement;
    
    /**
     * Whether this variant requires shipping.
     */
    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;
    
    /**
     * Whether this variant is taxable.
     */
    @Column(name = "taxable", nullable = false)
    private boolean taxable = true;
    
    /**
     * Tax code for this specific variant.
     */
    @Column(name = "tax_code", length = 20)
    private String taxCode;
    
    /**
     * Position/order of this variant in the list.
     */
    @Column(name = "position")
    private Integer position = 1;
    
    /**
     * Option 1 value (e.g., "Red" for color option).
     */
    @Column(name = "option1", length = 100)
    private String option1;
    
    /**
     * Option 2 value (e.g., "Large" for size option).
     */
    @Column(name = "option2", length = 100)
    private String option2;
    
    /**
     * Option 3 value (e.g., "Cotton" for material option).
     */
    @Column(name = "option3", length = 100)
    private String option3;
    
    /**
     * Variant image URL.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    /**
     * Whether this variant is available for purchase.
     */
    @Column(name = "available", nullable = false)
    private boolean available = true;
    
    /**
     * Variant-specific metadata as JSON.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Gets the effective price for this variant.
     * Returns variant price if set, otherwise parent product price.
     * 
     * @return The effective price
     */
    @Transient
    public BigDecimal getEffectivePrice() {
        if (price != null) {
            return price;
        }
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }
    
    /**
     * Gets the effective compare at price for this variant.
     * 
     * @return The effective compare at price
     */
    @Transient
    public BigDecimal getEffectiveCompareAtPrice() {
        if (compareAtPrice != null) {
            return compareAtPrice;
        }
        return product != null ? product.getCompareAtPrice() : null;
    }
    
    /**
     * Gets the effective weight for this variant.
     * 
     * @return The effective weight in grams
     */
    @Transient
    public BigDecimal getEffectiveWeight() {
        if (weight != null) {
            return weight;
        }
        return product != null ? product.getWeight() : null;
    }
    
    /**
     * Checks if this variant is on sale.
     * 
     * @return true if variant has a compare at price higher than current price
     */
    @Transient
    public boolean isOnSale() {
        BigDecimal effectivePrice = getEffectivePrice();
        BigDecimal effectiveComparePrice = getEffectiveCompareAtPrice();
        
        return effectiveComparePrice != null && 
               effectiveComparePrice.compareTo(effectivePrice) > 0;
    }
    
    /**
     * Gets the discount percentage if on sale.
     * 
     * @return Discount percentage or null if not on sale
     */
    @Transient
    public BigDecimal getDiscountPercentage() {
        if (!isOnSale()) {
            return null;
        }
        
        BigDecimal effectivePrice = getEffectivePrice();
        BigDecimal effectiveComparePrice = getEffectiveCompareAtPrice();
        
        BigDecimal discount = effectiveComparePrice.subtract(effectivePrice);
        return discount.divide(effectiveComparePrice, 4, BigDecimal.ROUND_HALF_UP)
                      .multiply(new BigDecimal("100"));
    }
    
    /**
     * Checks if this variant is in stock.
     * 
     * @return true if variant is in stock
     */
    @Transient
    public boolean isInStock() {
        if (product != null && !product.isTrackInventory()) {
            return true;
        }
        return inventoryQuantity != null && inventoryQuantity > 0;
    }
    
    /**
     * Checks if this variant can be backordered.
     * 
     * @return true if inventory policy allows continue when out of stock
     */
    @Transient
    public boolean canBackorder() {
        return "continue".equals(inventoryPolicy);
    }
    
    /**
     * Gets the formatted option values as a string.
     * 
     * @return Formatted option string (e.g., "Red / Large / Cotton")
     */
    @Transient
    public String getFormattedOptions() {
        StringBuilder options = new StringBuilder();
        
        if (option1 != null && !option1.trim().isEmpty()) {
            options.append(option1.trim());
        }
        
        if (option2 != null && !option2.trim().isEmpty()) {
            if (options.length() > 0) {
                options.append(" / ");
            }
            options.append(option2.trim());
        }
        
        if (option3 != null && !option3.trim().isEmpty()) {
            if (options.length() > 0) {
                options.append(" / ");
            }
            options.append(option3.trim());
        }
        
        return options.length() > 0 ? options.toString() : "Default";
    }
    
    /**
     * Gets the full variant title including options.
     * 
     * @return Full variant title
     */
    @Transient
    public String getFullTitle() {
        if (product != null) {
            String baseTitle = product.getName();
            String options = getFormattedOptions();
            
            if (!"Default".equals(options)) {
                return baseTitle + " - " + options;
            }
            return baseTitle;
        }
        return title;
    }
    
    /**
     * Checks if this variant is purchasable.
     * 
     * @return true if variant can be purchased
     */
    @Transient
    public boolean isPurchasable() {
        return available && (isInStock() || canBackorder()) && 
               product != null && product.isAvailable();
    }
    
    /**
     * Decrements inventory quantity for a purchase.
     * 
     * @param quantity The quantity to decrement
     * @return true if decrement was successful
     */
    public boolean decrementInventory(int quantity) {
        if (!isInStock() || inventoryQuantity < quantity) {
            if (!canBackorder()) {
                return false;
            }
        }
        
        if (inventoryQuantity != null) {
            inventoryQuantity -= quantity;
            if (inventoryQuantity < 0 && !canBackorder()) {
                inventoryQuantity = 0;
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Increments inventory quantity for a return or restock.
     * 
     * @param quantity The quantity to increment
     */
    public void incrementInventory(int quantity) {
        if (inventoryQuantity == null) {
            inventoryQuantity = quantity;
        } else {
            inventoryQuantity += quantity;
        }
    }
}