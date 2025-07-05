package com.gogidix.ecosystem.shared.model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UserRole enum functionality.
 * Tests role hierarchy, permissions, and business logic.
 */
class UserRoleTest {

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertThat(UserRole.ADMIN.getDisplayName()).isEqualTo("Admin");
        assertThat(UserRole.SUPER_ADMIN.getDisplayName()).isEqualTo("Super Admin");
        assertThat(UserRole.CUSTOMER.getDisplayName()).isEqualTo("Customer");
        assertThat(UserRole.VENDOR.getDisplayName()).isEqualTo("Vendor");
    }

    @Test
    @DisplayName("Should have correct descriptions")
    void shouldHaveCorrectDescriptions() {
        assertThat(UserRole.ADMIN.getDescription()).isEqualTo("Full system administrative access");
        assertThat(UserRole.CUSTOMER.getDescription()).isEqualTo("Standard customer access");
        assertThat(UserRole.VENDOR.getDescription()).isEqualTo("Product vendor access");
    }

    @Test
    @DisplayName("Should identify admin roles correctly")
    void shouldIdentifyAdminRoles() {
        assertThat(UserRole.ADMIN.isAdmin()).isTrue();
        assertThat(UserRole.SUPER_ADMIN.isAdmin()).isTrue();
        assertThat(UserRole.REGIONAL_ADMIN.isAdmin()).isTrue();
        assertThat(UserRole.CORPORATE_ADMIN.isAdmin()).isTrue();
        
        assertThat(UserRole.CUSTOMER.isAdmin()).isFalse();
        assertThat(UserRole.VENDOR.isAdmin()).isFalse();
        assertThat(UserRole.COURIER.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Should identify manager roles correctly")
    void shouldIdentifyManagerRoles() {
        assertThat(UserRole.WAREHOUSE_MANAGER.isManager()).isTrue();
        assertThat(UserRole.COURIER_MANAGER.isManager()).isTrue();
        assertThat(UserRole.ADMIN.isManager()).isTrue(); // Admins are also managers
        assertThat(UserRole.SUPER_ADMIN.isManager()).isTrue();
        
        assertThat(UserRole.CUSTOMER.isManager()).isFalse();
        assertThat(UserRole.WAREHOUSE_OPERATOR.isManager()).isFalse();
    }

    @Test
    @DisplayName("Should identify operational roles correctly")
    void shouldIdentifyOperationalRoles() {
        assertThat(UserRole.WAREHOUSE_OPERATOR.isOperational()).isTrue();
        assertThat(UserRole.COURIER.isOperational()).isTrue();
        assertThat(UserRole.SUPPORT_AGENT.isOperational()).isTrue();
        assertThat(UserRole.CONTENT_MODERATOR.isOperational()).isTrue();
        
        assertThat(UserRole.CUSTOMER.isOperational()).isFalse();
        assertThat(UserRole.ADMIN.isOperational()).isFalse();
    }

    @Test
    @DisplayName("Should identify business roles correctly")
    void shouldIdentifyBusinessRoles() {
        assertThat(UserRole.VENDOR.isBusiness()).isTrue();
        assertThat(UserRole.FINANCE_USER.isBusiness()).isTrue();
        assertThat(UserRole.MARKETING_USER.isBusiness()).isTrue();
        assertThat(UserRole.ANALYTICS_USER.isBusiness()).isTrue();
        
        assertThat(UserRole.CUSTOMER.isBusiness()).isFalse();
        assertThat(UserRole.ADMIN.isBusiness()).isFalse();
    }

    @Test
    @DisplayName("Should identify customer-facing roles correctly")
    void shouldIdentifyCustomerFacingRoles() {
        assertThat(UserRole.SUPPORT_AGENT.isCustomerFacing()).isTrue();
        assertThat(UserRole.COURIER.isCustomerFacing()).isTrue();
        assertThat(UserRole.VENDOR.isCustomerFacing()).isTrue();
        assertThat(UserRole.CONTENT_MODERATOR.isCustomerFacing()).isTrue();
        
        assertThat(UserRole.ADMIN.isCustomerFacing()).isFalse();
        assertThat(UserRole.FINANCE_USER.isCustomerFacing()).isFalse();
    }

    @Test
    @DisplayName("Should have correct hierarchy levels")
    void shouldHaveCorrectHierarchyLevels() {
        assertThat(UserRole.SUPER_ADMIN.getHierarchyLevel()).isEqualTo(10);
        assertThat(UserRole.ADMIN.getHierarchyLevel()).isEqualTo(9);
        assertThat(UserRole.CORPORATE_ADMIN.getHierarchyLevel()).isEqualTo(8);
        assertThat(UserRole.REGIONAL_ADMIN.getHierarchyLevel()).isEqualTo(7);
        assertThat(UserRole.WAREHOUSE_MANAGER.getHierarchyLevel()).isEqualTo(6);
        assertThat(UserRole.COURIER_MANAGER.getHierarchyLevel()).isEqualTo(6);
        assertThat(UserRole.FINANCE_USER.getHierarchyLevel()).isEqualTo(5);
        assertThat(UserRole.VENDOR.getHierarchyLevel()).isEqualTo(4);
        assertThat(UserRole.CUSTOMER.getHierarchyLevel()).isEqualTo(2);
        assertThat(UserRole.GUEST.getHierarchyLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle role assignment permissions correctly")
    void shouldHandleRoleAssignmentPermissions() {
        // Super admin can assign all roles
        assertThat(UserRole.SUPER_ADMIN.canAssignRole(UserRole.ADMIN)).isTrue();
        assertThat(UserRole.SUPER_ADMIN.canAssignRole(UserRole.CUSTOMER)).isTrue();
        assertThat(UserRole.SUPER_ADMIN.canAssignRole(UserRole.GUEST)).isTrue();
        
        // Admin cannot assign super admin
        assertThat(UserRole.ADMIN.canAssignRole(UserRole.SUPER_ADMIN)).isFalse();
        assertThat(UserRole.ADMIN.canAssignRole(UserRole.CUSTOMER)).isTrue();
        
        // Customer can assign guest role (level 2 > level 1)
        assertThat(UserRole.CUSTOMER.canAssignRole(UserRole.GUEST)).isTrue();
        assertThat(UserRole.CUSTOMER.canAssignRole(UserRole.CUSTOMER)).isFalse();
        
        // Role cannot assign itself
        assertThat(UserRole.ADMIN.canAssignRole(UserRole.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("Should return assignable roles correctly")
    void shouldReturnAssignableRoles() {
        // Super admin should be able to assign all roles except itself
        UserRole[] superAdminAssignable = UserRole.SUPER_ADMIN.getAssignableRoles();
        assertThat(superAdminAssignable).hasSize(UserRole.values().length - 1);
        assertThat(superAdminAssignable).doesNotContain(UserRole.SUPER_ADMIN);
        assertThat(superAdminAssignable).contains(UserRole.ADMIN, UserRole.CUSTOMER, UserRole.GUEST);
        
        // Customer should be able to assign guest role
        UserRole[] customerAssignable = UserRole.CUSTOMER.getAssignableRoles();
        assertThat(customerAssignable).hasSize(1);
        assertThat(customerAssignable).contains(UserRole.GUEST);
        
        // Admin should be able to assign most roles except super admin and admin
        UserRole[] adminAssignable = UserRole.ADMIN.getAssignableRoles();
        assertThat(adminAssignable).doesNotContain(UserRole.SUPER_ADMIN, UserRole.ADMIN);
        assertThat(adminAssignable).contains(UserRole.CUSTOMER, UserRole.VENDOR);
    }

    @Test
    @DisplayName("Should handle all enum values")
    void shouldHandleAllEnumValues() {
        // Verify all expected roles exist
        UserRole[] allRoles = UserRole.values();
        assertThat(allRoles).hasSize(17); // Update this if roles are added/removed
        
        // Verify specific critical roles exist
        assertThat(allRoles).contains(
            UserRole.SUPER_ADMIN,
            UserRole.ADMIN,
            UserRole.CUSTOMER,
            UserRole.VENDOR,
            UserRole.WAREHOUSE_OPERATOR,
            UserRole.WAREHOUSE_MANAGER,
            UserRole.COURIER,
            UserRole.COURIER_MANAGER,
            UserRole.REGIONAL_ADMIN,
            UserRole.CORPORATE_ADMIN,
            UserRole.SUPPORT_AGENT,
            UserRole.FINANCE_USER,
            UserRole.ANALYTICS_USER,
            UserRole.MARKETING_USER,
            UserRole.CONTENT_MODERATOR,
            UserRole.API_USER,
            UserRole.GUEST
        );
    }

    @Test
    @DisplayName("Should have consistent hierarchy ordering")
    void shouldHaveConsistentHierarchyOrdering() {
        // Higher hierarchy levels should be able to assign lower ones
        for (UserRole higherRole : UserRole.values()) {
            for (UserRole lowerRole : UserRole.values()) {
                if (higherRole.getHierarchyLevel() > lowerRole.getHierarchyLevel()) {
                    assertThat(higherRole.canAssignRole(lowerRole))
                        .as("Role %s (level %d) should be able to assign %s (level %d)", 
                            higherRole, higherRole.getHierarchyLevel(),
                            lowerRole, lowerRole.getHierarchyLevel())
                        .isTrue();
                }
            }
        }
    }
}