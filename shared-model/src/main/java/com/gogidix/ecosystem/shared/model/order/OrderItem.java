package com.gogidix.ecosystem.shared.model.order;

import com.gogidix.ecosystem.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order item entity representing individual items within an order.
 * Contains product details and quantities for order line items.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "productId"),
    @Index(name = "idx_order_item_variant_id", columnList = "variantId"),
    @Index(name = "idx_order_item_sku", columnList = "sku"),
    @Index(name = "idx_order_item_vendor_id", columnList = "vendorId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {
    
    /**
     * The order this item belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_order_item_order"))
    private Order order;
    
    /**
     * Product ID for this item.
     */
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    /**
     * Product variant ID (if applicable).
     */
    @Column(name = "variant_id")
    private UUID variantId;
    
    /**
     * Vendor ID who owns this product.
     */
    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;
    
    /**
     * SKU of the product/variant.
     */
    @Column(name = "sku", length = 50, nullable = false)
    private String sku;
    
    /**
     * Product name at time of order.
     */
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    /**
     * Product variant title (if applicable).
     */
    @Column(name = "variant_title", length = 200)
    private String variantTitle;
    
    /**
     * Quantity ordered.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * Unit price at time of order.
     */
    @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    /**
     * Total price for this line item (quantity × unit price).
     */
    @Column(name = "total_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPrice;
    
    /**
     * Currency code for prices.
     */
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    /**
     * Weight per unit in grams.
     */
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;
    
    /**
     * Total weight for this line item.
     */
    @Column(name = "total_weight", precision = 10, scale = 3)
    private BigDecimal totalWeight;
    
    /**
     * Product image URL at time of order.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    /**
     * Product brand.
     */
    @Column(name = "brand", length = 100)
    private String brand;
    
    /**
     * Product category.
     */
    @Column(name = "category", length = 100)
    private String category;
    
    /**
     * Whether this item requires shipping.
     */
    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;
    
    /**
     * Whether this item is taxable.
     */
    @Column(name = "taxable", nullable = false)
    private boolean taxable = true;
    
    /**
     * Tax amount for this line item.
     */
    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    /**
     * Discount amount applied to this item.
     */
    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    /**
     * Fulfillment status for this specific item.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status")
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;
    
    /**
     * Quantity fulfilled for this item.
     */
    @Column(name = "quantity_fulfilled")
    private Integer quantityFulfilled = 0;
    
    /**
     * Quantity shipped for this item.
     */
    @Column(name = "quantity_shipped")
    private Integer quantityShipped = 0;
    
    /**
     * Quantity returned for this item.
     */
    @Column(name = "quantity_returned")
    private Integer quantityReturned = 0;
    
    /**
     * Tracking number for this item's shipment.
     */
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    /**
     * Shipping carrier for this item.
     */
    @Column(name = "shipping_carrier", length = 50)
    private String shippingCarrier;
    
    /**
     * Whether this is a gift item.
     */
    @Column(name = "is_gift", nullable = false)
    private boolean isGift = false;
    
    /**
     * Gift message for this item.
     */
    @Column(name = "gift_message", length = 500)
    private String giftMessage;
    
    /**
     * Custom properties as JSON.
     */
    @Column(name = "custom_properties", columnDefinition = "TEXT")
    private String customProperties;
    
    /**
     * Item-specific notes.
     */
    @Column(name = "notes", length = 1000)
    private String notes;
    
    /**
     * Gets the effective total price including tax.
     * 
     * @return Total price including tax
     */
    @Transient
    public BigDecimal getTotalPriceWithTax() {
        return totalPrice.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }
    
    /**
     * Gets the final price after discount and tax.
     * 
     * @return Final price after all adjustments
     */
    @Transient
    public BigDecimal getFinalPrice() {
        return totalPrice
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }
    
    /**
     * Gets the display name for this item (product + variant).
     * 
     * @return Display name
     */
    @Transient
    public String getDisplayName() {
        if (variantTitle != null && !variantTitle.trim().isEmpty()) {
            return productName + " - " + variantTitle;
        }
        return productName;
    }
    
    /**
     * Checks if this item is fully fulfilled.
     * 
     * @return true if fully fulfilled
     */
    @Transient
    public boolean isFullyFulfilled() {
        return quantityFulfilled != null && quantityFulfilled.equals(quantity);
    }
    
    /**
     * Checks if this item is partially fulfilled.
     * 
     * @return true if partially fulfilled
     */
    @Transient
    public boolean isPartiallyFulfilled() {
        return quantityFulfilled != null && quantityFulfilled > 0 && 
               quantityFulfilled < quantity;
    }
    
    /**
     * Checks if this item is fully shipped.
     * 
     * @return true if fully shipped
     */
    @Transient
    public boolean isFullyShipped() {
        return quantityShipped != null && quantityShipped.equals(quantity);
    }
    
    /**
     * Gets the remaining quantity to fulfill.
     * 
     * @return Remaining quantity
     */
    @Transient
    public Integer getRemainingQuantity() {
        return quantity - (quantityFulfilled != null ? quantityFulfilled : 0);
    }
    
    /**
     * Gets the remaining quantity to ship.
     * 
     * @return Remaining quantity to ship
     */
    @Transient
    public Integer getRemainingToShip() {
        return quantity - (quantityShipped != null ? quantityShipped : 0);
    }
    
    /**
     * Recalculates the total price based on quantity and unit price.
     */
    public void recalculateTotal() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
        
        if (weight != null && quantity != null) {
            this.totalWeight = weight.multiply(new BigDecimal(quantity));
        }
    }
    
    /**
     * Updates the fulfillment quantity.
     * 
     * @param fulfilledQty The quantity fulfilled
     */
    public void updateFulfillment(Integer fulfilledQty) {
        this.quantityFulfilled = fulfilledQty;
        
        if (fulfilledQty.equals(quantity)) {
            this.fulfillmentStatus = FulfillmentStatus.DELIVERED;
        } else if (fulfilledQty > 0) {
            this.fulfillmentStatus = FulfillmentStatus.PARTIALLY_FULFILLED;
        } else {
            this.fulfillmentStatus = FulfillmentStatus.PENDING;
        }
    }
    
    /**
     * Updates the shipped quantity.
     * 
     * @param shippedQty The quantity shipped
     * @param trackingNum The tracking number
     * @param carrier The shipping carrier
     */
    public void updateShipment(Integer shippedQty, String trackingNum, String carrier) {
        this.quantityShipped = shippedQty;
        this.trackingNumber = trackingNum;
        this.shippingCarrier = carrier;
        
        if (shippedQty.equals(quantity)) {
            this.fulfillmentStatus = FulfillmentStatus.SHIPPED;
        } else if (shippedQty > 0) {
            this.fulfillmentStatus = FulfillmentStatus.PARTIALLY_FULFILLED;
        }
    }
    
    /**
     * Processes a return for this item.
     * 
     * @param returnedQty The quantity being returned
     */
    public void processReturn(Integer returnedQty) {
        this.quantityReturned = (this.quantityReturned != null ? this.quantityReturned : 0) + returnedQty;
        
        if (this.quantityReturned.equals(quantity)) {
            this.fulfillmentStatus = FulfillmentStatus.RETURNED;
        } else {
            this.fulfillmentStatus = FulfillmentStatus.RETURNING;
        }
    }
    
    /**
     * Applies a discount to this item.
     * 
     * @param discountAmt The discount amount
     */
    public void applyDiscount(BigDecimal discountAmt) {
        this.discountAmount = discountAmt;
    }
    
    /**
     * Calculates and sets tax amount.
     * 
     * @param taxRate The tax rate (as percentage)
     */
    public void calculateTax(BigDecimal taxRate) {
        if (taxable && taxRate != null) {
            BigDecimal taxableAmount = totalPrice.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
            this.taxAmount = taxableAmount.multiply(taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP));
        }
    }
}