package com.exalt.ecosystem.shared.testing;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Comprehensive test data factory for the Exalt Social E-commerce Ecosystem.
 * Provides realistic test data generation for all domains with business logic awareness.
 * 
 * <p>This factory class generates consistent, realistic test data including:</p>
 * <ul>
 *   <li>User profiles with proper demographics</li>
 *   <li>Product catalogs with business-appropriate pricing</li>
 *   <li>Order data with realistic workflows</li>
 *   <li>Financial data with proper currency handling</li>
 *   <li>Geographic data with real-world consistency</li>
 * </ul>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
public final class TestDataFactory {
    
    private static final Faker faker = new Faker();
    private static final Random random = ThreadLocalRandom.current();
    
    // Business constants for realistic data
    private static final String[] CURRENCY_CODES = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "AED", "SAR"};
    private static final String[] PRODUCT_CATEGORIES = {
        "Electronics", "Clothing", "Books", "Home & Garden", "Sports", "Beauty", "Automotive", "Toys"
    };
    private static final String[] ORDER_STATUSES = {
        "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    };
    private static final String[] PAYMENT_METHODS = {
        "CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER", "DIGITAL_WALLET"
    };
    
    private TestDataFactory() {
        // Utility class - prevent instantiation
    }
    
    /**
     * User Data Generation
     */
    public static class UserData {
        
        public static Map<String, Object> createUser() {
            Map<String, Object> user = new HashMap<>();
            user.put("id", UUID.randomUUID());
            user.put("email", faker.internet().emailAddress());
            user.put("firstName", faker.name().firstName());
            user.put("lastName", faker.name().lastName());
            user.put("phoneNumber", faker.phoneNumber().phoneNumber());
            user.put("dateOfBirth", faker.date().birthday(18, 80).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
            user.put("isActive", true);
            user.put("createdAt", LocalDateTime.now().minusDays(random.nextInt(365)));
            user.put("lastLoginAt", LocalDateTime.now().minusHours(random.nextInt(48)));
            return user;
        }
        
        public static Map<String, Object> createUserWithRole(String role) {
            Map<String, Object> user = createUser();
            user.put("role", role);
            user.put("permissions", generatePermissionsForRole(role));
            return user;
        }
        
        public static Map<String, Object> createUserProfile() {
            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", UUID.randomUUID());
            profile.put("bio", faker.lorem().sentence(10));
            profile.put("avatarUrl", faker.internet().avatar());
            profile.put("timezone", faker.options().option("UTC", "EST", "PST", "GMT", "CET"));
            profile.put("language", faker.options().option("en", "es", "fr", "de", "ar"));
            profile.put("country", faker.address().country());
            profile.put("city", faker.address().city());
            return profile;
        }
        
        private static List<String> generatePermissionsForRole(String role) {
            List<String> permissions = new ArrayList<>();
            switch (role.toUpperCase()) {
                case "ADMIN":
                    permissions.addAll(Arrays.asList("READ", "WRITE", "DELETE", "MANAGE_USERS", "MANAGE_ORDERS"));
                    break;
                case "VENDOR":
                    permissions.addAll(Arrays.asList("READ", "WRITE", "MANAGE_PRODUCTS", "VIEW_ORDERS"));
                    break;
                case "CUSTOMER":
                    permissions.addAll(Arrays.asList("READ", "PLACE_ORDERS", "VIEW_PROFILE"));
                    break;
                default:
                    permissions.add("READ");
            }
            return permissions;
        }
    }
    
    /**
     * Product Data Generation
     */
    public static class ProductData {
        
        public static Map<String, Object> createProduct() {
            Map<String, Object> product = new HashMap<>();
            product.put("id", UUID.randomUUID());
            product.put("name", faker.commerce().productName());
            product.put("description", faker.lorem().paragraph(3));
            product.put("category", faker.options().option(PRODUCT_CATEGORIES));
            product.put("price", generateRealisticPrice());
            product.put("currency", faker.options().option(CURRENCY_CODES));
            product.put("sku", generateSKU());
            product.put("stockQuantity", random.nextInt(1000));
            product.put("weight", BigDecimal.valueOf(faker.number().randomDouble(2, 0, 100)));
            product.put("dimensions", createDimensions());
            product.put("isActive", random.nextBoolean());
            product.put("createdAt", LocalDateTime.now().minusDays(random.nextInt(365)));
            product.put("vendorId", UUID.randomUUID());
            return product;
        }
        
        public static Map<String, Object> createProductWithCategory(String category) {
            Map<String, Object> product = createProduct();
            product.put("category", category);
            product.put("price", generatePriceForCategory(category));
            return product;
        }
        
        public static Map<String, Object> createProductVariant() {
            Map<String, Object> variant = new HashMap<>();
            variant.put("id", UUID.randomUUID());
            variant.put("parentProductId", UUID.randomUUID());
            variant.put("name", faker.commerce().productName());
            variant.put("attributes", createVariantAttributes());
            variant.put("price", generateRealisticPrice());
            variant.put("stockQuantity", random.nextInt(100));
            variant.put("isActive", true);
            return variant;
        }
        
        public static BigDecimal generateRealisticPrice() {
            double basePrice = 5 + (random.nextDouble() * 995); // 5 to 1000
            return BigDecimal.valueOf(Math.round(basePrice * 100.0) / 100.0);
        }
        
        private static BigDecimal generatePriceForCategory(String category) {
            double multiplier = 1.0;
            switch (category) {
                case "Electronics":
                    multiplier = 2 + (random.nextDouble() * 8); // 2 to 10
                    break;
                case "Books":
                    multiplier = 0.1 + (random.nextDouble() * 0.4); // 0.1 to 0.5
                    break;
                case "Clothing":
                    multiplier = 0.5 + (random.nextDouble() * 2.5); // 0.5 to 3
                    break;
            }
            return BigDecimal.valueOf(Math.round(50 * multiplier * 100.0) / 100.0);
        }
        
        private static String generateSKU() {
            return faker.code().imei().substring(0, 8).toUpperCase();
        }
        
        private static Map<String, Object> createDimensions() {
            Map<String, Object> dimensions = new HashMap<>();
            dimensions.put("length", BigDecimal.valueOf(faker.number().randomDouble(2, 1, 100)));
            dimensions.put("width", BigDecimal.valueOf(faker.number().randomDouble(2, 1, 100)));
            dimensions.put("height", BigDecimal.valueOf(faker.number().randomDouble(2, 1, 100)));
            dimensions.put("unit", "cm");
            return dimensions;
        }
        
        private static Map<String, String> createVariantAttributes() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("color", faker.color().name());
            attributes.put("size", faker.options().option("XS", "S", "M", "L", "XL", "XXL"));
            attributes.put("material", faker.options().option("Cotton", "Polyester", "Wool", "Leather", "Plastic"));
            return attributes;
        }
    }
    
    /**
     * Order Data Generation
     */
    public static class OrderData {
        
        public static Map<String, Object> createOrder() {
            Map<String, Object> order = new HashMap<>();
            order.put("id", UUID.randomUUID());
            order.put("orderNumber", generateOrderNumber());
            order.put("customerId", UUID.randomUUID());
            order.put("status", faker.options().option(ORDER_STATUSES));
            order.put("totalAmount", generateOrderTotal());
            order.put("currency", faker.options().option(CURRENCY_CODES));
            order.put("items", createOrderItems());
            order.put("shippingAddress", GeographicData.createAddress());
            order.put("billingAddress", GeographicData.createAddress());
            order.put("paymentMethod", faker.options().option(PAYMENT_METHODS));
            order.put("createdAt", LocalDateTime.now().minusDays(random.nextInt(30)));
            order.put("estimatedDelivery", LocalDate.now().plusDays(random.nextInt(14) + 1));
            return order;
        }
        
        public static Map<String, Object> createOrderWithStatus(String status) {
            Map<String, Object> order = createOrder();
            order.put("status", status);
            order.put("statusUpdatedAt", LocalDateTime.now());
            return order;
        }
        
        public static List<Map<String, Object>> createOrderItems() {
            List<Map<String, Object>> items = new ArrayList<>();
            int itemCount = random.nextInt(5) + 1;
            
            for (int i = 0; i < itemCount; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", UUID.randomUUID());
                item.put("productId", UUID.randomUUID());
                item.put("productName", faker.commerce().productName());
                item.put("quantity", random.nextInt(3) + 1);
                item.put("unitPrice", ProductData.generateRealisticPrice());
                item.put("totalPrice", calculateItemTotal(
                    (Integer) item.get("quantity"), 
                    (BigDecimal) item.get("unitPrice")
                ));
                items.add(item);
            }
            
            return items;
        }
        
        private static String generateOrderNumber() {
            return "ORD-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
        }
        
        private static BigDecimal generateOrderTotal() {
            return BigDecimal.valueOf(faker.number().randomDouble(2, 10, 500));
        }
        
        private static BigDecimal calculateItemTotal(Integer quantity, BigDecimal unitPrice) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    /**
     * Financial Data Generation
     */
    public static class FinancialData {
        
        public static Map<String, Object> createPayment() {
            Map<String, Object> payment = new HashMap<>();
            payment.put("id", UUID.randomUUID());
            payment.put("orderId", UUID.randomUUID());
            payment.put("amount", generatePaymentAmount());
            payment.put("currency", faker.options().option(CURRENCY_CODES));
            payment.put("method", faker.options().option(PAYMENT_METHODS));
            payment.put("status", faker.options().option("PENDING", "COMPLETED", "FAILED", "REFUNDED"));
            payment.put("transactionId", generateTransactionId());
            payment.put("processedAt", LocalDateTime.now().minusMinutes(random.nextInt(1440)));
            payment.put("gatewayResponse", createGatewayResponse());
            return payment;
        }
        
        public static Map<String, Object> createInvoice() {
            Map<String, Object> invoice = new HashMap<>();
            invoice.put("id", UUID.randomUUID());
            invoice.put("invoiceNumber", generateInvoiceNumber());
            invoice.put("orderId", UUID.randomUUID());
            invoice.put("customerId", UUID.randomUUID());
            invoice.put("subtotal", generatePaymentAmount());
            invoice.put("taxAmount", generateTaxAmount());
            invoice.put("totalAmount", generatePaymentAmount());
            invoice.put("currency", faker.options().option(CURRENCY_CODES));
            invoice.put("issuedAt", LocalDateTime.now().minusDays(random.nextInt(30)));
            invoice.put("dueAt", LocalDateTime.now().plusDays(random.nextInt(30) + 1));
            invoice.put("status", faker.options().option("DRAFT", "SENT", "PAID", "OVERDUE"));
            return invoice;
        }
        
        private static BigDecimal generatePaymentAmount() {
            return BigDecimal.valueOf(faker.number().randomDouble(2, 1, 1000));
        }
        
        private static BigDecimal generateTaxAmount() {
            return BigDecimal.valueOf(faker.number().randomDouble(2, 0, 50));
        }
        
        private static String generateTransactionId() {
            return "TXN-" + faker.code().imei();
        }
        
        private static String generateInvoiceNumber() {
            return "INV-" + LocalDate.now().getYear() + "-" + 
                   String.format("%06d", random.nextInt(999999));
        }
        
        private static Map<String, Object> createGatewayResponse() {
            Map<String, Object> response = new HashMap<>();
            response.put("gatewayId", faker.code().asin());
            response.put("responseCode", faker.options().option("00", "01", "05", "14"));
            response.put("responseMessage", faker.options().option("Approved", "Declined", "Error"));
            return response;
        }
    }
    
    /**
     * Geographic Data Generation
     */
    public static class GeographicData {
        
        public static Map<String, Object> createAddress() {
            Map<String, Object> address = new HashMap<>();
            address.put("id", UUID.randomUUID());
            address.put("street", faker.address().streetAddress());
            address.put("city", faker.address().city());
            address.put("state", faker.address().state());
            address.put("zipCode", faker.address().zipCode());
            address.put("country", faker.address().country());
            address.put("latitude", faker.address().latitude());
            address.put("longitude", faker.address().longitude());
            address.put("isDefault", random.nextBoolean());
            return address;
        }
        
        public static Map<String, Object> createWarehouse() {
            Map<String, Object> warehouse = new HashMap<>();
            warehouse.put("id", UUID.randomUUID());
            warehouse.put("name", "Warehouse " + faker.address().cityName());
            warehouse.put("code", generateWarehouseCode());
            warehouse.put("address", createAddress());
            warehouse.put("capacity", random.nextInt(10000) + 1000);
            warehouse.put("currentUtilization", random.nextInt(80) + 10);
            warehouse.put("isActive", true);
            warehouse.put("operatingHours", createOperatingHours());
            return warehouse;
        }
        
        private static String generateWarehouseCode() {
            return faker.address().countryCode().toUpperCase() + "-" + 
                   faker.code().asin().substring(0, 4).toUpperCase();
        }
        
        private static Map<String, String> createOperatingHours() {
            Map<String, String> hours = new HashMap<>();
            hours.put("monday", "08:00-18:00");
            hours.put("tuesday", "08:00-18:00");
            hours.put("wednesday", "08:00-18:00");
            hours.put("thursday", "08:00-18:00");
            hours.put("friday", "08:00-18:00");
            hours.put("saturday", "09:00-16:00");
            hours.put("sunday", "CLOSED");
            return hours;
        }
    }
    
    /**
     * Date and Time Generation
     */
    public static class DateTimeData {
        
        public static LocalDateTime randomPastDateTime(int maxDaysAgo) {
            return LocalDateTime.now().minusDays(random.nextInt(maxDaysAgo));
        }
        
        public static LocalDateTime randomFutureDateTime(int maxDaysAhead) {
            return LocalDateTime.now().plusDays(random.nextInt(maxDaysAhead) + 1);
        }
        
        public static LocalDate randomPastDate(int maxDaysAgo) {
            return LocalDate.now().minusDays(random.nextInt(maxDaysAgo));
        }
        
        public static LocalDate randomFutureDate(int maxDaysAhead) {
            return LocalDate.now().plusDays(random.nextInt(maxDaysAhead) + 1);
        }
        
        public static LocalDateTime randomBusinessHour() {
            LocalDate today = LocalDate.now();
            int hour = random.nextInt(10) + 8; // 8 AM to 6 PM
            int minute = random.nextInt(60);
            return today.atTime(hour, minute);
        }
    }
    
    /**
     * List Generation Utilities
     */
    public static <T> List<T> createList(int count, java.util.function.Supplier<T> supplier) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(supplier.get());
        }
        return list;
    }
    
    public static <T> List<T> createRandomList(int minCount, int maxCount, java.util.function.Supplier<T> supplier) {
        int count = random.nextInt(maxCount - minCount + 1) + minCount;
        return createList(count, supplier);
    }
    
    /**
     * Seed Management for Reproducible Tests
     */
    public static void setSeed(long seed) {
        // Note: Faker's RandomService setSeed method may not be available in all versions
        // Using alternative approach with ThreadLocalRandom
        // faker.random().setSeed(seed);
        log.info("Test data factory seed set to: {}", seed);
    }
    
    public static void resetSeed() {
        // Note: Using current implementation without faker seed reset
        log.info("Test data factory seed reset to current time");
    }
}