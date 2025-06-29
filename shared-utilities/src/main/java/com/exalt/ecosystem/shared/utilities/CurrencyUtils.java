package com.exalt.ecosystem.shared.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Comprehensive currency utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced currency operations, formatting, conversion, and validation methods.
 * 
 * <p>This utility class handles currency operations across the ecosystem including:
 * currency formatting, conversion, validation, and calculations with proper precision.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class CurrencyUtils {
    
    // Major currencies with their details
    private static final Map<String, CurrencyInfo> CURRENCY_INFO = new HashMap<>();
    
    // Common currency codes
    public static final String USD = "USD";
    public static final String EUR = "EUR";
    public static final String GBP = "GBP";
    public static final String JPY = "JPY";
    public static final String CHF = "CHF";
    public static final String CAD = "CAD";
    public static final String AUD = "AUD";
    public static final String CNY = "CNY";
    public static final String INR = "INR";
    public static final String AED = "AED";
    public static final String SAR = "SAR";
    public static final String QAR = "QAR";
    public static final String KWD = "KWD";
    public static final String BHD = "BHD";
    public static final String OMR = "OMR";
    
    // Default scale for monetary calculations
    private static final int DEFAULT_SCALE = 4;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    
    static {
        initializeCurrencyInfo();
    }
    
    private CurrencyUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Currency information holder.
     */
    public static class CurrencyInfo {
        private final String code;
        private final String name;
        private final String symbol;
        private final int decimalPlaces;
        private final boolean symbolPrefix;
        
        public CurrencyInfo(String code, String name, String symbol, int decimalPlaces, boolean symbolPrefix) {
            this.code = code;
            this.name = name;
            this.symbol = symbol;
            this.decimalPlaces = decimalPlaces;
            this.symbolPrefix = symbolPrefix;
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public int getDecimalPlaces() { return decimalPlaces; }
        public boolean isSymbolPrefix() { return symbolPrefix; }
    }
    
    /**
     * Initializes currency information.
     */
    private static void initializeCurrencyInfo() {
        CURRENCY_INFO.put(USD, new CurrencyInfo(USD, "US Dollar", "$", 2, true));
        CURRENCY_INFO.put(EUR, new CurrencyInfo(EUR, "Euro", "€", 2, true));
        CURRENCY_INFO.put(GBP, new CurrencyInfo(GBP, "British Pound", "£", 2, true));
        CURRENCY_INFO.put(JPY, new CurrencyInfo(JPY, "Japanese Yen", "¥", 0, true));
        CURRENCY_INFO.put(CHF, new CurrencyInfo(CHF, "Swiss Franc", "CHF", 2, false));
        CURRENCY_INFO.put(CAD, new CurrencyInfo(CAD, "Canadian Dollar", "C$", 2, true));
        CURRENCY_INFO.put(AUD, new CurrencyInfo(AUD, "Australian Dollar", "A$", 2, true));
        CURRENCY_INFO.put(CNY, new CurrencyInfo(CNY, "Chinese Yuan", "¥", 2, true));
        CURRENCY_INFO.put(INR, new CurrencyInfo(INR, "Indian Rupee", "₹", 2, true));
        CURRENCY_INFO.put(AED, new CurrencyInfo(AED, "UAE Dirham", "د.إ", 2, false));
        CURRENCY_INFO.put(SAR, new CurrencyInfo(SAR, "Saudi Riyal", "ر.س", 2, false));
        CURRENCY_INFO.put(QAR, new CurrencyInfo(QAR, "Qatari Riyal", "ر.ق", 2, false));
        CURRENCY_INFO.put(KWD, new CurrencyInfo(KWD, "Kuwaiti Dinar", "د.ك", 3, false));
        CURRENCY_INFO.put(BHD, new CurrencyInfo(BHD, "Bahraini Dinar", "د.ب", 3, false));
        CURRENCY_INFO.put(OMR, new CurrencyInfo(OMR, "Omani Rial", "ر.ع", 3, false));
    }
    
    /**
     * Validates if a currency code is supported.
     * 
     * @param currencyCode currency code to validate
     * @return true if currency is supported
     */
    public static boolean isValidCurrency(String currencyCode) {
        if (StringUtils.isEmpty(currencyCode)) return false;
        return CURRENCY_INFO.containsKey(currencyCode.toUpperCase());
    }
    
    /**
     * Gets currency information for a currency code.
     * 
     * @param currencyCode currency code
     * @return currency information or null if not found
     */
    public static CurrencyInfo getCurrencyInfo(String currencyCode) {
        if (StringUtils.isEmpty(currencyCode)) return null;
        return CURRENCY_INFO.get(currencyCode.toUpperCase());
    }
    
    /**
     * Gets all supported currency codes.
     * 
     * @return set of supported currency codes
     */
    public static Set<String> getSupportedCurrencies() {
        return new HashSet<>(CURRENCY_INFO.keySet());
    }
    
    /**
     * Formats an amount with the specified currency.
     * 
     * @param amount amount to format
     * @param currencyCode currency code
     * @return formatted currency string
     */
    public static String formatCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null || StringUtils.isEmpty(currencyCode)) return "";
        
        CurrencyInfo info = getCurrencyInfo(currencyCode);
        if (info == null) return amount.toString() + " " + currencyCode;
        
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(info.getDecimalPlaces());
        formatter.setMinimumFractionDigits(info.getDecimalPlaces());
        formatter.setGroupingUsed(true);
        formatter.setRoundingMode(DEFAULT_ROUNDING);
        
        String formattedAmount = formatter.format(amount);
        
        if (info.isSymbolPrefix()) {
            return info.getSymbol() + formattedAmount;
        } else {
            return formattedAmount + " " + info.getSymbol();
        }
    }
    
    /**
     * Formats an amount with the specified currency (double version).
     * 
     * @param amount amount to format
     * @param currencyCode currency code
     * @return formatted currency string
     */
    public static String formatCurrency(double amount, String currencyCode) {
        return formatCurrency(BigDecimal.valueOf(amount), currencyCode);
    }
    
    /**
     * Formats an amount for display with default locale formatting.
     * 
     * @param amount amount to format
     * @param currencyCode currency code
     * @param locale locale for formatting
     * @return formatted currency string
     */
    public static String formatCurrencyLocale(BigDecimal amount, String currencyCode, Locale locale) {
        if (amount == null || StringUtils.isEmpty(currencyCode)) return "";
        
        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
            formatter.setCurrency(currency);
            return formatter.format(amount);
        } catch (Exception e) {
            return formatCurrency(amount, currencyCode);
        }
    }
    
    /**
     * Parses a currency string to extract the numeric amount.
     * 
     * @param currencyString currency string to parse
     * @return parsed amount or null if parsing fails
     */
    public static BigDecimal parseCurrency(String currencyString) {
        if (StringUtils.isEmpty(currencyString)) return null;
        
        // Remove common currency symbols and clean the string
        String cleaned = currencyString
                .replaceAll("[^0-9.,+-]", "")
                .trim();
        
        if (StringUtils.isEmpty(cleaned)) return null;
        
        try {
            // Handle different decimal separators
            if (cleaned.contains(",") && cleaned.contains(".")) {
                // European format (1.000,50) or US format (1,000.50)
                int lastComma = cleaned.lastIndexOf(',');
                int lastDot = cleaned.lastIndexOf('.');
                
                if (lastDot > lastComma) {
                    // US format: remove commas
                    cleaned = cleaned.replace(",", "");
                } else {
                    // European format: swap comma and dot
                    cleaned = cleaned.substring(0, lastComma).replace(".", "") + 
                             "." + cleaned.substring(lastComma + 1);
                }
            } else if (cleaned.contains(",")) {
                // Check if comma is decimal separator or thousands separator
                int commaPos = cleaned.lastIndexOf(',');
                String afterComma = cleaned.substring(commaPos + 1);
                
                if (afterComma.length() <= 2) {
                    // Likely decimal separator
                    cleaned = cleaned.replace(",", ".");
                } else {
                    // Likely thousands separator
                    cleaned = cleaned.replace(",", "");
                }
            }
            
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Adds two currency amounts with proper precision.
     * 
     * @param amount1 first amount
     * @param amount2 second amount
     * @return sum of amounts
     */
    public static BigDecimal add(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null && amount2 == null) return BigDecimal.ZERO;
        if (amount1 == null) return amount2;
        if (amount2 == null) return amount1;
        
        return amount1.add(amount2).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }
    
    /**
     * Subtracts two currency amounts with proper precision.
     * 
     * @param amount1 first amount
     * @param amount2 second amount
     * @return difference of amounts
     */
    public static BigDecimal subtract(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) amount1 = BigDecimal.ZERO;
        if (amount2 == null) amount2 = BigDecimal.ZERO;
        
        return amount1.subtract(amount2).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }
    
    /**
     * Multiplies two currency amounts with proper precision.
     * 
     * @param amount amount to multiply
     * @param multiplier multiplier
     * @return product of amounts
     */
    public static BigDecimal multiply(BigDecimal amount, BigDecimal multiplier) {
        if (amount == null || multiplier == null) return BigDecimal.ZERO;
        
        return amount.multiply(multiplier).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }
    
    /**
     * Multiplies a currency amount by a double with proper precision.
     * 
     * @param amount amount to multiply
     * @param multiplier multiplier
     * @return product
     */
    public static BigDecimal multiply(BigDecimal amount, double multiplier) {
        return multiply(amount, BigDecimal.valueOf(multiplier));
    }
    
    /**
     * Divides two currency amounts with proper precision.
     * 
     * @param amount dividend
     * @param divisor divisor
     * @return quotient
     */
    public static BigDecimal divide(BigDecimal amount, BigDecimal divisor) {
        if (amount == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING);
    }
    
    /**
     * Divides a currency amount by a double with proper precision.
     * 
     * @param amount dividend
     * @param divisor divisor
     * @return quotient
     */
    public static BigDecimal divide(BigDecimal amount, double divisor) {
        return divide(amount, BigDecimal.valueOf(divisor));
    }
    
    /**
     * Calculates percentage of an amount.
     * 
     * @param amount base amount
     * @param percentage percentage (e.g., 15 for 15%)
     * @return percentage amount
     */
    public static BigDecimal percentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) return BigDecimal.ZERO;
        
        return multiply(amount, divide(percentage, BigDecimal.valueOf(100)));
    }
    
    /**
     * Calculates percentage of an amount (double version).
     * 
     * @param amount base amount
     * @param percentage percentage (e.g., 15.5 for 15.5%)
     * @return percentage amount
     */
    public static BigDecimal percentage(BigDecimal amount, double percentage) {
        return percentage(amount, BigDecimal.valueOf(percentage));
    }
    
    /**
     * Adds percentage to an amount (e.g., adding tax).
     * 
     * @param amount base amount
     * @param percentage percentage to add
     * @return amount plus percentage
     */
    public static BigDecimal addPercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null) return BigDecimal.ZERO;
        
        BigDecimal percentageAmount = percentage(amount, percentage);
        return add(amount, percentageAmount);
    }
    
    /**
     * Adds percentage to an amount (double version).
     * 
     * @param amount base amount
     * @param percentage percentage to add
     * @return amount plus percentage
     */
    public static BigDecimal addPercentage(BigDecimal amount, double percentage) {
        return addPercentage(amount, BigDecimal.valueOf(percentage));
    }
    
    /**
     * Subtracts percentage from an amount (e.g., applying discount).
     * 
     * @param amount base amount
     * @param percentage percentage to subtract
     * @return amount minus percentage
     */
    public static BigDecimal subtractPercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null) return BigDecimal.ZERO;
        
        BigDecimal percentageAmount = percentage(amount, percentage);
        return subtract(amount, percentageAmount);
    }
    
    /**
     * Subtracts percentage from an amount (double version).
     * 
     * @param amount base amount
     * @param percentage percentage to subtract
     * @return amount minus percentage
     */
    public static BigDecimal subtractPercentage(BigDecimal amount, double percentage) {
        return subtractPercentage(amount, BigDecimal.valueOf(percentage));
    }
    
    /**
     * Rounds an amount to the appropriate decimal places for a currency.
     * 
     * @param amount amount to round
     * @param currencyCode currency code
     * @return rounded amount
     */
    public static BigDecimal roundToCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) return BigDecimal.ZERO;
        
        CurrencyInfo info = getCurrencyInfo(currencyCode);
        int scale = info != null ? info.getDecimalPlaces() : 2;
        
        return amount.setScale(scale, DEFAULT_ROUNDING);
    }
    
    /**
     * Converts an amount between currencies using exchange rate.
     * 
     * @param amount amount to convert
     * @param fromCurrency source currency
     * @param toCurrency target currency
     * @param exchangeRate exchange rate from source to target
     * @return converted amount
     */
    public static BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, 
                                           String toCurrency, BigDecimal exchangeRate) {
        if (amount == null || exchangeRate == null) return BigDecimal.ZERO;
        if (!isValidCurrency(fromCurrency) || !isValidCurrency(toCurrency)) return amount;
        
        BigDecimal converted = multiply(amount, exchangeRate);
        return roundToCurrency(converted, toCurrency);
    }
    
    /**
     * Compares two currency amounts.
     * 
     * @param amount1 first amount
     * @param amount2 second amount
     * @return -1 if amount1 < amount2, 0 if equal, 1 if amount1 > amount2
     */
    public static int compare(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) amount1 = BigDecimal.ZERO;
        if (amount2 == null) amount2 = BigDecimal.ZERO;
        
        return amount1.compareTo(amount2);
    }
    
    /**
     * Checks if an amount is zero.
     * 
     * @param amount amount to check
     * @return true if amount is zero or null
     */
    public static boolean isZero(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Checks if an amount is positive.
     * 
     * @param amount amount to check
     * @return true if amount is positive
     */
    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Checks if an amount is negative.
     * 
     * @param amount amount to check
     * @return true if amount is negative
     */
    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Returns the absolute value of an amount.
     * 
     * @param amount amount to process
     * @return absolute value
     */
    public static BigDecimal abs(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.abs();
    }
    
    /**
     * Returns the maximum of two amounts.
     * 
     * @param amount1 first amount
     * @param amount2 second amount
     * @return maximum amount
     */
    public static BigDecimal max(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) return amount2;
        if (amount2 == null) return amount1;
        return amount1.max(amount2);
    }
    
    /**
     * Returns the minimum of two amounts.
     * 
     * @param amount1 first amount
     * @param amount2 second amount
     * @return minimum amount
     */
    public static BigDecimal min(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null) return amount2;
        if (amount2 == null) return amount1;
        return amount1.min(amount2);
    }
    
    /**
     * Calculates the sum of a list of amounts.
     * 
     * @param amounts list of amounts
     * @return sum of all amounts
     */
    public static BigDecimal sum(List<BigDecimal> amounts) {
        if (CollectionUtils.isEmpty(amounts)) return BigDecimal.ZERO;
        
        return amounts.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, CurrencyUtils::add);
    }
    
    /**
     * Calculates the average of a list of amounts.
     * 
     * @param amounts list of amounts
     * @return average amount
     */
    public static BigDecimal average(List<BigDecimal> amounts) {
        if (CollectionUtils.isEmpty(amounts)) return BigDecimal.ZERO;
        
        List<BigDecimal> nonNullAmounts = amounts.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
        
        if (nonNullAmounts.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal sum = sum(nonNullAmounts);
        return divide(sum, nonNullAmounts.size());
    }
    
    /**
     * Validates if an amount is within acceptable range for a currency.
     * 
     * @param amount amount to validate
     * @param currencyCode currency code
     * @return true if amount is valid
     */
    public static boolean isValidAmount(BigDecimal amount, String currencyCode) {
        if (amount == null || !isValidCurrency(currencyCode)) return false;
        
        // Check for reasonable limits
        BigDecimal minAmount = new BigDecimal("0.01");
        BigDecimal maxAmount = new BigDecimal("999999999.99");
        
        return amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) <= 0;
    }
    
    /**
     * Formats an amount as words (for checks, invoices).
     * 
     * @param amount amount to convert
     * @param currencyCode currency code
     * @return amount in words
     */
    public static String toWords(BigDecimal amount, String currencyCode) {
        if (amount == null || !isValidCurrency(currencyCode)) return "";
        
        // This is a simplified implementation
        // A full implementation would handle proper number-to-words conversion
        CurrencyInfo info = getCurrencyInfo(currencyCode);
        String currencyName = info != null ? info.getName() : currencyCode;
        
        return amount + " " + currencyName;
    }
    
    /**
     * Creates a BigDecimal from string with proper currency precision.
     * 
     * @param value string value
     * @return BigDecimal with proper scale
     */
    public static BigDecimal createAmount(String value) {
        BigDecimal amount = parseCurrency(value);
        return amount != null ? amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING) : BigDecimal.ZERO;
    }
    
    /**
     * Creates a BigDecimal from double with proper currency precision.
     * 
     * @param value double value
     * @return BigDecimal with proper scale
     */
    public static BigDecimal createAmount(double value) {
        return BigDecimal.valueOf(value).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }
}