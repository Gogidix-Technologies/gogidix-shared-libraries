package com.gogidix.ecosystem.shared.validation.validators;

import com.gogidix.ecosystem.shared.validation.constraints.ValidPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Phone number validator for custom validation annotation.
 * Validates phone numbers according to international standards.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private static final Pattern US_PHONE_PATTERN = Pattern.compile(
        "^\\+?1?[2-9]\\d{2}[2-9]\\d{2}\\d{4}$"
    );
    
    private static final Pattern UK_PHONE_PATTERN = Pattern.compile(
        "^\\+?44[1-9]\\d{8,9}$"
    );
    
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{10,14}$"
    );
    
    private boolean allowEmpty;
    private String region;
    private boolean mobileOnly;
    
    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.region = constraintAnnotation.region();
        this.mobileOnly = constraintAnnotation.mobileOnly();
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return allowEmpty;
        }
        
        String cleanPhone = cleanPhoneNumber(phone);
        
        // Basic format validation
        if (!isValidFormat(cleanPhone)) {
            return false;
        }
        
        // Region-specific validation
        if (!isValidForRegion(cleanPhone)) {
            return false;
        }
        
        // Mobile-only validation
        if (mobileOnly && !isMobileNumber(cleanPhone)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Cleans phone number by removing formatting characters.
     * 
     * @param phone Raw phone number
     * @return Cleaned phone number
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        
        // Remove all non-digit characters except + at the beginning
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]+", "");
        
        // Ensure + is only at the beginning
        if (cleaned.contains("+")) {
            if (cleaned.startsWith("+")) {
                cleaned = "+" + cleaned.substring(1).replaceAll("\\+", "");
            } else {
                cleaned = cleaned.replaceAll("\\+", "");
            }
        }
        
        return cleaned;
    }
    
    /**
     * Validates basic phone number format.
     * 
     * @param phone Cleaned phone number
     * @return true if basic format is valid
     */
    private boolean isValidFormat(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        // Check for international format
        if (phone.startsWith("+")) {
            return INTERNATIONAL_PHONE_PATTERN.matcher(phone).matches();
        }
        
        // Check for domestic format (digits only)
        return phone.matches("^\\d{7,15}$");
    }
    
    /**
     * Validates phone number for specific region.
     * 
     * @param phone Cleaned phone number
     * @return true if valid for region
     */
    private boolean isValidForRegion(String phone) {
        if ("ANY".equalsIgnoreCase(region) || region.isEmpty()) {
            return true;
        }
        
        switch (region.toUpperCase()) {
            case "US":
            case "USA":
                return isValidUSPhone(phone);
            case "UK":
            case "GB":
                return isValidUKPhone(phone);
            case "INTERNATIONAL":
                return phone.startsWith("+") && INTERNATIONAL_PHONE_PATTERN.matcher(phone).matches();
            default:
                return true; // Default to allow if region not specifically handled
        }
    }
    
    /**
     * Validates US phone number format.
     * 
     * @param phone Cleaned phone number
     * @return true if valid US format
     */
    private boolean isValidUSPhone(String phone) {
        // Remove + and country code if present
        String usPhone = phone;
        if (usPhone.startsWith("+1")) {
            usPhone = usPhone.substring(2);
        } else if (usPhone.startsWith("1") && usPhone.length() == 11) {
            usPhone = usPhone.substring(1);
        }
        
        return usPhone.length() == 10 && US_PHONE_PATTERN.matcher("1" + usPhone).matches();
    }
    
    /**
     * Validates UK phone number format.
     * 
     * @param phone Cleaned phone number
     * @return true if valid UK format
     */
    private boolean isValidUKPhone(String phone) {
        // Handle UK format with or without country code
        if (phone.startsWith("+44")) {
            return UK_PHONE_PATTERN.matcher(phone).matches();
        } else if (phone.startsWith("44")) {
            return UK_PHONE_PATTERN.matcher("+" + phone).matches();
        } else if (phone.startsWith("0")) {
            // UK domestic format starting with 0
            return phone.length() >= 10 && phone.length() <= 11;
        }
        
        return false;
    }
    
    /**
     * Checks if number appears to be a mobile number.
     * 
     * @param phone Cleaned phone number
     * @return true if appears to be mobile
     */
    private boolean isMobileNumber(String phone) {
        if (phone == null || phone.length() < 10) {
            return false;
        }
        
        // International mobile patterns
        if (phone.startsWith("+")) {
            return MOBILE_PATTERN.matcher(phone).matches();
        }
        
        // US mobile patterns (all US numbers are potentially mobile)
        if (phone.length() == 10 || (phone.length() == 11 && phone.startsWith("1"))) {
            return true;
        }
        
        // UK mobile patterns (starts with 07)
        if (phone.startsWith("07") && phone.length() == 11) {
            return true;
        }
        
        // Default assumption for international numbers
        return phone.length() >= 10 && phone.length() <= 15;
    }
    
    /**
     * Gets the carrier type of a phone number (if detectable).
     * 
     * @param phone Cleaned phone number
     * @return Estimated carrier type
     */
    public String getCarrierType(String phone) {
        if (phone == null) {
            return "UNKNOWN";
        }
        
        String cleaned = cleanPhoneNumber(phone);
        
        if (isMobileNumber(cleaned)) {
            return "MOBILE";
        }
        
        // Basic landline detection for known patterns
        if (cleaned.startsWith("+1") || (cleaned.length() == 10)) {
            return "FIXED_LINE_OR_MOBILE"; // US numbers are ambiguous
        }
        
        if (cleaned.startsWith("+44") && !cleaned.substring(3).startsWith("7")) {
            return "FIXED_LINE";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Formats phone number for display.
     * 
     * @param phone Raw phone number
     * @param format Desired format (INTERNATIONAL, NATIONAL, E164)
     * @return Formatted phone number
     */
    public String formatPhoneNumber(String phone, String format) {
        if (phone == null) {
            return null;
        }
        
        String cleaned = cleanPhoneNumber(phone);
        if (!isValidFormat(cleaned)) {
            return phone; // Return original if invalid
        }
        
        switch (format.toUpperCase()) {
            case "E164":
                return cleaned.startsWith("+") ? cleaned : "+" + cleaned;
            case "INTERNATIONAL":
                return formatInternational(cleaned);
            case "NATIONAL":
                return formatNational(cleaned);
            default:
                return cleaned;
        }
    }
    
    /**
     * Formats phone number in international format.
     * 
     * @param phone Cleaned phone number
     * @return Internationally formatted phone number
     */
    private String formatInternational(String phone) {
        if (phone.startsWith("+")) {
            return phone;
        }
        
        // Assume US number if no country code
        if (phone.length() == 10) {
            return "+1 " + phone.substring(0, 3) + " " + 
                   phone.substring(3, 6) + " " + phone.substring(6);
        }
        
        return "+" + phone;
    }
    
    /**
     * Formats phone number in national format.
     * 
     * @param phone Cleaned phone number
     * @return Nationally formatted phone number
     */
    private String formatNational(String phone) {
        // Remove country code for national format
        String national = phone;
        if (phone.startsWith("+1")) {
            national = phone.substring(2);
        } else if (phone.startsWith("+44")) {
            national = "0" + phone.substring(3);
        }
        
        // Format US numbers
        if (national.length() == 10) {
            return "(" + national.substring(0, 3) + ") " + 
                   national.substring(3, 6) + "-" + national.substring(6);
        }
        
        return national;
    }
}