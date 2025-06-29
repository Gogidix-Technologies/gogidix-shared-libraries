package com.exalt.ecosystem.shared.validation.constraints;

import com.exalt.ecosystem.shared.validation.validators.EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint annotation for email address validation.
 * Validates email addresses according to RFC standards with comprehensive format checking.
 * 
 * <p>Usage examples:
 * <pre>
 * {@code @ValidEmail}
 * private String email;
 * 
 * {@code @ValidEmail(allowEmpty = true)}
 * private String optionalEmail;
 * 
 * {@code @ValidEmail(message = "Please provide a valid business email address")}
 * private String businessEmail;
 * </pre>
 * 
 * <p>This validator performs comprehensive email validation including:
 * <ul>
 * <li>RFC-compliant email format validation</li>
 * <li>Local part validation (before @)</li>
 * <li>Domain part validation (after @)</li>
 * <li>Length constraints (overall: 254 chars, local: 64 chars, domain: 253 chars)</li>
 * <li>Domain label validation (max 63 chars per label)</li>
 * <li>Prevention of consecutive dots</li>
 * <li>Proper handling of special characters</li>
 * </ul>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidEmail.List.class)
public @interface ValidEmail {
    
    /**
     * Default validation error message.
     * 
     * @return Error message
     */
    String message() default "Email address format is invalid";
    
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
     * When true, null or empty strings will pass validation.
     * When false (default), null or empty strings will fail validation.
     * 
     * @return true if empty values are allowed
     */
    boolean allowEmpty() default false;
    
    /**
     * Custom error message for empty values when allowEmpty is false.
     * 
     * @return Custom error message for empty validation
     */
    String emptyMessage() default "Email address is required";
    
    /**
     * Custom error message for format validation failures.
     * 
     * @return Custom error message for format validation
     */
    String formatMessage() default "Email address format is invalid";
    
    /**
     * Custom error message for length validation failures.
     * 
     * @return Custom error message for length validation
     */
    String lengthMessage() default "Email address is too long";
    
    /**
     * Custom error message for local part validation failures.
     * 
     * @return Custom error message for local part validation
     */
    String localPartMessage() default "Email address local part (before @) is invalid";
    
    /**
     * Custom error message for domain part validation failures.
     * 
     * @return Custom error message for domain part validation
     */
    String domainPartMessage() default "Email address domain part (after @) is invalid";
    
    /**
     * Defines several {@code @ValidEmail} annotations on the same element.
     */
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidEmail[] value();
    }
}