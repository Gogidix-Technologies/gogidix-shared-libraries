package com.gogidix.ecosystem.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for SecurityUtils functionality.
 * Tests security context management, role checking, and password operations.
 */
@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    private SecurityContext securityContext;
    private Authentication authentication;
    private UserDetailsImpl userDetails;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        passwordEncoder = new BCryptPasswordEncoder();
        
        userDetails = createTestUserDetails();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    @DisplayName("Should get current user details from security context")
    void shouldGetCurrentUserDetailsFromSecurityContext() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Optional<UserDetailsImpl> result = SecurityUtils.getCurrentUserDetails();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(userDetails);
        }
    }

    @Test
    @DisplayName("Should return empty when no authentication in context")
    void shouldReturnEmptyWhenNoAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Optional<UserDetailsImpl> result = SecurityUtils.getCurrentUserDetails();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Should return empty when authentication not authenticated")
    void shouldReturnEmptyWhenNotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Optional<UserDetailsImpl> result = SecurityUtils.getCurrentUserDetails();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Should return empty when principal is not UserDetailsImpl")
    void shouldReturnEmptyWhenPrincipalIsNotUserDetails() {
        // Given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Optional<UserDetailsImpl> result = SecurityUtils.getCurrentUserDetails();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Should get current user ID")
    void shouldGetCurrentUserId() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Optional<UUID> result = SecurityUtils.getCurrentUserId();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(userDetails.getId());
        }
    }

    @Test
    @DisplayName("Should get current username")
    void shouldGetCurrentUsername() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Optional<String> result = SecurityUtils.getCurrentUsername();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(userDetails.getUsername());
        }
    }

    @Test
    @DisplayName("Should check if user has specific role")
    void shouldCheckIfUserHasSpecificRole() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.hasRole("CUSTOMER")).isTrue();
            assertThat(SecurityUtils.hasRole("ADMIN")).isFalse();
            assertThat(SecurityUtils.hasRole("VENDOR")).isTrue();
        }
    }

    @Test
    @DisplayName("Should check if user has any of the specified roles")
    void shouldCheckIfUserHasAnyRole() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.hasAnyRole("ADMIN", "CUSTOMER")).isTrue();
            assertThat(SecurityUtils.hasAnyRole("ADMIN", "SUPPORT_AGENT")).isFalse();
            assertThat(SecurityUtils.hasAnyRole("VENDOR", "WAREHOUSE_OPERATOR")).isTrue();
        }
    }

    @Test
    @DisplayName("Should check if user has all specified roles")
    void shouldCheckIfUserHasAllRoles() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.hasAllRoles("CUSTOMER", "VENDOR")).isTrue();
            assertThat(SecurityUtils.hasAllRoles("CUSTOMER", "ADMIN")).isFalse();
            assertThat(SecurityUtils.hasAllRoles("ADMIN", "SUPER_ADMIN")).isFalse();
        }
    }

    @Test
    @DisplayName("Should return false for role checks when no authentication")
    void shouldReturnFalseForRoleChecksWhenNoAuth() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.hasRole("CUSTOMER")).isFalse();
            assertThat(SecurityUtils.hasAnyRole("CUSTOMER", "VENDOR")).isFalse();
            assertThat(SecurityUtils.hasAllRoles("CUSTOMER")).isFalse();
        }
    }

    @Test
    @DisplayName("Should check if user can access resource")
    void shouldCheckIfUserCanAccessResource() {
        // Given
        UUID resourceOwnerId = userDetails.getId();
        UUID differentUserId = UUID.randomUUID();
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.canAccessResource(resourceOwnerId)).isTrue();
            assertThat(SecurityUtils.canAccessResource(differentUserId)).isFalse();
        }
    }

    @Test
    @DisplayName("Should check if user is resource owner")
    void shouldCheckIfUserIsResourceOwner() {
        // Given
        UUID resourceOwnerId = userDetails.getId();
        UUID differentUserId = UUID.randomUUID();
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.isResourceOwner(resourceOwnerId)).isTrue();
            assertThat(SecurityUtils.isResourceOwner(differentUserId)).isFalse();
        }
    }

    @Test
    @DisplayName("Should admin users access any resource")
    void shouldAdminUsersAccessAnyResource() {
        // Given - Create admin user
        UserDetailsImpl adminUser = createAdminUserDetails();
        when(authentication.getPrincipal()).thenReturn(adminUser);
        
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            UUID anyResourceId = UUID.randomUUID();
            
            // When/Then
            assertThat(SecurityUtils.canAccessResource(anyResourceId)).isTrue();
        }
    }

    @Test
    @DisplayName("Should encode password securely")
    void shouldEncodePasswordSecurely() {
        // Given
        String rawPassword = "testPassword123!";

        // When
        String encodedPassword = SecurityUtils.encodePassword(rawPassword);

        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$"); // BCrypt format
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("Should generate different encoded passwords for same input")
    void shouldGenerateDifferentEncodedPasswordsForSameInput() {
        // Given
        String password = "testPassword123!";

        // When
        String encoded1 = SecurityUtils.encodePassword(password);
        String encoded2 = SecurityUtils.encodePassword(password);

        // Then
        assertThat(encoded1).isNotEqualTo(encoded2); // Different salt
        assertThat(passwordEncoder.matches(password, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(password, encoded2)).isTrue();
    }

    @Test
    @DisplayName("Should generate secure random password")
    void shouldGenerateSecureRandomPassword() {
        // When
        String password1 = SecurityUtils.generateSecurePassword();
        String password2 = SecurityUtils.generateSecurePassword();

        // Then
        assertThat(password1).isNotNull();
        assertThat(password1).isNotEmpty();
        assertThat(password1).hasSizeGreaterThanOrEqualTo(12);
        assertThat(password1).isNotEqualTo(password2); // Should be different
        
        // Should contain various character types
        assertThat(password1).matches(".*[a-z].*"); // lowercase
        assertThat(password1).matches(".*[A-Z].*"); // uppercase
        assertThat(password1).matches(".*[0-9].*"); // digits
        assertThat(password1).matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*"); // special chars
    }

    @Test
    @DisplayName("Should generate secure password with custom length")
    void shouldGenerateSecurePasswordWithCustomLength() {
        // Given
        int customLength = 20;

        // When
        String password = SecurityUtils.generateSecurePassword(customLength);

        // Then
        assertThat(password).hasSize(customLength);
        assertThat(password).matches(".*[a-z].*");
        assertThat(password).matches(".*[A-Z].*");
        assertThat(password).matches(".*[0-9].*");
        assertThat(password).matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
    }

    @Test
    @DisplayName("Should handle null and empty values gracefully")
    void shouldHandleNullAndEmptyValuesGracefully() {
        // Test null password encoding
        assertThatThrownBy(() -> SecurityUtils.encodePassword(null))
            .isInstanceOf(IllegalArgumentException.class);

        // Test empty password encoding
        assertThatThrownBy(() -> SecurityUtils.encodePassword(""))
            .isInstanceOf(IllegalArgumentException.class);

        // Test invalid password length
        assertThatThrownBy(() -> SecurityUtils.generateSecurePassword(3))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should check user authentication status")
    void shouldCheckUserAuthenticationStatus() {
        // Given - authenticated user
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.isAuthenticated()).isTrue();
        }

        // Given - not authenticated
        when(authentication.isAuthenticated()).thenReturn(false);
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When/Then
            assertThat(SecurityUtils.isAuthenticated()).isFalse();
        }
    }

    @Test
    @DisplayName("Should validate password strength")
    void shouldValidatePasswordStrength() {
        // Strong passwords
        assertThat(SecurityUtils.isValidPassword("StrongP@ssw0rd123")).isTrue();
        assertThat(SecurityUtils.isValidPassword("MyC0mpl3x!P@ssw0rd")).isTrue();

        // Weak passwords
        assertThat(SecurityUtils.isValidPassword("weak")).isFalse();
        assertThat(SecurityUtils.isValidPassword("password123")).isFalse();
        assertThat(SecurityUtils.isValidPassword("PASSWORD123")).isFalse();
        assertThat(SecurityUtils.isValidPassword("Password!")).isFalse(); // too short
        assertThat(SecurityUtils.isValidPassword(null)).isFalse();
        assertThat(SecurityUtils.isValidPassword("")).isFalse();
    }

    @Test
    @DisplayName("Should clear security context")
    void shouldClearSecurityContext() {
        // Given
        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            // When
            SecurityUtils.clearSecurityContext();

            // Then
            holder.verify(SecurityContextHolder::clearContext);
        }
    }

    // Helper methods
    private UserDetailsImpl createTestUserDetails() {
        return UserDetailsImpl.create(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            "password",
            "customer",
            List.of("CUSTOMER", "VENDOR"),
            true, true, true, true
        );
    }

    private UserDetailsImpl createAdminUserDetails() {
        return UserDetailsImpl.create(
            UUID.randomUUID(),
            "admin",
            "admin@example.com",
            "password",
            "admin",
            List.of("ADMIN", "SUPER_ADMIN"),
            true, true, true, true
        );
    }
}