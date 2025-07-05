package com.gogidix.ecosystem.shared.validation.validators;

import com.gogidix.ecosystem.shared.validation.constraints.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Email validator for custom validation annotation.
 * Validates email format according to RFC standards.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private boolean allowEmpty;
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return allowEmpty;
        }
        
        String trimmedEmail = email.trim();
        
        // Check length constraints
        if (trimmedEmail.length() > 254) {
            return false;
        }
        
        // Check basic format
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return false;
        }
        
        // Additional validations
        return isValidEmailStructure(trimmedEmail);
    }
    
    /**
     * Validates detailed email structure.
     * 
     * @param email Email to validate
     * @return true if structure is valid
     */
    private boolean isValidEmailStructure(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // Validate local part (before @)
        if (!isValidLocalPart(localPart)) {
            return false;
        }
        
        // Validate domain part (after @)
        return isValidDomainPart(domainPart);
    }
    
    /**
     * Validates the local part of email (before @).
     * 
     * @param localPart Local part to validate
     * @return true if valid
     */
    private boolean isValidLocalPart(String localPart) {
        if (localPart.length() > 64) {
            return false;
        }
        
        // Cannot start or end with dot
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }
        
        // Cannot have consecutive dots
        if (localPart.contains("..")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates the domain part of email (after @).
     * 
     * @param domainPart Domain part to validate
     * @return true if valid
     */
    private boolean isValidDomainPart(String domainPart) {
        if (domainPart.length() > 253) {
            return false;
        }
        
        // Must contain at least one dot
        if (!domainPart.contains(".")) {
            return false;
        }
        
        // Cannot start or end with dot or hyphen
        if (domainPart.startsWith(".") || domainPart.endsWith(".") ||
            domainPart.startsWith("-") || domainPart.endsWith("-")) {
            return false;
        }
        
        // Check each domain label
        String[] labels = domainPart.split("\\.");
        for (String label : labels) {
            if (!isValidDomainLabel(label)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates a domain label.
     * 
     * @param label Domain label to validate
     * @return true if valid
     */
    private boolean isValidDomainLabel(String label) {
        if (label.length() == 0 || label.length() > 63) {
            return false;
        }
        
        // Cannot start or end with hyphen
        if (label.startsWith("-") || label.endsWith("-")) {
            return false;
        }
        
        // Must contain only valid characters
        return label.matches("^[a-zA-Z0-9-]+$");
    }
}