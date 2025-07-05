package com.gogidix.ecosystem.shared.validation.constraints;

import com.gogidix.ecosystem.shared.validation.validators.CurrencyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint annotation for currency validation.
 * Validates currency codes, amounts, and formats according to international standards.
 * 
 * <p>Usage examples:
 * <pre>
 * {@code @ValidCurrency(validationType = ValidCurrency.ValidationType.CODE)}
 * private String currencyCode; // Validates "USD", "EUR", etc.
 * 
 * {@code @ValidCurrency(validationType = ValidCurrency.ValidationType.AMOUNT, maxAmount = "10000.00")}
 * private String amount; // Validates "100.50"
 * 
 * {@code @ValidCurrency(validationType = ValidCurrency.ValidationType.FORMATTED_AMOUNT)}
 * private String formattedAmount; // Validates "100.50 USD"
 * 
 * {@code @ValidCurrency(allowedCurrencies = {"USD", "EUR", "GBP"}, region = "EUROPE")}
 * private String restrictedCurrency;
 * 
 * {@code @ValidCurrency(allowCrypto = true, blockedCurrencies = {"BTC"})}
 * private String cryptoFriendlyCurrency;
 * </pre>
 * 
 * <p>This validator supports:
 * <ul>
 * <li>ISO 4217 currency code validation</li>
 * <li>Cryptocurrency validation (BTC, ETH, etc.)</li>
 * <li>Regional currency restrictions</li>
 * <li>Amount range and decimal place validation</li>
 * <li>Formatted amount validation (amount + currency)</li>
 * <li>Whitelist/blacklist currency filtering</li>
 * </ul>
 * 
 * <p>Supported regions: AFRICA, EUROPE, ASIA, AMERICA, SUPPORTED, ANY
 * <br>Supported validation types: CODE, AMOUNT, FORMATTED_AMOUNT
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidCurrency.List.class)
public @interface ValidCurrency {
    
    /**
     * Validation type enumeration.
     */
    enum ValidationType {
        /**
         * Validates currency code only (e.g., "USD", "EUR").
         */
        CODE,
        
        /**
         * Validates currency amount only (e.g., "100.50").
         */
        AMOUNT,
        
        /**
         * Validates formatted currency amount (e.g., "100.50 USD").
         */
        FORMATTED_AMOUNT
    }
    
    /**
     * Default validation error message.
     * 
     * @return Error message
     */
    String message() default "Currency value is invalid";
    
    /**
     * Validation groups.
     * 
     * @return Validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for validation metadata.
     * 
     * @return Validation payload
     */
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Whether to allow empty/null values.
     * 
     * @return true if empty values are allowed
     */
    boolean allowEmpty() default false;
    
    /**
     * Type of currency validation to perform.
     * 
     * @return Validation type
     */
    ValidationType validationType() default ValidationType.CODE;
    
    /**
     * Array of allowed currency codes.
     * If empty, all supported currencies are allowed.
     * 
     * @return Array of allowed currency codes
     */
    String[] allowedCurrencies() default {};
    
    /**
     * Array of blocked currency codes.
     * These currencies will be rejected even if in allowed list.
     * 
     * @return Array of blocked currency codes
     */
    String[] blockedCurrencies() default {};
    
    /**
     * Regional currency restriction.
     * Supported values: AFRICA, EUROPE, ASIA, AMERICA, SUPPORTED, ANY
     * 
     * @return Region code
     */
    String region() default "ANY";
    
    /**
     * Whether to allow cryptocurrency codes.
     * 
     * @return true if cryptocurrencies are allowed
     */
    boolean allowCrypto() default false;
    
    /**
     * Minimum amount value (for AMOUNT and FORMATTED_AMOUNT types).
     * 
     * @return Minimum amount as string
     */
    String minAmount() default "0.00";
    
    /**
     * Maximum amount value (for AMOUNT and FORMATTED_AMOUNT types).
     * 
     * @return Maximum amount as string
     */
    String maxAmount() default "999999999.99";
    
    /**
     * Maximum number of decimal places allowed.
     * 
     * @return Maximum decimal places
     */
    int maxDecimalPlaces() default 2;
    
    /**
     * Custom error message for invalid currency code.
     * 
     * @return Custom error message
     */
    String codeMessage() default "Invalid currency code format";
    
    /**
     * Custom error message for invalid amount.
     * 
     * @return Custom error message
     */
    String amountMessage() default "Invalid currency amount";
    
    /**
     * Custom error message for blocked currencies.
     * 
     * @return Custom error message
     */
    String blockedMessage() default "Currency is not allowed";
    
    /**
     * Custom error message for regional restrictions.
     * 
     * @return Custom error message
     */
    String regionMessage() default "Currency is not supported in this region";
    
    /**
     * Custom error message for cryptocurrency restrictions.
     * 
     * @return Custom error message
     */
    String cryptoMessage() default "Cryptocurrency is not allowed";
    
    /**
     * Custom error message for amount range violations.
     * 
     * @return Custom error message
     */
    String rangeMessage() default "Amount is outside allowed range";
    
    /**
     * Defines several {@code @ValidCurrency} annotations on the same element.
     */
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidCurrency[] value();
    }
}