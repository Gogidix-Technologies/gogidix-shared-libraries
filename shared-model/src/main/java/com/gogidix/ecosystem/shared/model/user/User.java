package com.gogidix.ecosystem.shared.model.user;

import com.gogidix.ecosystem.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Core user entity representing users across all microservices.
 * Provides common user attributes and relationships.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_type", columnList = "userType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {
    
    /**
     * Unique username for the user.
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;
    
    /**
     * User's email address.
     */
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;
    
    /**
     * User's first name.
     */
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;
    
    /**
     * User's last name.
     */
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;
    
    /**
     * User's phone number.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    /**
     * User's profile image URL.
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    /**
     * Date of birth.
     */
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    /**
     * User's gender.
     */
    @Column(name = "gender", length = 10)
    private String gender;
    
    /**
     * User's preferred language code.
     */
    @Column(name = "language_code", length = 5)
    private String languageCode;
    
    /**
     * User's timezone.
     */
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    /**
     * Current status of the user account.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;
    
    /**
     * Type of user (customer, vendor, admin, etc.).
     */
    @Column(name = "user_type", length = 20, nullable = false)
    private String userType;
    
    /**
     * Roles assigned to the user.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        indexes = @Index(name = "idx_user_roles_user_id", columnList = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();
    
    /**
     * Whether the user's email is verified.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
    
    /**
     * Whether the user's phone number is verified.
     */
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;
    
    /**
     * Whether two-factor authentication is enabled.
     */
    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;
    
    /**
     * Last login timestamp.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * Failed login attempts count.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;
    
    /**
     * Account locked until timestamp.
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    /**
     * Password reset token.
     */
    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;
    
    /**
     * Password reset token expiry.
     */
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    /**
     * Email verification token.
     */
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;
    
    /**
     * Phone verification token.
     */
    @Column(name = "phone_verification_token", length = 10)
    private String phoneVerificationToken;
    
    /**
     * Terms and conditions acceptance timestamp.
     */
    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;
    
    /**
     * Privacy policy acceptance timestamp.
     */
    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;
    
    /**
     * Marketing communications opt-in.
     */
    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn = false;
    
    /**
     * Gets the user's full name.
     * 
     * @return Combined first and last name
     */
    @Transient
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.toString();
    }
    
    /**
     * Checks if the user account is locked.
     * 
     * @return true if account is currently locked
     */
    @Transient
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    /**
     * Checks if the user account is active.
     * 
     * @return true if account is active and not locked
     */
    @Transient
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isAccountLocked();
    }
    
    /**
     * Checks if the user has a specific role.
     * 
     * @param role The role to check
     * @return true if user has the role
     */
    public boolean hasRole(UserRole role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Adds a role to the user.
     * 
     * @param role The role to add
     */
    public void addRole(UserRole role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
    
    /**
     * Removes a role from the user.
     * 
     * @param role The role to remove
     */
    public void removeRole(UserRole role) {
        if (roles != null) {
            roles.remove(role);
        }
    }
    
    /**
     * Increments failed login attempts.
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
    
    /**
     * Resets failed login attempts to zero.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * Locks the account for a specified duration.
     * 
     * @param lockDurationMinutes Duration to lock the account in minutes
     */
    public void lockAccount(long lockDurationMinutes) {
        this.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }
    
    /**
     * Updates the last login timestamp.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        resetFailedLoginAttempts();
    }
    
    /**
     * Verifies the user's email.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
    }
    
    /**
     * Verifies the user's phone number.
     */
    public void verifyPhone() {
        this.phoneVerified = true;
        this.phoneVerificationToken = null;
    }
    
    /**
     * Accepts terms and conditions.
     */
    public void acceptTerms() {
        this.termsAcceptedAt = LocalDateTime.now();
    }
    
    /**
     * Accepts privacy policy.
     */
    public void acceptPrivacy() {
        this.privacyAcceptedAt = LocalDateTime.now();
    }
}