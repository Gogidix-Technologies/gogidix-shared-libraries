package com.gogidix.ecosystem.shared.model.product;

import com.gogidix.ecosystem.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Core product entity representing products across all microservices.
 * Provides common product attributes and relationships.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_vendor_id", columnList = "vendorId"),
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_brand", columnList = "brand"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_featured", columnList = "featured"),
    @Index(name = "idx_product_tags", columnList = "tags")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {
    
    /**
     * Stock Keeping Unit - unique product identifier.
     */
    @Column(name = "sku", length = 50, nullable = false, unique = true)
    private String sku;
    
    /**
     * Product name/title.
     */
    @Column(name = "name", length = 200, nullable = false)
    private String name;
    
    /**
     * Product description.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Short product description for listings.
     */
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    /**
     * Product brand name.
     */
    @Column(name = "brand", length = 100)
    private String brand;
    
    /**
     * Product category.
     */
    @Column(name = "category", length = 100, nullable = false)
    private String category;
    
    /**
     * Product subcategory.
     */
    @Column(name = "subcategory", length = 100)
    private String subcategory;
    
    /**
     * Product tags for search and filtering.
     */
    @Column(name = "tags", length = 1000)
    private String tags;
    
    /**
     * Product current status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;
    
    /**
     * Base price of the product.
     */
    @Column(name = "price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;
    
    /**
     * Compare at price (original/MSRP price).
     */
    @Column(name = "compare_at_price", precision = 19, scale = 2)
    private BigDecimal compareAtPrice;
    
    /**
     * Cost price for margin calculations.
     */
    @Column(name = "cost_price", precision = 19, scale = 2)
    private BigDecimal costPrice;
    
    /**
     * Currency code for pricing.
     */
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    /**
     * Product weight in grams.
     */
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;
    
    /**
     * Product dimensions - length in centimeters.
     */
    @Column(name = "length_cm", precision = 10, scale = 2)
    private BigDecimal lengthCm;
    
    /**
     * Product dimensions - width in centimeters.
     */
    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm;
    
    /**
     * Product dimensions - height in centimeters.
     */
    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm;
    
    /**
     * Vendor/seller ID who owns this product.
     */
    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;
    
    /**
     * Manufacturer name.
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;
    
    /**
     * Manufacturer Part Number.
     */
    @Column(name = "mpn", length = 50)
    private String mpn;
    
    /**
     * Global Trade Item Number (barcode).
     */
    @Column(name = "gtin", length = 20)
    private String gtin;
    
    /**
     * Main product image URL.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    /**
     * Additional product image URLs (JSON array).
     */
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;
    
    /**
     * Product video URL.
     */
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    /**
     * Minimum order quantity.
     */
    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity = 1;
    
    /**
     * Maximum order quantity (0 = unlimited).
     */
    @Column(name = "max_order_quantity")
    private Integer maxOrderQuantity;
    
    /**
     * Whether the product is featured.
     */
    @Column(name = "featured", nullable = false)
    private boolean featured = false;
    
    /**
     * Whether the product is digital (downloadable).
     */
    @Column(name = "digital", nullable = false)
    private boolean digital = false;
    
    /**
     * Whether the product requires shipping.
     */
    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;
    
    /**
     * Whether the product is taxable.
     */
    @Column(name = "taxable", nullable = false)
    private boolean taxable = true;
    
    /**
     * Tax category for the product.
     */
    @Column(name = "tax_category", length = 50)
    private String taxCategory;
    
    /**
     * Whether inventory tracking is enabled.
     */
    @Column(name = "track_inventory", nullable = false)
    private boolean trackInventory = true;
    
    /**
     * Current inventory quantity.
     */
    @Column(name = "inventory_quantity")
    private Integer inventoryQuantity = 0;
    
    /**
     * Inventory policy when out of stock.
     */
    @Column(name = "inventory_policy", length = 20)
    private String inventoryPolicy = "deny"; // deny, continue
    
    /**
     * SEO title for search engines.
     */
    @Column(name = "seo_title", length = 70)
    private String seoTitle;
    
    /**
     * SEO meta description.
     */
    @Column(name = "seo_description", length = 160)
    private String seoDescription;
    
    /**
     * SEO-friendly URL handle.
     */
    @Column(name = "handle", length = 100)
    private String handle;
    
    /**
     * Product template for custom rendering.
     */
    @Column(name = "template", length = 50)
    private String template;
    
    /**
     * Date when product was published.
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    /**
     * Product average rating.
     */
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
    
    /**
     * Number of reviews for the product.
     */
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;
    
    /**
     * Product variants (sizes, colors, etc.).
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductVariant> variants = new HashSet<>();
    
    /**
     * Product options (variant types).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "product_options",
        joinColumns = @JoinColumn(name = "product_id"),
        indexes = @Index(name = "idx_product_options_product_id", columnList = "product_id")
    )
    @Column(name = "option_name")
    private Set<String> options = new HashSet<>();
    
    /**
     * Product metadata as JSON.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Vendor-specific product data.
     */
    @Column(name = "vendor_data", columnDefinition = "TEXT")
    private String vendorData;
    
    /**
     * Gets the display price (sale price if available, otherwise regular price).
     * 
     * @return The price to display to customers
     */
    @Transient
    public BigDecimal getDisplayPrice() {
        // If compare at price exists and is higher than current price, show current as sale
        if (compareAtPrice != null && compareAtPrice.compareTo(price) > 0) {
            return price;
        }
        return price;
    }
    
    /**
     * Checks if the product is on sale.
     * 
     * @return true if product has a compare at price higher than current price
     */
    @Transient
    public boolean isOnSale() {
        return compareAtPrice != null && compareAtPrice.compareTo(price) > 0;
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
        BigDecimal discount = compareAtPrice.subtract(price);
        return discount.divide(compareAtPrice, 4, BigDecimal.ROUND_HALF_UP)
                      .multiply(new BigDecimal("100"));
    }
    
    /**
     * Checks if the product is published and available.
     * 
     * @return true if product is published and active
     */
    @Transient
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && publishedAt != null && 
               publishedAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * Checks if the product is in stock.
     * 
     * @return true if product is in stock or doesn't track inventory
     */
    @Transient
    public boolean isInStock() {
        if (!trackInventory) {
            return true;
        }
        return inventoryQuantity != null && inventoryQuantity > 0;
    }
    
    /**
     * Checks if the product can be backordered.
     * 
     * @return true if inventory policy allows continue when out of stock
     */
    @Transient
    public boolean canBackorder() {
        return "continue".equals(inventoryPolicy);
    }
    
    /**
     * Gets the product dimensions as a formatted string.
     * 
     * @return Formatted dimensions string
     */
    @Transient
    public String getFormattedDimensions() {
        if (lengthCm == null || widthCm == null || heightCm == null) {
            return null;
        }
        return String.format("%.1f × %.1f × %.1f cm", 
                           lengthCm.doubleValue(), 
                           widthCm.doubleValue(), 
                           heightCm.doubleValue());
    }
    
    /**
     * Adds a variant to the product.
     * 
     * @param variant The variant to add
     */
    public void addVariant(ProductVariant variant) {
        if (variants == null) {
            variants = new HashSet<>();
        }
        variants.add(variant);
        variant.setProduct(this);
    }
    
    /**
     * Removes a variant from the product.
     * 
     * @param variant The variant to remove
     */
    public void removeVariant(ProductVariant variant) {
        if (variants != null) {
            variants.remove(variant);
            variant.setProduct(null);
        }
    }
    
    /**
     * Publishes the product.
     */
    public void publish() {
        this.publishedAt = LocalDateTime.now();
        if (this.status == ProductStatus.DRAFT) {
            this.status = ProductStatus.ACTIVE;
        }
    }
    
    /**
     * Unpublishes the product.
     */
    public void unpublish() {
        this.publishedAt = null;
        this.status = ProductStatus.INACTIVE;
    }
    
    /**
     * Updates the product rating.
     * 
     * @param newRating The new rating to add
     */
    public void updateRating(BigDecimal newRating) {
        if (rating == null || reviewCount == 0) {
            rating = newRating;
            reviewCount = 1;
        } else {
            // Calculate weighted average
            BigDecimal totalRating = rating.multiply(new BigDecimal(reviewCount));
            totalRating = totalRating.add(newRating);
            reviewCount++;
            rating = totalRating.divide(new BigDecimal(reviewCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}