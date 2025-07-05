package com.gogidix.ecosystem.shared.model.order;

import com.gogidix.ecosystem.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core order entity representing customer orders across all microservices.
 * Provides common order attributes and relationships.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "orderNumber", unique = true),
    @Index(name = "idx_order_customer_id", columnList = "customerId"),
    @Index(name = "idx_order_vendor_id", columnList = "vendorId"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_payment_status", columnList = "paymentStatus"),
    @Index(name = "idx_order_fulfillment_status", columnList = "fulfillmentStatus"),
    @Index(name = "idx_order_total", columnList = "totalAmount"),
    @Index(name = "idx_order_created_at", columnList = "created_at"),
    @Index(name = "idx_order_processed_at", columnList = "processedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {
    
    /**
     * Unique order number for customer reference.
     */
    @Column(name = "order_number", length = 20, nullable = false, unique = true)
    private String orderNumber;
    
    /**
     * Customer who placed the order.
     */
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    /**
     * Primary vendor for this order (in case of multi-vendor).
     */
    @Column(name = "vendor_id")
    private UUID vendorId;
    
    /**
     * Current order status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    /**
     * Payment status of the order.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    
    /**
     * Fulfillment status of the order.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentStatus fulfillmentStatus;
    
    /**
     * Subtotal amount (before tax and shipping).
     */
    @Column(name = "subtotal_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotalAmount;
    
    /**
     * Total tax amount.
     */
    @Column(name = "tax_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    /**
     * Total shipping cost.
     */
    @Column(name = "shipping_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    /**
     * Total discount amount.
     */
    @Column(name = "discount_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    /**
     * Total order amount.
     */
    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    /**
     * Currency code for all amounts.
     */
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    /**
     * Customer email address.
     */
    @Column(name = "customer_email", length = 100, nullable = false)
    private String customerEmail;
    
    /**
     * Customer phone number.
     */
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    /**
     * Billing address as JSON.
     */
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;
    
    /**
     * Shipping address as JSON.
     */
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;
    
    /**
     * Payment method used.
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    /**
     * Payment gateway used.
     */
    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;
    
    /**
     * Payment transaction ID.
     */
    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;
    
    /**
     * Shipping method selected.
     */
    @Column(name = "shipping_method", length = 100)
    private String shippingMethod;
    
    /**
     * Shipping carrier.
     */
    @Column(name = "shipping_carrier", length = 50)
    private String shippingCarrier;
    
    /**
     * Tracking number for shipment.
     */
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    /**
     * Tracking URL for shipment.
     */
    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;
    
    /**
     * Applied coupon code.
     */
    @Column(name = "coupon_code", length = 50)
    private String couponCode;
    
    /**
     * Gift message for the order.
     */
    @Column(name = "gift_message", length = 500)
    private String giftMessage;
    
    /**
     * Special instructions from customer.
     */
    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;
    
    /**
     * Order source (web, mobile, api, etc.).
     */
    @Column(name = "order_source", length = 50, nullable = false)
    private String orderSource = "web";
    
    /**
     * Sales channel (online, in-store, etc.).
     */
    @Column(name = "sales_channel", length = 50)
    private String salesChannel;
    
    /**
     * Marketing campaign source.
     */
    @Column(name = "campaign_source", length = 100)
    private String campaignSource;
    
    /**
     * Referrer URL.
     */
    @Column(name = "referrer_url", length = 500)
    private String referrerUrl;
    
    /**
     * Landing page URL.
     */
    @Column(name = "landing_page_url", length = 500)
    private String landingPageUrl;
    
    /**
     * Whether this is a guest checkout.
     */
    @Column(name = "guest_checkout", nullable = false)
    private boolean guestCheckout = false;
    
    /**
     * Whether this order is a test order.
     */
    @Column(name = "test_order", nullable = false)
    private boolean testOrder = false;
    
    /**
     * Whether this order requires manual review.
     */
    @Column(name = "requires_review", nullable = false)
    private boolean requiresReview = false;
    
    /**
     * Risk assessment score (0-100).
     */
    @Column(name = "risk_score")
    private Integer riskScore;
    
    /**
     * Risk assessment level.
     */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    /**
     * When the order was processed.
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    /**
     * When the order was shipped.
     */
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    /**
     * When the order was delivered.
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    /**
     * When the order was cancelled.
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    /**
     * Reason for cancellation.
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    /**
     * Estimated delivery date.
     */
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    /**
     * Order weight in grams.
     */
    @Column(name = "total_weight", precision = 10, scale = 3)
    private BigDecimal totalWeight;
    
    /**
     * Number of items in the order.
     */
    @Column(name = "item_count", nullable = false)
    private Integer itemCount = 0;
    
    /**
     * Total quantity of all items.
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;
    
    /**
     * Order items.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    /**
     * Order metadata as JSON.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Internal notes for the order.
     */
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;
    
    /**
     * Order tags for categorization.
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * Gets the final total after all calculations.
     * 
     * @return Final total amount
     */
    @Transient
    public BigDecimal getFinalTotal() {
        return subtotalAmount
                .add(taxAmount)
                .add(shippingAmount)
                .subtract(discountAmount);
    }
    
    /**
     * Checks if the order is paid.
     * 
     * @return true if order is fully paid
     */
    @Transient
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID || 
               paymentStatus == PaymentStatus.PARTIALLY_REFUNDED;
    }
    
    /**
     * Checks if the order is shipped.
     * 
     * @return true if order has been shipped
     */
    @Transient
    public boolean isShipped() {
        return fulfillmentStatus == FulfillmentStatus.SHIPPED || 
               fulfillmentStatus == FulfillmentStatus.DELIVERED;
    }
    
    /**
     * Checks if the order is delivered.
     * 
     * @return true if order has been delivered
     */
    @Transient
    public boolean isDelivered() {
        return fulfillmentStatus == FulfillmentStatus.DELIVERED;
    }
    
    /**
     * Checks if the order can be cancelled.
     * 
     * @return true if order can be cancelled
     */
    @Transient
    public boolean canBeCancelled() {
        return status != OrderStatus.CANCELLED && 
               status != OrderStatus.REFUNDED &&
               fulfillmentStatus != FulfillmentStatus.SHIPPED &&
               fulfillmentStatus != FulfillmentStatus.DELIVERED;
    }
    
    /**
     * Checks if the order can be refunded.
     * 
     * @return true if order can be refunded
     */
    @Transient
    public boolean canBeRefunded() {
        return isPaid() && status != OrderStatus.CANCELLED;
    }
    
    /**
     * Gets the display status for the order.
     * 
     * @return Human-readable status
     */
    @Transient
    public String getDisplayStatus() {
        if (status == OrderStatus.CANCELLED) {
            return "Cancelled";
        }
        
        switch (fulfillmentStatus) {
            case DELIVERED:
                return "Delivered";
            case SHIPPED:
                return "Shipped";
            case FULFILLING:
                return "Processing";
            case PENDING:
                if (isPaid()) {
                    return "Processing";
                } else {
                    return "Payment Pending";
                }
            default:
                return status.getDisplayName();
        }
    }
    
    /**
     * Adds an item to the order.
     * 
     * @param item The order item to add
     */
    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
        recalculateTotals();
    }
    
    /**
     * Removes an item from the order.
     * 
     * @param item The order item to remove
     */
    public void removeItem(OrderItem item) {
        if (items != null) {
            items.remove(item);
            item.setOrder(null);
            recalculateTotals();
        }
    }
    
    /**
     * Recalculates order totals based on items.
     */
    public void recalculateTotals() {
        if (items == null || items.isEmpty()) {
            subtotalAmount = BigDecimal.ZERO;
            totalAmount = BigDecimal.ZERO;
            itemCount = 0;
            totalQuantity = 0;
            totalWeight = BigDecimal.ZERO;
            return;
        }
        
        subtotalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        itemCount = items.size();
        
        totalQuantity = items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        totalWeight = items.stream()
                .map(item -> {
                    BigDecimal weight = item.getWeight();
                    return weight != null ? 
                        weight.multiply(new BigDecimal(item.getQuantity())) : 
                        BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalAmount = getFinalTotal();
    }
    
    /**
     * Marks the order as processed.
     */
    public void markAsProcessed() {
        this.status = OrderStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Marks the order as shipped.
     * 
     * @param trackingNumber The tracking number
     * @param carrier The shipping carrier
     */
    public void markAsShipped(String trackingNumber, String carrier) {
        this.fulfillmentStatus = FulfillmentStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
        this.trackingNumber = trackingNumber;
        this.shippingCarrier = carrier;
        
        if (this.status == OrderStatus.PROCESSING) {
            this.status = OrderStatus.SHIPPED;
        }
    }
    
    /**
     * Marks the order as delivered.
     */
    public void markAsDelivered() {
        this.fulfillmentStatus = FulfillmentStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.status = OrderStatus.COMPLETED;
    }
    
    /**
     * Cancels the order.
     * 
     * @param reason The cancellation reason
     */
    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    /**
     * Updates the payment status.
     * 
     * @param newStatus The new payment status
     * @param transactionId The transaction ID
     */
    public void updatePaymentStatus(PaymentStatus newStatus, String transactionId) {
        this.paymentStatus = newStatus;
        this.paymentTransactionId = transactionId;
        
        if (newStatus == PaymentStatus.PAID && status == OrderStatus.PENDING) {
            markAsProcessed();
        }
    }
}