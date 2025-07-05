package com.gogidix.ecosystem.shared.validation.constraints;

import com.gogidix.ecosystem.shared.validation.validators.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint annotation for phone number validation.
 * Validates phone numbers according to international standards and region-specific formats.
 * 
 * <p>Usage examples:
 * <pre>
 * {@code @ValidPhone}
 * private String phoneNumber;
 * 
 * {@code @ValidPhone(region = "US", mobileOnly = true)}
 * private String mobilePhone;
 * 
 * {@code @ValidPhone(allowEmpty = true, region = "INTERNATIONAL")}
 * private String optionalPhone;
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidPhone.List.class)
public @interface ValidPhone {
    
    /**
     * Default validation error message.
     * 
     * @return Error message
     */
    String message() default "Phone number format is invalid";
    
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
     * Region for phone number validation.
     * Supported regions: US, UK, INTERNATIONAL, ANY
     * 
     * @return Region code
     */
    String region() default "ANY";
    
    /**
     * Whether to only allow mobile numbers.
     * 
     * @return true if only mobile numbers are allowed
     */
    boolean mobileOnly() default false;
    
    /**
     * Custom error message for invalid format.
     * 
     * @return Custom error message
     */
    String formatMessage() default "Phone number format is invalid for the specified region";
    
    /**
     * Custom error message for non-mobile numbers when mobileOnly is true.
     * 
     * @return Custom error message for mobile-only validation
     */
    String mobileOnlyMessage() default "Only mobile phone numbers are allowed";
    
    /**
     * Custom error message for region-specific validation.
     * 
     * @return Custom error message for region validation
     */
    String regionMessage() default "Phone number is not valid for the specified region";
    
    /**
     * Defines several {@code @ValidPhone} annotations on the same element.
     */
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidPhone[] value();
    }
}