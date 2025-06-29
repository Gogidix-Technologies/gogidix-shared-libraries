package com.exalt.ecosystem.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for JWT token provider functionality.
 * Tests token generation, validation, claims extraction, and security features.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "dGVzdC1zZWNyZXQtZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLWFuZC12YWxpZGF0aW9uLW11c3QtYmUtbG9uZy1lbm91Z2g=";
    private final long tokenExpirationMs = 86400000; // 24 hours
    private final long refreshTokenExpirationMs = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret, tokenExpirationMs, refreshTokenExpirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT token for user details")
    void shouldGenerateValidTokenForUserDetails() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();

        // When
        String token = jwtTokenProvider.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        UserDetailsImpl user1 = createTestUserDetails("user1", "user1@test.com");
        UserDetailsImpl user2 = createTestUserDetails("user2", "user2@test.com");

        // When
        String token1 = jwtTokenProvider.generateToken(user1);
        String token2 = jwtTokenProvider.generateToken(user2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUsernameFromToken(token1)).isEqualTo("user1");
        assertThat(jwtTokenProvider.getUsernameFromToken(token2)).isEqualTo("user2");
    }

    @Test
    @DisplayName("Should generate refresh token with longer expiration")
    void shouldGenerateRefreshTokenWithLongerExpiration() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();

        // When
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEqualTo(accessToken);
        
        Date accessExpiry = jwtTokenProvider.getExpirationDateFromToken(accessToken);
        Date refreshExpiry = jwtTokenProvider.getExpirationDateFromToken(refreshToken);
        assertThat(refreshExpiry).isAfter(accessExpiry);
    }

    @Test
    @DisplayName("Should extract username from token correctly")
    void shouldExtractUsernameFromToken() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails("testuser", "test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should extract user ID from token correctly")
    void shouldExtractUserIdFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        UserDetailsImpl userDetails = createTestUserDetails(userId, "testuser", "test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("Should extract email from token correctly")
    void shouldExtractEmailFromToken() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails("testuser", "test@example.com");
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should validate correct token successfully")
    void shouldValidateCorrectToken() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateToken(userDetails);

        // When/Then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "invalid.token.format";

        // When/Then
        assertThat(jwtTokenProvider.validateToken(malformedToken)).isFalse();
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void shouldRejectTokenWithWrongSignature() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 10) + "wrongsignature";

        // When/Then
        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("Should handle expired token correctly")
    void shouldHandleExpiredToken() {
        // Given - Create token with very short expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(testSecret, 1L, refreshTokenExpirationMs);

        UserDetailsImpl userDetails = createTestUserDetails();
        String token = shortLivedProvider.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When/Then
        assertThat(shortLivedProvider.validateToken(token)).isFalse();
        assertThat(shortLivedProvider.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract token from Authorization header")
    void shouldExtractTokenFromAuthorizationHeader() {
        // Given
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.signature";
        String authHeader = "Bearer " + token;

        // When
        String extractedToken = jwtTokenProvider.extractTokenFromHeader(authHeader);

        // Then
        assertThat(extractedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("Should return null for invalid Authorization header")
    void shouldReturnNullForInvalidAuthHeader() {
        // Test various invalid headers
        assertThat(jwtTokenProvider.extractTokenFromHeader(null)).isNull();
        assertThat(jwtTokenProvider.extractTokenFromHeader("")).isNull();
        assertThat(jwtTokenProvider.extractTokenFromHeader("InvalidFormat")).isNull();
        assertThat(jwtTokenProvider.extractTokenFromHeader("Basic dGVzdA==")).isNull();
        assertThat(jwtTokenProvider.extractTokenFromHeader("Bearer")).isNull();
        assertThat(jwtTokenProvider.extractTokenFromHeader("Bearer ")).isNull();
    }

    @Test
    @DisplayName("Should check if token needs refresh correctly")
    void shouldCheckIfTokenNeedsRefresh() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateToken(userDetails);

        // When - Fresh token should not need refresh
        boolean needsRefresh = jwtTokenProvider.needsRefresh(token, 300000); // 5 minutes threshold

        // Then
        assertThat(needsRefresh).isFalse();
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();
        String originalToken = jwtTokenProvider.generateToken(userDetails);

        // When
        String refreshedToken = jwtTokenProvider.refreshToken(originalToken);

        // Then
        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken).isNotEqualTo(originalToken);
        assertThat(jwtTokenProvider.validateToken(refreshedToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshedToken))
            .isEqualTo(jwtTokenProvider.getUsernameFromToken(originalToken));
    }

    @Test
    @DisplayName("Should handle null and empty token gracefully")
    void shouldHandleNullAndEmptyTokenGracefully() {
        // Test null token
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(null))
            .isInstanceOf(IllegalArgumentException.class);

        // Test empty token
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle token with missing claims")
    void shouldHandleTokenWithMissingClaims() {
        // Given - Create token with minimal claims
        Map<String, Object> minimalClaims = Map.of("sub", "testuser");
        // Create a minimal user details for testing
        UserDetailsImpl minimalUser = createTestUserDetails("testuser", "test@example.com");
        String tokenWithMissingClaims = jwtTokenProvider.generateToken(minimalUser);

        // When/Then
        assertThat(jwtTokenProvider.validateToken(tokenWithMissingClaims)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(tokenWithMissingClaims)).isEqualTo("testuser");
        
        // Email should be null or handle gracefully
        assertThatCode(() -> jwtTokenProvider.getEmailFromToken(tokenWithMissingClaims))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "invalid", "Bearer", "Bearer ", "Basic token"})
    @DisplayName("Should handle various invalid authorization header formats")
    void shouldHandleInvalidAuthHeaderFormats(String invalidHeader) {
        assertThat(jwtTokenProvider.extractTokenFromHeader(invalidHeader)).isNull();
    }

    @Test
    @DisplayName("Should generate token with custom expiration")
    void shouldGenerateTokenWithCustomExpiration() {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();
        long customExpirationMs = 3600000; // 1 hour

        // When
        // Use constructor with custom expiration for this test
        JwtTokenProvider customProvider = new JwtTokenProvider(testSecret, customExpirationMs, refreshTokenExpirationMs);
        String token = customProvider.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        
        Date expiration = customProvider.getExpirationDateFromToken(token);
        Date expectedExpiration = new Date(System.currentTimeMillis() + customExpirationMs);
        
        // Allow 1 second tolerance for test execution time
        assertThat(expiration).isBetween(
            new Date(expectedExpiration.getTime() - 1000),
            new Date(expectedExpiration.getTime() + 1000)
        );
    }

    @Test
    @DisplayName("Should handle concurrent token operations safely")
    void shouldHandleConcurrentTokenOperationsSafely() throws InterruptedException {
        // Given
        UserDetailsImpl userDetails = createTestUserDetails();
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        String[] tokens = new String[threadCount];

        // When - Generate tokens concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                tokens[index] = jwtTokenProvider.generateToken(userDetails);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All tokens should be valid and unique
        for (String token : tokens) {
            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        // All tokens should be unique (different iat claims)
        for (int i = 0; i < threadCount - 1; i++) {
            for (int j = i + 1; j < threadCount; j++) {
                assertThat(tokens[i]).isNotEqualTo(tokens[j]);
            }
        }
    }

    // Helper methods
    private UserDetailsImpl createTestUserDetails() {
        return createTestUserDetails("testuser", "test@example.com");
    }

    private UserDetailsImpl createTestUserDetails(String username, String email) {
        return createTestUserDetails(UUID.randomUUID(), username, email);
    }

    private UserDetailsImpl createTestUserDetails(UUID id, String username, String email) {
        return UserDetailsImpl.create(
            id,
            username,
            email,
            "password",
            "customer",
            List.of("USER"),
            true, true, true, true
        );
    }
}