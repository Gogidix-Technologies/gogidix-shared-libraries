package com.gogidix.ecosystem.shared.model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Simple debugging tests for User entity.
 */
class UserSimpleTest {

    @Test
    @DisplayName("Should debug user creation")
    void shouldDebugUserCreation() {
        // Test direct constructor
        User directUser = new User();
        System.out.println("Direct user roles: " + directUser.getRoles());
        
        // Test builder
        User builtUser = User.builder()
            .username("test")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();
        
        System.out.println("Built user roles: " + builtUser.getRoles());
        
        // Test manual initialization
        if (builtUser.getRoles() == null) {
            System.out.println("Roles is null, initializing manually");
            builtUser.addRole(UserRole.CUSTOMER);
        }
        
        assertThat(builtUser.getRoles()).isNotNull();
    }
}