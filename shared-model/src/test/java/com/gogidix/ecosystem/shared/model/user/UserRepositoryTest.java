package com.gogidix.ecosystem.shared.model.user;

import com.gogidix.ecosystem.shared.model.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive database integration tests for User entity.
 * Tests JPA repository operations, constraints, indexing, and complex queries.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.hibernate.SQL=ERROR"
})
@org.junit.jupiter.api.Disabled("Requires Spring Boot application context - for future implementation")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserTestRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("1234567890")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();
    }

    @Test
    @DisplayName("Should save user with all required fields")
    void shouldSaveUserWithRequiredFields() {
        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should enforce username uniqueness constraint")
    void shouldEnforceUsernameUniqueness() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User duplicateUser = User.builder()
            .username("testuser") // Same username
            .email("different@example.com")
            .firstName("Different")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();

        // When/Then
        assertThatThrownBy(() -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should enforce email uniqueness constraint")
    void shouldEnforceEmailUniqueness() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User duplicateUser = User.builder()
            .username("differentuser")
            .email("test@example.com") // Same email
            .firstName("Different")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();

        // When/Then
        assertThatThrownBy(() -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should handle user roles collection operations")
    void shouldHandleUserRolesCollection() {
        // Given
        testUser.addRole(UserRole.CUSTOMER);
        testUser.addRole(UserRole.VENDOR);

        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(foundUser.getRoles()).hasSize(2);
        assertThat(foundUser.getRoles()).contains(UserRole.CUSTOMER, UserRole.VENDOR);
        assertThat(foundUser.hasRole(UserRole.CUSTOMER)).isTrue();
        assertThat(foundUser.hasRole(UserRole.VENDOR)).isTrue();
        assertThat(foundUser.hasRole(UserRole.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("Should persist user business logic methods correctly")
    void shouldPersistUserBusinessLogic() {
        // Given
        testUser.incrementFailedLoginAttempts();
        testUser.incrementFailedLoginAttempts();
        testUser.lockAccount(60); // 60 minutes
        testUser.setEmailVerificationToken("verification-token-123");
        testUser.setPhoneVerificationToken("123456");

        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(foundUser.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(foundUser.getLockedUntil()).isNotNull();
        assertThat(foundUser.isAccountLocked()).isTrue();
        assertThat(foundUser.getEmailVerificationToken()).isEqualTo("verification-token-123");
        assertThat(foundUser.getPhoneVerificationToken()).isEqualTo("123456");
    }

    @Test
    @DisplayName("Should handle soft delete operations")
    void shouldHandleSoftDeleteOperations() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // When - soft delete
        savedUser.markAsDeleted("admin");
        userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(foundUser.isDeleted()).isTrue();
        assertThat(foundUser.getDeletedAt()).isNotNull();
        assertThat(foundUser.getDeletedBy()).isEqualTo("admin");

        // When - restore
        foundUser.restore();
        userRepository.save(foundUser);
        entityManager.flush();
        entityManager.clear();

        User restoredUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(restoredUser.isDeleted()).isFalse();
        assertThat(restoredUser.getDeletedAt()).isNull();
        assertThat(restoredUser.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("Should find users by email with case sensitivity")
    void shouldFindUsersByEmail() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        Optional<User> notFound = userRepository.findByEmail("TEST@EXAMPLE.COM");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(notFound).isEmpty(); // Case sensitive
    }

    @Test
    @DisplayName("Should find users by username with case sensitivity")
    void shouldFindUsersByUsername() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        Optional<User> notFound = userRepository.findByUsername("TESTUSER");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(notFound).isEmpty(); // Case sensitive
    }

    @Test
    @DisplayName("Should find users by status with indexing optimization")
    void shouldFindUsersByStatus() {
        // Given
        User activeUser = testUser;
        User inactiveUser = User.builder()
            .username("inactive")
            .email("inactive@example.com")
            .firstName("Inactive")
            .lastName("User")
            .status(UserStatus.DEACTIVATED)
            .userType("customer")
            .build();
        User suspendedUser = User.builder()
            .username("suspended")
            .email("suspended@example.com")
            .firstName("Suspended")
            .lastName("User")
            .status(UserStatus.SUSPENDED)
            .userType("customer")
            .build();

        userRepository.saveAll(List.of(activeUser, inactiveUser, suspendedUser));
        entityManager.flush();

        // When
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByStatus(UserStatus.DEACTIVATED);
        List<User> suspendedUsers = userRepository.findByStatus(UserStatus.SUSPENDED);

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getStatus()).isEqualTo(UserStatus.DEACTIVATED);
        assertThat(suspendedUsers).hasSize(1);
        assertThat(suspendedUsers.get(0).getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should find users by user type with indexing optimization")
    void shouldFindUsersByUserType() {
        // Given
        User customerUser = testUser;
        User vendorUser = User.builder()
            .username("vendor")
            .email("vendor@example.com")
            .firstName("Vendor")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .userType("vendor")
            .build();

        userRepository.saveAll(List.of(customerUser, vendorUser));
        entityManager.flush();

        // When
        List<User> customers = userRepository.findByUserType("customer");
        List<User> vendors = userRepository.findByUserType("vendor");

        // Then
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getUserType()).isEqualTo("customer");
        assertThat(vendors).hasSize(1);
        assertThat(vendors.get(0).getUserType()).isEqualTo("vendor");
    }

    @Test
    @DisplayName("Should handle pagination and sorting correctly")
    void shouldHandlePaginationAndSorting() {
        // Given - Create multiple users
        for (int i = 1; i <= 10; i++) {
            User user = User.builder()
                .username("user" + i)
                .email("user" + i + "@example.com")
                .firstName("User")
                .lastName("Number" + i)
                .status(UserStatus.ACTIVE)
                .userType("customer")
                .build();
            userRepository.save(user);
        }
        entityManager.flush();

        // When - Get first page sorted by username
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("username"));
        Page<User> firstPage = userRepository.findAll(pageRequest);

        // Then
        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(firstPage.getTotalElements()).isEqualTo(10);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();
        
        // Verify sorting
        List<String> usernames = firstPage.getContent().stream()
            .map(User::getUsername)
            .toList();
        assertThat(usernames).isSorted();
    }

    @Test
    @DisplayName("Should support complex search queries")
    void shouldSupportComplexSearchQueries() {
        // Given
        User john = User.builder()
            .username("john.doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .status(UserStatus.ACTIVE)
            .userType("customer")
            .build();

        User jane = User.builder()
            .username("jane.smith")
            .email("jane@example.com")
            .firstName("Jane")
            .lastName("Smith")
            .status(UserStatus.ACTIVE)
            .userType("vendor")
            .build();

        userRepository.saveAll(List.of(john, jane));
        entityManager.flush();

        // When
        List<User> johnResults = userRepository.findByFirstNameContainingIgnoreCase("john");
        List<User> doeResults = userRepository.findByLastNameContainingIgnoreCase("doe");
        List<User> emailResults = userRepository.findByEmailContainingIgnoreCase("jane");

        // Then
        assertThat(johnResults).hasSize(1);
        assertThat(johnResults.get(0).getFirstName()).isEqualTo("John");
        
        assertThat(doeResults).hasSize(1);
        assertThat(doeResults.get(0).getLastName()).isEqualTo("Doe");
        
        assertThat(emailResults).hasSize(1);
        assertThat(emailResults.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Should test optimistic locking with version field")
    void shouldTestOptimisticLocking() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // When - Load same entity in two different sessions
        User user1 = userRepository.findById(savedUser.getId()).orElseThrow();
        User user2 = userRepository.findById(savedUser.getId()).orElseThrow();

        // Modify both entities
        user1.setFirstName("Modified1");
        user2.setFirstName("Modified2");

        // Save first entity (should succeed)
        userRepository.save(user1);
        entityManager.flush();

        // Then - Save second entity should fail due to optimistic locking
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // OptimisticLockException or similar
    }

    @Test
    @DisplayName("Should test audit fields are automatically populated")
    void shouldTestAuditFieldsPopulation() {
        // Given
        LocalDateTime beforeSave = LocalDateTime.now();

        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        LocalDateTime afterSave = LocalDateTime.now();

        // Then
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isBetween(beforeSave, afterSave);
        assertThat(savedUser.getUpdatedAt()).isBetween(beforeSave, afterSave);
        assertThat(savedUser.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should test user business methods with database persistence")
    void shouldTestUserBusinessMethodsPersistence() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();

        // When - Test email verification
        savedUser.setEmailVerificationToken("token123");
        savedUser.verifyEmail();
        userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        User verifiedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(verifiedUser.isEmailVerified()).isTrue();
        assertThat(verifiedUser.getEmailVerificationToken()).isNull();

        // When - Test phone verification
        verifiedUser.setPhoneVerificationToken("123456");
        verifiedUser.verifyPhone();
        userRepository.save(verifiedUser);
        entityManager.flush();
        entityManager.clear();

        User phoneVerifiedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(phoneVerifiedUser.isPhoneVerified()).isTrue();
        assertThat(phoneVerifiedUser.getPhoneVerificationToken()).isNull();

        // When - Test terms acceptance
        phoneVerifiedUser.acceptTerms();
        phoneVerifiedUser.acceptPrivacy();
        userRepository.save(phoneVerifiedUser);
        entityManager.flush();
        entityManager.clear();

        User termsAcceptedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // Then
        assertThat(termsAcceptedUser.getTermsAcceptedAt()).isNotNull();
        assertThat(termsAcceptedUser.getPrivacyAcceptedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle database constraints for required fields")
    void shouldHandleDatabaseConstraints() {
        // Test username constraint
        assertThatThrownBy(() -> {
            User invalidUser = User.builder()
                .email("valid@example.com")
                .firstName("Valid")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .userType("customer")
                // Missing username
                .build();
            userRepository.save(invalidUser);
            entityManager.flush();
        }).isInstanceOf(Exception.class);

        // Test email constraint
        assertThatThrownBy(() -> {
            User invalidUser = User.builder()
                .username("validuser")
                .firstName("Valid")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .userType("customer")
                // Missing email
                .build();
            userRepository.save(invalidUser);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    /**
     * Test repository interface for User entity testing.
     */
    @Repository
    interface UserTestRepository extends JpaRepository<User, UUID> {
        
        Optional<User> findByEmail(String email);
        
        Optional<User> findByUsername(String username);
        
        List<User> findByStatus(UserStatus status);
        
        List<User> findByUserType(String userType);
        
        List<User> findByFirstNameContainingIgnoreCase(String firstName);
        
        List<User> findByLastNameContainingIgnoreCase(String lastName);
        
        List<User> findByEmailContainingIgnoreCase(String email);
        
        @Query("SELECT u FROM User u WHERE u.deleted = false")
        List<User> findAllActive();
        
        @Query("SELECT u FROM User u WHERE u.lastLoginAt > :since")
        List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
        
        @Query("SELECT u FROM User u WHERE u.emailVerified = true AND u.phoneVerified = true")
        List<User> findFullyVerifiedUsers();
        
        @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
        long countByStatus(@Param("status") UserStatus status);
    }
}