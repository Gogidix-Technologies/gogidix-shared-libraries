package com.gogidix.ecosystem.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Security utility class providing common security operations.
 * Contains methods for authentication, authorization, and security context management.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class SecurityUtils {
    
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // Password validation patterns
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
    
    // Private constructor to prevent instantiation
    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Gets the current authenticated user details.
     * 
     * @return Optional containing UserDetailsImpl if authenticated, empty otherwise
     */
    public static Optional<UserDetailsImpl> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                return Optional.of((UserDetailsImpl) authentication.getPrincipal());
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Gets the current user ID.
     * 
     * @return Optional containing user ID if authenticated, empty otherwise
     */
    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUserDetails().map(UserDetailsImpl::getId);
    }
    
    /**
     * Gets the current username.
     * 
     * @return Optional containing username if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserDetails().map(UserDetailsImpl::getUsername);
    }
    
    /**
     * Gets the current user email.
     * 
     * @return Optional containing email if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserDetails().map(UserDetailsImpl::getEmail);
    }
    
    /**
     * Gets the current user type.
     * 
     * @return Optional containing user type if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserType() {
        return getCurrentUserDetails().map(UserDetailsImpl::getUserType);
    }
    
    /**
     * Gets the current user authorities.
     * 
     * @return List of authorities for current user, empty if not authenticated
     */
    public static List<String> getCurrentUserAuthorities() {
        return getCurrentUserDetails()
                .map(userDetails -> userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
    
    /**
     * Gets the current user roles (without ROLE_ prefix).
     * 
     * @return List of roles for current user, empty if not authenticated
     */
    public static List<String> getCurrentUserRoles() {
        return getCurrentUserDetails()
                .map(UserDetailsImpl::getRoles)
                .orElse(List.of());
    }
    
    /**
     * Checks if current user is authenticated.
     * 
     * @return true if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
    
    /**
     * Checks if current user has a specific role.
     * 
     * @param role Role name (without ROLE_ prefix)
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        return getCurrentUserDetails()
                .map(userDetails -> userDetails.hasRole(role))
                .orElse(false);
    }
    
    /**
     * Checks if current user has any of the specified roles.
     * 
     * @param roles Role names (without ROLE_ prefix)
     * @return true if user has any of the roles
     */
    public static boolean hasAnyRole(String... roles) {
        return getCurrentUserDetails()
                .map(userDetails -> userDetails.hasAnyRole(roles))
                .orElse(false);
    }
    
    /**
     * Checks if current user has all specified roles.
     * 
     * @param roles Role names (without ROLE_ prefix)
     * @return true if user has all roles
     */
    public static boolean hasAllRoles(String... roles) {
        Optional<UserDetailsImpl> userDetails = getCurrentUserDetails();
        if (userDetails.isEmpty()) {
            return false;
        }
        
        for (String role : roles) {
            if (!userDetails.get().hasRole(role)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if current user has a specific authority.
     * 
     * @param authority Authority name
     * @return true if user has the authority
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentUserDetails()
                .map(userDetails -> userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(authority)))
                .orElse(false);
    }
    
    /**
     * Checks if current user has any of the specified authorities.
     * 
     * @param authorities Authority names
     * @return true if user has any of the authorities
     */
    public static boolean hasAnyAuthority(String... authorities) {
        Optional<UserDetailsImpl> userDetails = getCurrentUserDetails();
        if (userDetails.isEmpty()) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> userAuthorities = userDetails.get().getAuthorities();
        for (String authority : authorities) {
            if (userAuthorities.stream().anyMatch(auth -> auth.getAuthority().equals(authority))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if current user is the owner of a resource.
     * 
     * @param resourceOwnerId Resource owner ID
     * @return true if current user owns the resource
     */
    public static boolean isResourceOwner(UUID resourceOwnerId) {
        return getCurrentUserId()
                .map(userId -> userId.equals(resourceOwnerId))
                .orElse(false);
    }
    
    /**
     * Checks if current user can access a resource (is owner or has admin role).
     * 
     * @param resourceOwnerId Resource owner ID
     * @return true if user can access the resource
     */
    public static boolean canAccessResource(UUID resourceOwnerId) {
        return isResourceOwner(resourceOwnerId) || 
               hasAnyRole("ADMIN", "SUPER_ADMIN", "CORPORATE_ADMIN");
    }
    
    /**
     * Encodes a password using BCrypt.
     * 
     * @param rawPassword Raw password
     * @return Encoded password
     */
    public static String encodePassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }
    
    /**
     * Matches a raw password with an encoded password.
     * 
     * @param rawPassword Raw password
     * @param encodedPassword Encoded password
     * @return true if passwords match
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Validates password strength.
     * 
     * @param password Password to validate
     * @return true if password meets strength requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.matches(PASSWORD_PATTERN);
    }
    
    /**
     * Generates a secure random password.
     * 
     * @param length Password length
     * @return Generated password
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }
        
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = upperCase + lowerCase + digits + specialChars;
        
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one character from each category
        password.append(upperCase.charAt(SECURE_RANDOM.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(SECURE_RANDOM.nextInt(lowerCase.length())));
        password.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        password.append(specialChars.charAt(SECURE_RANDOM.nextInt(specialChars.length())));
        
        // Fill remaining length with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }
    
    /**
     * Generates a secure random token.
     * 
     * @param length Token length
     * @return Generated token
     */
    public static String generateSecureToken(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            token.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return token.toString();
    }
    
    /**
     * Generates a secure numeric token.
     * 
     * @param length Token length
     * @return Generated numeric token
     */
    public static String generateNumericToken(int length) {
        StringBuilder token = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            token.append(SECURE_RANDOM.nextInt(10));
        }
        
        return token.toString();
    }
    
    /**
     * Clears the security context.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
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
     * Helper method to shuffle a string.
     * 
     * @param input Input string
     * @return Shuffled string
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}