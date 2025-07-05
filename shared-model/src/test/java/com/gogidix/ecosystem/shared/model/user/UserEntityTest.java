package com.gogidix.ecosystem.shared.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for User entity functionality.
 * Tests business logic, role management, and entity behaviors.
 */
class UserEntityTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("1234567890");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setUserType("customer");
    }

    @Test
    @DisplayName("Should create user with all required fields")
    void shouldCreateUserWithRequiredFields() {
        // When
        User user = User.builder()
            .username("newuser")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();

        // Then
        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getEmail()).isEqualTo("newuser@example.com");
        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getUserType()).isEqualTo("customer");
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isPhoneVerified()).isFalse();
        assertThat(user.isTwoFactorEnabled()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.isMarketingOptIn()).isFalse();
    }

    @Test
    @DisplayName("Should handle user roles correctly")
    void shouldHandleUserRoles() {
        // Given - User starts with empty roles collection
        assertThat(testUser.getRoles()).isNotNull();
        assertThat(testUser.getRoles()).isEmpty();

        // When - Add roles
        testUser.addRole(UserRole.CUSTOMER);
        testUser.addRole(UserRole.VENDOR);

        // Then
        assertThat(testUser.getRoles()).hasSize(2);
        assertThat(testUser.getRoles()).contains(UserRole.CUSTOMER, UserRole.VENDOR);
        assertThat(testUser.hasRole(UserRole.CUSTOMER)).isTrue();
        assertThat(testUser.hasRole(UserRole.VENDOR)).isTrue();
        assertThat(testUser.hasRole(UserRole.ADMIN)).isFalse();

        // When - Remove a role
        testUser.removeRole(UserRole.CUSTOMER);

        // Then
        assertThat(testUser.getRoles()).hasSize(1);
        assertThat(testUser.getRoles()).contains(UserRole.VENDOR);
        assertThat(testUser.hasRole(UserRole.CUSTOMER)).isFalse();
        assertThat(testUser.hasRole(UserRole.VENDOR)).isTrue();
    }

    @Test
    @DisplayName("Should handle roles collection when null")
    void shouldHandleNullRolesCollection() {
        // Given - User with null roles
        testUser.setRoles(null);

        // When - Add role to null collection
        testUser.addRole(UserRole.CUSTOMER);

        // Then - Should create new collection
        assertThat(testUser.getRoles()).isNotNull();
        assertThat(testUser.getRoles()).hasSize(1);
        assertThat(testUser.hasRole(UserRole.CUSTOMER)).isTrue();

        // When - Remove from existing collection
        testUser.removeRole(UserRole.CUSTOMER);

        // Then
        assertThat(testUser.getRoles()).isEmpty();

        // When - Remove from null collection (after setting to null)
        testUser.setRoles(null);
        testUser.removeRole(UserRole.VENDOR); // Should not throw exception

        // Then - Should remain null
        assertThat(testUser.getRoles()).isNull();
    }

    @Test
    @DisplayName("Should calculate full name correctly")
    void shouldCalculateFullName() {
        // Test with both names
        assertThat(testUser.getFullName()).isEqualTo("Test User");

        // Test with only first name
        testUser.setLastName(null);
        assertThat(testUser.getFullName()).isEqualTo("Test");

        // Test with only last name
        testUser.setFirstName(null);
        testUser.setLastName("User");
        assertThat(testUser.getFullName()).isEqualTo("User");

        // Test with neither name (should return username)
        testUser.setFirstName(null);
        testUser.setLastName(null);
        assertThat(testUser.getFullName()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should handle failed login attempts correctly")
    void shouldHandleFailedLoginAttempts() {
        // Given - Initial state
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(testUser.getLockedUntil()).isNull();
        assertThat(testUser.isAccountLocked()).isFalse();

        // When - Increment failed attempts
        testUser.incrementFailedLoginAttempts();
        testUser.incrementFailedLoginAttempts();

        // Then
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(2);

        // When - Lock account
        testUser.lockAccount(60); // 60 minutes

        // Then
        assertThat(testUser.getLockedUntil()).isNotNull();
        assertThat(testUser.isAccountLocked()).isTrue();

        // When - Reset failed attempts
        testUser.resetFailedLoginAttempts();

        // Then
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(testUser.getLockedUntil()).isNull();
        assertThat(testUser.isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("Should handle account locking and unlocking")
    void shouldHandleAccountLocking() {
        // Given - Account not locked
        assertThat(testUser.isAccountLocked()).isFalse();

        // When - Lock account for 30 minutes
        testUser.lockAccount(30);

        // Then - Account should be locked
        assertThat(testUser.isAccountLocked()).isTrue();
        assertThat(testUser.getLockedUntil()).isAfter(LocalDateTime.now());

        // When - Set lock time in the past (simulate expired lock)
        testUser.setLockedUntil(LocalDateTime.now().minusHours(1));

        // Then - Account should not be locked
        assertThat(testUser.isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("Should determine account active status correctly")
    void shouldDetermineAccountActiveStatus() {
        // Given - Active user, not locked
        testUser.setStatus(UserStatus.ACTIVE);
        assertThat(testUser.isActive()).isTrue();

        // When - User is locked
        testUser.lockAccount(60);

        // Then - Should not be active even though status is ACTIVE
        assertThat(testUser.isActive()).isFalse();

        // When - Unlock account but change status
        testUser.resetFailedLoginAttempts();
        testUser.setStatus(UserStatus.SUSPENDED);

        // Then - Should not be active
        assertThat(testUser.isActive()).isFalse();

        // When - Both active status and unlocked
        testUser.setStatus(UserStatus.ACTIVE);

        // Then - Should be active
        assertThat(testUser.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle last login update correctly")
    void shouldHandleLastLoginUpdate() {
        // Given - No previous login
        assertThat(testUser.getLastLoginAt()).isNull();
        testUser.setFailedLoginAttempts(3);
        testUser.lockAccount(30);

        // When - Update last login
        LocalDateTime beforeUpdate = LocalDateTime.now();
        testUser.updateLastLogin();
        LocalDateTime afterUpdate = LocalDateTime.now();

        // Then - Should update timestamp and reset failed attempts
        assertThat(testUser.getLastLoginAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(testUser.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("Should handle email verification correctly")
    void shouldHandleEmailVerification() {
        // Given - Email not verified, has verification token
        testUser.setEmailVerified(false);
        testUser.setEmailVerificationToken("token-123");

        // When - Verify email
        testUser.verifyEmail();

        // Then - Should be verified and token cleared
        assertThat(testUser.isEmailVerified()).isTrue();
        assertThat(testUser.getEmailVerificationToken()).isNull();
    }

    @Test
    @DisplayName("Should handle phone verification correctly")
    void shouldHandlePhoneVerification() {
        // Given - Phone not verified, has verification token
        testUser.setPhoneVerified(false);
        testUser.setPhoneVerificationToken("123456");

        // When - Verify phone
        testUser.verifyPhone();

        // Then - Should be verified and token cleared
        assertThat(testUser.isPhoneVerified()).isTrue();
        assertThat(testUser.getPhoneVerificationToken()).isNull();
    }

    @Test
    @DisplayName("Should handle terms and privacy acceptance")
    void shouldHandleTermsAndPrivacyAcceptance() {
        // Given - No acceptance timestamps
        assertThat(testUser.getTermsAcceptedAt()).isNull();
        assertThat(testUser.getPrivacyAcceptedAt()).isNull();

        // When - Accept terms
        LocalDateTime beforeTerms = LocalDateTime.now();
        testUser.acceptTerms();
        LocalDateTime afterTerms = LocalDateTime.now();

        // Then - Terms timestamp should be set
        assertThat(testUser.getTermsAcceptedAt()).isBetween(beforeTerms, afterTerms);

        // When - Accept privacy
        LocalDateTime beforePrivacy = LocalDateTime.now();
        testUser.acceptPrivacy();
        LocalDateTime afterPrivacy = LocalDateTime.now();

        // Then - Privacy timestamp should be set
        assertThat(testUser.getPrivacyAcceptedAt()).isBetween(beforePrivacy, afterPrivacy);
    }

    @Test
    @DisplayName("Should handle builder pattern correctly")
    void shouldHandleBuilderPattern() {
        // When - Build user with various fields
        User builtUser = User.builder()
            .username("builderuser")
            .email("builder@example.com")
            .firstName("Builder")
            .lastName("User")
            .phoneNumber("9876543210")
            .profileImageUrl("https://example.com/profile.jpg")
            .dateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0))
            .gender("Male")
            .languageCode("en")
            .timezone("UTC")
            .status(UserStatus.PENDING)
            .userType("vendor")
            .emailVerified(true)
            .phoneVerified(true)
            .twoFactorEnabled(true)
            .marketingOptIn(true)
            .build();

        // Then - All fields should be set correctly
        assertThat(builtUser.getUsername()).isEqualTo("builderuser");
        assertThat(builtUser.getEmail()).isEqualTo("builder@example.com");
        assertThat(builtUser.getFirstName()).isEqualTo("Builder");
        assertThat(builtUser.getLastName()).isEqualTo("User");
        assertThat(builtUser.getPhoneNumber()).isEqualTo("9876543210");
        assertThat(builtUser.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        assertThat(builtUser.getDateOfBirth()).isEqualTo(LocalDateTime.of(1990, 1, 1, 0, 0));
        assertThat(builtUser.getGender()).isEqualTo("Male");
        assertThat(builtUser.getLanguageCode()).isEqualTo("en");
        assertThat(builtUser.getTimezone()).isEqualTo("UTC");
        assertThat(builtUser.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(builtUser.getUserType()).isEqualTo("vendor");
        assertThat(builtUser.isEmailVerified()).isTrue();
        assertThat(builtUser.isPhoneVerified()).isTrue();
        assertThat(builtUser.isTwoFactorEnabled()).isTrue();
        assertThat(builtUser.isMarketingOptIn()).isTrue();
    }

    @Test
    @DisplayName("Should test equals and hashCode consistency")
    void shouldTestEqualsAndHashCodeConsistency() {
        // Given - Two users with same ID
        UUID sharedId = UUID.randomUUID();
        User user1 = User.builder()
            .username("user1")
            .email("user1@example.com")
            .firstName("User")
            .lastName("One")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();
        user1.setId(sharedId);

        User user2 = User.builder()
            .username("user2")
            .email("user2@example.com")
            .firstName("User")
            .lastName("Two")
            .status(UserStatus.PENDING)
            .userType("vendor")
            .build();
        user2.setId(sharedId);

        // When/Then - Should be equal based on ID only
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());

        // Given - Different IDs
        user2.setId(UUID.randomUUID());

        // When/Then - Should not be equal
        assertThat(user1).isNotEqualTo(user2);

        // Given - One user with null ID
        user2.setId(null);

        // When/Then - Should not be equal
        assertThat(user1).isNotEqualTo(user2);

        // Given - Both users with null IDs
        user1.setId(null);

        // When/Then - Should not be equal (different instances)
        assertThat(user1).isNotEqualTo(user2);

        // When/Then - Same reference should be equal
        assertThat(user1).isEqualTo(user1);
    }

    @Test
    @DisplayName("Should inherit BaseEntity functionality")
    void shouldInheritBaseEntityFunctionality() {
        // Given - New user
        assertThat(testUser.isNew()).isTrue();
        assertThat(testUser.getId()).isNull();

        // When - Set ID (simulating persistence)
        testUser.setId(UUID.randomUUID());

        // Then - Should not be new
        assertThat(testUser.isNew()).isFalse();

        // Test soft delete functionality
        assertThat(testUser.isDeleted()).isFalse();
        testUser.markAsDeleted("admin");
        assertThat(testUser.isDeleted()).isTrue();
        assertThat(testUser.getDeletedBy()).isEqualTo("admin");

        // Test restore functionality
        testUser.restore();
        assertThat(testUser.isDeleted()).isFalse();
        assertThat(testUser.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void shouldHandleEdgeCases() {
        // Test hasRole with null roles collection
        testUser.setRoles(null);
        assertThat(testUser.hasRole(UserRole.CUSTOMER)).isFalse();

        // Test removeRole with null roles collection
        testUser.removeRole(UserRole.CUSTOMER); // Should not throw exception

        // Test addRole creating new collection
        testUser.addRole(UserRole.CUSTOMER);
        assertThat(testUser.getRoles()).isNotNull();
        assertThat(testUser.getRoles()).hasSize(1);

        // Test account locked with null lockedUntil
        testUser.setLockedUntil(null);
        assertThat(testUser.isAccountLocked()).isFalse();

        // Test isActive with various status combinations
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setLockedUntil(null);
        assertThat(testUser.isActive()).isTrue();

        testUser.setStatus(UserStatus.DELETED);
        assertThat(testUser.isActive()).isFalse();
    }
}