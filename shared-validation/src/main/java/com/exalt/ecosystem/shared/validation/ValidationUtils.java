package com.exalt.ecosystem.shared.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility class for common validation operations.
 * Provides methods for data validation, format checking, and validation result processing.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Component
public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,30}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$"
    );
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );
    
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]+$"
    );
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
        "^\\d+$"
    );
    
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{3,10}$"
    );
    
    /**
     * Validates an email address format.
     * 
     * @param email Email to validate
     * @return true if email format is valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates a phone number format.
     * 
     * @param phone Phone number to validate
     * @return true if phone format is valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * Validates a username format.
     * 
     * @param username Username to validate
     * @return true if username format is valid
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validates a password strength.
     * 
     * @param password Password to validate
     * @return true if password meets strength requirements
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validates a URL format.
     * 
     * @param url URL to validate
     * @return true if URL format is valid
     */
    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url.trim()).matches();
    }
    
    /**
     * Validates if string contains only alphanumeric characters.
     * 
     * @param input String to validate
     * @return true if alphanumeric only
     */
    public static boolean isAlphanumeric(String input) {
        return input != null && ALPHANUMERIC_PATTERN.matcher(input).matches();
    }
    
    /**
     * Validates if string contains only numeric characters.
     * 
     * @param input String to validate
     * @return true if numeric only
     */
    public static boolean isNumeric(String input) {
        return input != null && NUMERIC_PATTERN.matcher(input).matches();
    }
    
    /**
     * Validates a postal code format.
     * 
     * @param postalCode Postal code to validate
     * @return true if postal code format is valid
     */
    public static boolean isValidPostalCode(String postalCode) {
        return postalCode != null && POSTAL_CODE_PATTERN.matcher(postalCode.toUpperCase()).matches();
    }
    
    /**
     * Validates if string is not null or empty.
     * 
     * @param input String to validate
     * @return true if not null or empty
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
    /**
     * Validates string length is within range.
     * 
     * @param input String to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if length is within range
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) return false;
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validates if number is within range.
     * 
     * @param value Number to validate
     * @param min Minimum value
     * @param max Maximum value
     * @return true if within range
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) return false;
        double val = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();
        return val >= minVal && val <= maxVal;
    }
    
    /**
     * Validates if value is positive.
     * 
     * @param value Number to validate
     * @return true if positive
     */
    public static boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0;
    }
    
    /**
     * Validates if value is non-negative.
     * 
     * @param value Number to validate
     * @return true if non-negative
     */
    public static boolean isNonNegative(Number value) {
        return value != null && value.doubleValue() >= 0;
    }
    
    /**
     * Validates UUID format.
     * 
     * @param uuid UUID string to validate
     * @return true if valid UUID format
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Validates if collection is not null or empty.
     * 
     * @param collection Collection to validate
     * @return true if not null or empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    /**
     * Validates if array is not null or empty.
     * 
     * @param array Array to validate
     * @return true if not null or empty
     */
    public static boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }
    
    /**
     * Validates if map is not null or empty.
     * 
     * @param map Map to validate
     * @return true if not null or empty
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }
    
    /**
     * Sanitizes input string by removing potentially harmful characters.
     * 
     * @param input Input string
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    /**
     * Validates object using Bean Validation.
     * 
     * @param object Object to validate
     * @param validator Validator instance
     * @param groups Validation groups
     * @return ValidationResult with errors
     */
    public static ValidationResult validate(Object object, Validator validator, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object, groups);
        
        ValidationResult result = new ValidationResult();
        for (ConstraintViolation<Object> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            result.addError(field, message);
        }
        
        return result;
    }
    
    /**
     * Validates specific property of an object.
     * 
     * @param object Object to validate
     * @param propertyName Property name
     * @param validator Validator instance
     * @param groups Validation groups
     * @return ValidationResult with errors
     */
    public static ValidationResult validateProperty(Object object, String propertyName, 
                                                   Validator validator, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = 
            validator.validateProperty(object, propertyName, groups);
        
        ValidationResult result = new ValidationResult();
        for (ConstraintViolation<Object> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            result.addError(field, message);
        }
        
        return result;
    }
    
    /**
     * Checks if all required fields are present.
     * 
     * @param fields Map of field names to values
     * @param requiredFields Required field names
     * @return ValidationResult with missing field errors
     */
    public static ValidationResult validateRequiredFields(Map<String, Object> fields, String... requiredFields) {
        ValidationResult result = new ValidationResult();
        
        for (String field : requiredFields) {
            Object value = fields.get(field);
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                result.addError(field, "Field is required");
            }
        }
        
        return result;
    }
    
    /**
     * Gets password strength score (0-100).
     * 
     * @param password Password to analyze
     * @return Strength score
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length scoring
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        
        // Character variety scoring
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[A-Z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 10;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score += 20;
        
        return Math.min(score, 100);
    }
    
    /**
     * Validates credit card number using Luhn algorithm.
     * 
     * @param cardNumber Credit card number
     * @return true if valid
     */
    public static boolean isValidCreditCard(String cardNumber) {
        if (cardNumber == null) return false;
        
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        if (!isNumeric(cleaned) || cleaned.length() < 13 || cleaned.length() > 19) {
            return false;
        }
        
        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cleaned.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleaned.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    /**
     * Validates if date string is in valid format.
     * 
     * @param date Date string
     * @param format Expected format pattern
     * @return true if valid format
     */
    public static boolean isValidDateFormat(String date, String format) {
        if (date == null || format == null) return false;
        
        try {
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern(format);
            java.time.LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Converts validation violations to error map.
     * 
     * @param violations Set of constraint violations
     * @return Map of field names to error messages
     */
    public static Map<String, List<String>> violationsToErrorMap(Set<ConstraintViolation<?>> violations) {
        Map<String, List<String>> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        }
        
        return errors;
    }
}