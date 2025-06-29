package com.exalt.ecosystem.shared.validation.validators;

import com.exalt.ecosystem.shared.validation.constraints.ValidCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Currency validator for custom validation annotation.
 * Validates currency codes, amounts, and formats according to international standards.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, Object> {
    
    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern CURRENCY_AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,4})?$");
    
    // Major currencies supported by the platform
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD",
        "MXN", "SGD", "HKD", "NOK", "KRW", "TRY", "RUB", "INR", "BRL", "ZAR",
        "PLN", "DKK", "CZK", "HUF", "ILS", "CLP", "PHP", "AED", "COP", "PEN",
        "EGP", "SAR", "MAD", "KES", "NGN", "GHS", "TND", "DZD"
    );
    
    // Crypto currencies (if enabled)
    private static final Set<String> CRYPTO_CURRENCIES = Set.of(
        "BTC", "ETH", "LTC", "XRP", "BCH", "ADA", "DOT", "LINK", "XLM", "USDT",
        "USDC", "BNB", "SOL", "AVAX", "MATIC", "UNI", "ATOM", "VET", "TRX", "FIL"
    );
    
    // Regional currency groups
    private static final Set<String> AFRICAN_CURRENCIES = Set.of(
        "ZAR", "EGP", "MAD", "KES", "NGN", "GHS", "TND", "DZD", "XOF", "XAF", "ETB", "UGX"
    );
    
    private static final Set<String> EUROPEAN_CURRENCIES = Set.of(
        "EUR", "GBP", "CHF", "SEK", "NOK", "DKK", "PLN", "CZK", "HUF", "RON", "BGN", "HRK"
    );
    
    private static final Set<String> ASIAN_CURRENCIES = Set.of(
        "JPY", "CNY", "KRW", "INR", "SGD", "HKD", "THB", "MYR", "IDR", "PHP", "VND", "TWD"
    );
    
    private static final Set<String> AMERICAN_CURRENCIES = Set.of(
        "USD", "CAD", "MXN", "BRL", "ARS", "CLP", "COP", "PEN", "UYU", "BOB", "PYG", "VES"
    );
    
    private boolean allowEmpty;
    private String[] allowedCurrencies;
    private String[] blockedCurrencies;
    private String region;
    private boolean allowCrypto;
    private ValidCurrency.ValidationType validationType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private int maxDecimalPlaces;
    
    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.allowedCurrencies = constraintAnnotation.allowedCurrencies();
        this.blockedCurrencies = constraintAnnotation.blockedCurrencies();
        this.region = constraintAnnotation.region();
        this.allowCrypto = constraintAnnotation.allowCrypto();
        this.validationType = constraintAnnotation.validationType();
        this.minAmount = new BigDecimal(constraintAnnotation.minAmount());
        this.maxAmount = new BigDecimal(constraintAnnotation.maxAmount());
        this.maxDecimalPlaces = constraintAnnotation.maxDecimalPlaces();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowEmpty;
        }
        
        String stringValue = value.toString().trim();
        if (stringValue.isEmpty()) {
            return allowEmpty;
        }
        
        switch (validationType) {
            case CODE:
                return isValidCurrencyCode(stringValue, context);
            case AMOUNT:
                return isValidCurrencyAmount(stringValue, context);
            case FORMATTED_AMOUNT:
                return isValidFormattedAmount(stringValue, context);
            default:
                return false;
        }
    }
    
    /**
     * Validates currency code format and support.
     * 
     * @param currencyCode Currency code to validate
     * @param context Validation context
     * @return true if currency code is valid
     */
    private boolean isValidCurrencyCode(String currencyCode, ConstraintValidatorContext context) {
        // Basic format validation
        if (!CURRENCY_CODE_PATTERN.matcher(currencyCode).matches()) {
            addConstraintViolation(context, "Currency code must be a 3-letter ISO code (e.g., USD, EUR)");
            return false;
        }
        
        // Check if currency is blocked
        if (isBlockedCurrency(currencyCode)) {
            addConstraintViolation(context, 
                String.format("Currency '%s' is not allowed in this context", currencyCode));
            return false;
        }
        
        // Check allowed currencies list
        if (allowedCurrencies.length > 0 && !isAllowedCurrency(currencyCode)) {
            addConstraintViolation(context, 
                String.format("Currency '%s' is not in the allowed currencies list", currencyCode));
            return false;
        }
        
        // Check crypto currency restrictions
        if (!allowCrypto && CurrencyValidator.isCryptoCurrency(currencyCode)) {
            addConstraintViolation(context, 
                String.format("Cryptocurrency '%s' is not allowed", currencyCode));
            return false;
        }
        
        // Check regional restrictions
        if (!isValidForRegion(currencyCode, context)) {
            return false;
        }
        
        // Check if currency is supported by Java Currency API
        if (!CurrencyValidator.isCryptoCurrency(currencyCode)) {
            try {
                Currency.getInstance(currencyCode);
            } catch (IllegalArgumentException e) {
                addConstraintViolation(context, 
                    String.format("Currency '%s' is not a valid ISO 4217 currency code", currencyCode));
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates currency amount format and range.
     * 
     * @param amount Amount to validate
     * @param context Validation context
     * @return true if amount is valid
     */
    private boolean isValidCurrencyAmount(String amount, ConstraintValidatorContext context) {
        try {
            BigDecimal amountValue = new BigDecimal(amount);
            
            // Check range
            if (amountValue.compareTo(minAmount) < 0) {
                addConstraintViolation(context, 
                    String.format("Amount must be at least %s", minAmount.toPlainString()));
                return false;
            }
            
            if (amountValue.compareTo(maxAmount) > 0) {
                addConstraintViolation(context, 
                    String.format("Amount must not exceed %s", maxAmount.toPlainString()));
                return false;
            }
            
            // Check decimal places
            int decimalPlaces = amountValue.scale();
            if (decimalPlaces > maxDecimalPlaces) {
                addConstraintViolation(context, 
                    String.format("Amount must not have more than %d decimal places", maxDecimalPlaces));
                return false;
            }
            
            // Check for negative values
            if (amountValue.compareTo(BigDecimal.ZERO) < 0) {
                addConstraintViolation(context, "Amount must be positive");
                return false;
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            addConstraintViolation(context, "Amount must be a valid number");
            return false;
        }
    }
    
    /**
     * Validates formatted currency amount (e.g., "100.50 USD").
     * 
     * @param formattedAmount Formatted amount to validate
     * @param context Validation context
     * @return true if formatted amount is valid
     */
    private boolean isValidFormattedAmount(String formattedAmount, ConstraintValidatorContext context) {
        String[] parts = formattedAmount.trim().split("\\s+");
        
        if (parts.length != 2) {
            addConstraintViolation(context, "Formatted amount must be in format 'amount currency' (e.g., '100.50 USD')");
            return false;
        }
        
        String amount = parts[0];
        String currency = parts[1];
        
        // Validate amount part
        if (!isValidCurrencyAmount(amount, context)) {
            return false;
        }
        
        // Validate currency part
        return isValidCurrencyCode(currency, context);
    }
    
    /**
     * Checks if currency is blocked.
     * 
     * @param currencyCode Currency code to check
     * @return true if currency is blocked
     */
    private boolean isBlockedCurrency(String currencyCode) {
        for (String blocked : blockedCurrencies) {
            if (blocked.equalsIgnoreCase(currencyCode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if currency is in allowed list.
     * 
     * @param currencyCode Currency code to check
     * @return true if currency is allowed
     */
    private boolean isAllowedCurrency(String currencyCode) {
        for (String allowed : allowedCurrencies) {
            if (allowed.equalsIgnoreCase(currencyCode)) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Validates currency for specific region.
     * 
     * @param currencyCode Currency code to validate
     * @param context Validation context
     * @return true if valid for region
     */
    private boolean isValidForRegion(String currencyCode, ConstraintValidatorContext context) {
        if ("ANY".equalsIgnoreCase(region) || region.isEmpty()) {
            return true;
        }
        
        Set<String> regionCurrencies;
        switch (region.toUpperCase()) {
            case "AFRICA":
                regionCurrencies = AFRICAN_CURRENCIES;
                break;
            case "EUROPE":
                regionCurrencies = EUROPEAN_CURRENCIES;
                break;
            case "ASIA":
                regionCurrencies = ASIAN_CURRENCIES;
                break;
            case "AMERICA":
                regionCurrencies = AMERICAN_CURRENCIES;
                break;
            case "SUPPORTED":
                regionCurrencies = SUPPORTED_CURRENCIES;
                break;
            default:
                return true; // Unknown region, allow all
        }
        
        if (!regionCurrencies.contains(currencyCode.toUpperCase())) {
            addConstraintViolation(context, 
                String.format("Currency '%s' is not supported in region '%s'", currencyCode, region));
            return false;
        }
        
        return true;
    }
    
    /**
     * Adds a constraint violation with custom message.
     * 
     * @param context Validation context
     * @param message Error message
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
    
    /**
     * Gets supported currencies for a region.
     * 
     * @param region Region name
     * @return Set of supported currency codes
     */
    public static Set<String> getSupportedCurrenciesForRegion(String region) {
        switch (region.toUpperCase()) {
            case "AFRICA":
                return AFRICAN_CURRENCIES;
            case "EUROPE":
                return EUROPEAN_CURRENCIES;
            case "ASIA":
                return ASIAN_CURRENCIES;
            case "AMERICA":
                return AMERICAN_CURRENCIES;
            case "SUPPORTED":
                return SUPPORTED_CURRENCIES;
            case "CRYPTO":
                return CRYPTO_CURRENCIES;
            default:
                return SUPPORTED_CURRENCIES;
        }
    }
    
    /**
     * Checks if a currency supports the specified number of decimal places.
     * 
     * @param currencyCode Currency code
     * @param decimalPlaces Number of decimal places
     * @return true if supported
     */
    public static boolean supportsDecimalPlaces(String currencyCode, int decimalPlaces) {
        // Special cases for currencies with different decimal place rules
        switch (currencyCode.toUpperCase()) {
            case "JPY": // Japanese Yen - no decimal places
            case "KRW": // South Korean Won - no decimal places
            case "VND": // Vietnamese Dong - no decimal places
                return decimalPlaces == 0;
            case "BTC": // Bitcoin - up to 8 decimal places
                return decimalPlaces <= 8;
            case "ETH": // Ethereum - up to 18 decimal places
                return decimalPlaces <= 18;
            default:
                // Most currencies support 2 decimal places
                return decimalPlaces <= 2;
        }
    }
    
    /**
     * Formats a currency amount according to currency rules.
     * 
     * @param amount Amount to format
     * @param currencyCode Currency code
     * @return Formatted amount string
     */
    public static String formatCurrencyAmount(BigDecimal amount, String currencyCode) {
        if (!supportsDecimalPlaces(currencyCode, amount.scale())) {
            // Adjust decimal places according to currency rules
            if (currencyCode.equals("JPY") || currencyCode.equals("KRW") || currencyCode.equals("VND")) {
                amount = amount.setScale(0, BigDecimal.ROUND_HALF_UP);
            } else {
                amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
        
        return amount.toPlainString() + " " + currencyCode;
    }
    
    /**
     * Gets currency symbol for display purposes.
     * 
     * @param currencyCode Currency code
     * @return Currency symbol or code if symbol not available
     */
    public static String getCurrencySymbol(String currencyCode) {
        try {
            if (!CurrencyValidator.isCryptoCurrency(currencyCode)) {
                return Currency.getInstance(currencyCode).getSymbol();
            }
        } catch (IllegalArgumentException e) {
            // Currency not found, return code
        }
        
        // Return crypto symbols or original code
        switch (currencyCode.toUpperCase()) {
            case "BTC": return "₿";
            case "ETH": return "Ξ";
            case "LTC": return "Ł";
            default: return currencyCode;
        }
    }
    
    /**
     * Checks if currency is cryptocurrency.
     * 
     * @param currencyCode Currency code to check
     * @return true if cryptocurrency
     */
    public static boolean isCryptoCurrency(String currencyCode) {
        return CRYPTO_CURRENCIES.contains(currencyCode.toUpperCase());
    }
}