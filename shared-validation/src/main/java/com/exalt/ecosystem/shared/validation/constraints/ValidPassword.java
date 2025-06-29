package com.exalt.ecosystem.shared.validation.constraints;

import com.exalt.ecosystem.shared.validation.validators.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint annotation for password validation.
 * Validates password strength according to configurable security requirements.
 * 
 * <p>Usage examples:
 * <pre>
 * {@code @ValidPassword}
 * private String password;
 * 
 * {@code @ValidPassword(minLength = 12, requireSpecialChars = true, minStrengthScore = 80)}
 * private String strongPassword;
 * 
 * {@code @ValidPassword(allowEmpty = true, minStrengthScore = 40)}
 * private String optionalPassword;
 * 
 * {@code @ValidPassword(rejectCommonPasswords = true, allowWhitespace = false)}
 * private String securePassword;
 * </pre>
 * 
 * <p>This validator performs comprehensive password validation including:
 * <ul>
 * <li>Length requirements (configurable min/max)</li>
 * <li>Character type requirements (uppercase, lowercase, digits, special chars)</li>
 * <li>Common password detection and rejection</li>
 * <li>Password strength scoring (0-100)</li>
 * <li>Whitespace validation</li>
 * <li>Pattern detection (sequential, repeating characters)</li>
 * <li>Username similarity checking</li>
 * </ul>
 * 
 * <p>Strength scoring criteria:
 * <ul>
 * <li>Length (8+ chars: 10pts, 12+ chars: +15pts, 16+ chars: +15pts)</li>
 * <li>Character types (10pts each: lower, upper, digit, special)</li>
 * <li>Complexity bonus (10pts diversity, 10pts no patterns)</li>
 * </ul>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidPassword.List.class)
public @interface ValidPassword {
    
    /**
     * Default validation error message.
     * 
     * @return Error message
     */
    String message() default "Password does not meet security requirements";
    
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
     * Minimum password length.
     * 
     * @return Minimum length
     */
    int minLength() default 8;
    
    /**
     * Maximum password length.
     * 
     * @return Maximum length
     */
    int maxLength() default 128;
    
    /**
     * Whether to require at least one lowercase letter.
     * 
     * @return true if lowercase letters are required
     */
    boolean requireLowercase() default true;
    
    /**
     * Whether to require at least one uppercase letter.
     * 
     * @return true if uppercase letters are required
     */
    boolean requireUppercase() default true;
    
    /**
     * Whether to require at least one digit.
     * 
     * @return true if digits are required
     */
    boolean requireDigits() default true;
    
    /**
     * Whether to require at least one special character.
     * 
     * @return true if special characters are required
     */
    boolean requireSpecialChars() default false;
    
    /**
     * Whether to allow whitespace characters in the password.
     * 
     * @return true if whitespace is allowed
     */
    boolean allowWhitespace() default false;
    
    /**
     * Whether to reject common/weak passwords.
     * 
     * @return true if common passwords should be rejected
     */
    boolean rejectCommonPasswords() default true;
    
    /**
     * Minimum password strength score (0-100).
     * 
     * @return Minimum strength score
     */
    int minStrengthScore() default 40;
    
    /**
     * Defines several {@code @ValidPassword} annotations on the same element.
     */
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidPassword[] value();
    }
}