package com.exalt.ecosystem.shared.validation.validators;

import com.exalt.ecosystem.shared.validation.constraints.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Password validator for custom validation annotation.
 * Validates password strength according to configurable security requirements.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    private static final Pattern NO_WHITESPACE_PATTERN = Pattern.compile("^\\S*$");
    
    // Common password patterns to reject
    private static final Pattern[] COMMON_PATTERNS = {
        Pattern.compile("(?i).*password.*"),
        Pattern.compile("(?i).*123456.*"),
        Pattern.compile("(?i).*qwerty.*"),
        Pattern.compile("(?i).*admin.*"),
        Pattern.compile("(?i).*login.*"),
        Pattern.compile("(?i).*welcome.*"),
        Pattern.compile("(?i).*letmein.*"),
        Pattern.compile("(?i).*monkey.*"),
        Pattern.compile("(?i).*sunshine.*"),
        Pattern.compile("(?i).*master.*")
    };
    
    private boolean allowEmpty;
    private int minLength;
    private int maxLength;
    private boolean requireLowercase;
    private boolean requireUppercase;
    private boolean requireDigits;
    private boolean requireSpecialChars;
    private boolean allowWhitespace;
    private boolean rejectCommonPasswords;
    private int minStrengthScore;
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireDigits = constraintAnnotation.requireDigits();
        this.requireSpecialChars = constraintAnnotation.requireSpecialChars();
        this.allowWhitespace = constraintAnnotation.allowWhitespace();
        this.rejectCommonPasswords = constraintAnnotation.rejectCommonPasswords();
        this.minStrengthScore = constraintAnnotation.minStrengthScore();
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return allowEmpty;
        }
        
        // Disable default constraint violation message
        context.disableDefaultConstraintViolation();
        
        boolean isValid = true;
        
        // Length validation
        if (!isValidLength(password, context)) {
            isValid = false;
        }
        
        // Character requirements validation
        if (!isValidCharacterRequirements(password, context)) {
            isValid = false;
        }
        
        // Whitespace validation
        if (!allowWhitespace && !isValidWhitespace(password, context)) {
            isValid = false;
        }
        
        // Common password validation
        if (rejectCommonPasswords && !isValidCommonPassword(password, context)) {
            isValid = false;
        }
        
        // Strength score validation
        if (!isValidStrengthScore(password, context)) {
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Validates password length requirements.
     * 
     * @param password Password to validate
     * @param context Validation context
     * @return true if length is valid
     */
    private boolean isValidLength(String password, ConstraintValidatorContext context) {
        if (password.length() < minLength) {
            context.buildConstraintViolationWithTemplate(
                String.format("Password must be at least %d characters long", minLength))
                .addConstraintViolation();
            return false;
        }
        
        if (password.length() > maxLength) {
            context.buildConstraintViolationWithTemplate(
                String.format("Password must not exceed %d characters", maxLength))
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates character requirements.
     * 
     * @param password Password to validate
     * @param context Validation context
     * @return true if character requirements are met
     */
    private boolean isValidCharacterRequirements(String password, ConstraintValidatorContext context) {
        boolean isValid = true;
        
        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one lowercase letter")
                .addConstraintViolation();
            isValid = false;
        }
        
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one uppercase letter")
                .addConstraintViolation();
            isValid = false;
        }
        
        if (requireDigits && !DIGIT_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one digit")
                .addConstraintViolation();
            isValid = false;
        }
        
        if (requireSpecialChars && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one special character")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Validates whitespace requirements.
     * 
     * @param password Password to validate
     * @param context Validation context
     * @return true if whitespace requirements are met
     */
    private boolean isValidWhitespace(String password, ConstraintValidatorContext context) {
        if (!NO_WHITESPACE_PATTERN.matcher(password).matches()) {
            context.buildConstraintViolationWithTemplate(
                "Password must not contain whitespace characters")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
    
    /**
     * Validates against common passwords.
     * 
     * @param password Password to validate
     * @param context Validation context
     * @return true if password is not common
     */
    private boolean isValidCommonPassword(String password, ConstraintValidatorContext context) {
        for (Pattern pattern : COMMON_PATTERNS) {
            if (pattern.matcher(password).matches()) {
                context.buildConstraintViolationWithTemplate(
                    "Password is too common and easily guessable")
                    .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validates password strength score.
     * 
     * @param password Password to validate
     * @param context Validation context
     * @return true if strength score meets minimum requirement
     */
    private boolean isValidStrengthScore(String password, ConstraintValidatorContext context) {
        int score = calculatePasswordStrength(password);
        
        if (score < minStrengthScore) {
            String strengthLevel = getStrengthLevel(score);
            context.buildConstraintViolationWithTemplate(
                String.format("Password strength is %s (score: %d). Minimum required: %d", 
                    strengthLevel, score, minStrengthScore))
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculates password strength score (0-100).
     * 
     * @param password Password to analyze
     * @return Strength score
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length scoring (40 points max)
        int length = password.length();
        if (length >= 8) score += 10;
        if (length >= 12) score += 15;
        if (length >= 16) score += 15;
        
        // Character variety scoring (40 points max)
        if (LOWERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (UPPERCASE_PATTERN.matcher(password).matches()) score += 10;
        if (DIGIT_PATTERN.matcher(password).matches()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score += 10;
        
        // Complexity bonus (20 points max)
        int uniqueChars = (int) password.chars().distinct().count();
        if (uniqueChars >= length * 0.6) score += 10; // Good character diversity
        if (!hasRepeatingPatterns(password)) score += 10; // No obvious patterns
        
        return Math.min(score, 100);
    }
    
    /**
     * Checks for repeating patterns in password.
     * 
     * @param password Password to check
     * @return true if repeating patterns are found
     */
    private static boolean hasRepeatingPatterns(String password) {
        // Check for repeated characters (e.g., aaa, 111)
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        
        // Check for sequential patterns (e.g., abc, 123)
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if ((c2 == c1 + 1 && c3 == c2 + 1) || 
                (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets strength level description based on score.
     * 
     * @param score Password strength score
     * @return Strength level description
     */
    public static String getStrengthLevel(int score) {
        if (score >= 80) return "Very Strong";
        if (score >= 60) return "Strong";
        if (score >= 40) return "Moderate";
        if (score >= 20) return "Weak";
        return "Very Weak";
    }
    
    /**
     * Generates password suggestions based on current password.
     * 
     * @param password Current password
     * @return Array of suggestions
     */
    public static String[] generatePasswordSuggestions(String password) {
        if (password == null || password.isEmpty()) {
            return new String[]{
                "Use at least 8 characters",
                "Include uppercase and lowercase letters",
                "Add numbers and special characters",
                "Avoid common words and patterns"
            };
        }
        
        java.util.List<String> suggestions = new java.util.ArrayList<>();
        
        if (password.length() < 8) {
            suggestions.add("Increase length to at least 8 characters");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            suggestions.add("Add lowercase letters (a-z)");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            suggestions.add("Add uppercase letters (A-Z)");
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            suggestions.add("Add numbers (0-9)");
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            suggestions.add("Add special characters (!@#$%^&*)");
        }
        
        if (hasRepeatingPatterns(password)) {
            suggestions.add("Avoid repeating characters or sequential patterns");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Password strength is good");
        }
        
        return suggestions.toArray(new String[0]);
    }
    
    /**
     * Validates password against username to prevent similarity.
     * 
     * @param password Password to validate
     * @param username Username to compare against
     * @return true if password is sufficiently different from username
     */
    public static boolean isPasswordDifferentFromUsername(String password, String username) {
        if (password == null || username == null) {
            return password != null;
        }
        
        String lowerPassword = password.toLowerCase();
        String lowerUsername = username.toLowerCase();
        
        // Password should not contain username
        if (lowerPassword.contains(lowerUsername)) {
            return false;
        }
        
        // Username should not contain password
        if (lowerUsername.contains(lowerPassword)) {
            return false;
        }
        
        // Check for similarity (Levenshtein distance)
        int distance = calculateLevenshteinDistance(lowerPassword, lowerUsername);
        int minLength = Math.min(password.length(), username.length());
        
        // Passwords should be at least 50% different
        return distance >= minLength * 0.5;
    }
    
    /**
     * Calculates Levenshtein distance between two strings.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Edit distance
     */
    private static int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}