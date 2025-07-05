package com.gogidix.ecosystem.shared.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ValidationUtils functionality.
 * Tests pattern validation, range validation, collection validation, and security features.
 */
class ValidationUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@domain.co.uk",
        "first.last+tag@example.org",
        "test123@test-domain.com",
        "user@sub.domain.com"
    })
    @DisplayName("Should validate correct email formats")
    void shouldValidateCorrectEmailFormats(String email) {
        assertThat(ValidationUtils.isValidEmail(email)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",
        "@domain.com",
        "user@",
        "user..name@domain.com",
        "user@domain",
        "",
        "   ",
        "user@domain..com"
    })
    @DisplayName("Should reject invalid email formats")
    void shouldRejectInvalidEmailFormats(String email) {
        assertThat(ValidationUtils.isValidEmail(email)).isFalse();
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void shouldHandleNullEmailGracefully() {
        assertThat(ValidationUtils.isValidEmail(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "+1234567890",
        "+44 20 7946 0958",
        "+33 1 42 34 56 78",
        "01234567890",
        "(555) 123-4567",
        "555-123-4567"
    })
    @DisplayName("Should validate correct phone formats")
    void shouldValidateCorrectPhoneFormats(String phone) {
        assertThat(ValidationUtils.isValidPhone(phone)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123",
        "abc",
        "123-",
        "+1234567890123456789", // too long
        "",
        "   "
    })
    @DisplayName("Should reject invalid phone formats")
    void shouldRejectInvalidPhoneFormats(String phone) {
        assertThat(ValidationUtils.isValidPhone(phone)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "StrongP@ssw0rd123, true",
        "MyC0mpl3x!P@ssw0rd, true",
        "Secur3$Password, true",
        "weak, false",
        "password123, false",
        "PASSWORD123, false",
        "Password!, false", // too short
        "password, false",  // no caps/numbers/symbols
        "'', false"
    })
    @DisplayName("Should validate password strength correctly")
    void shouldValidatePasswordStrength(String password, boolean expected) {
        assertThat(ValidationUtils.isValidPassword(password)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should handle null password in strength validation")
    void shouldHandleNullPasswordInStrengthValidation() {
        assertThat(ValidationUtils.isValidPassword(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://www.example.com",
        "http://example.com",
        "https://sub.domain.com/path?param=value",
        "http://localhost:8080",
        "https://api.service.com/v1/endpoint"
    })
    @DisplayName("Should validate correct URL formats")
    void shouldValidateCorrectUrlFormats(String url) {
        assertThat(ValidationUtils.isValidUrl(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-url",
        "ftp://example.com", // not http/https
        "http://",
        "https://",
        "",
        "   ",
        "just-text"
    })
    @DisplayName("Should reject invalid URL formats")
    void shouldRejectInvalidUrlFormats(String url) {
        assertThat(ValidationUtils.isValidUrl(url)).isFalse();
    }

    @Test
    @DisplayName("Should validate UUID format correctly")
    void shouldValidateUuidFormat() {
        // Valid UUIDs
        String validUuid = UUID.randomUUID().toString();
        assertThat(ValidationUtils.isValidUUID(validUuid)).isTrue();
        assertThat(ValidationUtils.isValidUUID("550e8400-e29b-41d4-a716-446655440000")).isTrue();

        // Invalid UUIDs
        assertThat(ValidationUtils.isValidUUID("invalid-uuid")).isFalse();
        assertThat(ValidationUtils.isValidUUID("550e8400-e29b-41d4-a716")).isFalse(); // too short
        assertThat(ValidationUtils.isValidUUID("")).isFalse();
        assertThat(ValidationUtils.isValidUUID(null)).isFalse();
    }

    @Test
    @DisplayName("Should validate numeric ranges correctly")
    void shouldValidateNumericRanges() {
        // Valid ranges
        assertThat(ValidationUtils.isInRange(5, 1, 10)).isTrue();
        assertThat(ValidationUtils.isInRange(1, 1, 10)).isTrue(); // boundary
        assertThat(ValidationUtils.isInRange(10, 1, 10)).isTrue(); // boundary
        assertThat(ValidationUtils.isInRange(5.5, 1.0, 10.0)).isTrue();

        // Invalid ranges  
        assertThat(ValidationUtils.isInRange(0, 1, 10)).isFalse();
        assertThat(ValidationUtils.isInRange(11, 1, 10)).isFalse();
        assertThat(ValidationUtils.isInRange(-1, 1, 10)).isFalse();
    }

    @Test
    @DisplayName("Should validate collections correctly")
    void shouldValidateCollections() {
        // Valid collections
        List<String> validList = List.of("item1", "item2", "item3");
        assertThat(ValidationUtils.isValidCollection(validList, 1, 5)).isTrue();
        assertThat(ValidationUtils.isValidCollection(validList, 3, 3)).isTrue(); // exact size

        Set<String> validSet = Set.of("item1", "item2");
        assertThat(ValidationUtils.isValidCollection(validSet, 1, 5)).isTrue();

        // Invalid collections
        assertThat(ValidationUtils.isValidCollection(validList, 5, 10)).isFalse(); // too small
        assertThat(ValidationUtils.isValidCollection(validList, 1, 2)).isFalse(); // too large
        assertThat(ValidationUtils.isValidCollection(null, 1, 5)).isFalse();
        assertThat(ValidationUtils.isValidCollection(List.of(), 1, 5)).isFalse(); // empty
    }

    @Test
    @DisplayName("Should validate credit card numbers using Luhn algorithm")
    void shouldValidateCreditCardNumbers() {
        // Valid credit card numbers (test numbers)
        assertThat(ValidationUtils.isValidCreditCard("4532015112830366")).isTrue(); // Visa
        assertThat(ValidationUtils.isValidCreditCard("5555555555554444")).isTrue(); // MasterCard
        assertThat(ValidationUtils.isValidCreditCard("378282246310005")).isTrue(); // Amex
        
        // With spaces and hyphens
        assertThat(ValidationUtils.isValidCreditCard("4532 0151 1283 0366")).isTrue();
        assertThat(ValidationUtils.isValidCreditCard("4532-0151-1283-0366")).isTrue();

        // Invalid credit card numbers
        assertThat(ValidationUtils.isValidCreditCard("1234567890123456")).isFalse(); // fails Luhn
        assertThat(ValidationUtils.isValidCreditCard("123")).isFalse(); // too short
        assertThat(ValidationUtils.isValidCreditCard("abcd1234efgh5678")).isFalse(); // contains letters
        assertThat(ValidationUtils.isValidCreditCard("")).isFalse();
        assertThat(ValidationUtils.isValidCreditCard(null)).isFalse();
    }

    @Test
    @DisplayName("Should sanitize input strings correctly")
    void shouldSanitizeInputStrings() {
        // HTML/Script sanitization
        assertThat(ValidationUtils.sanitizeInput("<script>alert('xss')</script>"))
            .doesNotContain("<script>")
            .doesNotContain("alert");

        assertThat(ValidationUtils.sanitizeInput("<img src=x onerror=alert(1)>"))
            .doesNotContain("onerror")
            .doesNotContain("alert");

        // SQL injection patterns
        assertThat(ValidationUtils.sanitizeInput("'; DROP TABLE users; --"))
            .doesNotContain("DROP TABLE")
            .doesNotContain("--");

        // Normal text should remain unchanged
        String normalText = "This is normal text with numbers 123 and symbols !@#";
        assertThat(ValidationUtils.sanitizeInput(normalText)).isEqualTo(normalText);

        // Null and empty handling
        assertThat(ValidationUtils.sanitizeInput(null)).isNull();
        assertThat(ValidationUtils.sanitizeInput("")).isEmpty();
    }

    @Test
    @DisplayName("Should validate bean using Jakarta validation")
    void shouldValidateBeanUsingJakartaValidation() {
        // Valid bean
        TestBean validBean = new TestBean("test@example.com", "Valid Name", "password123");
        ValidationResult result = ValidationUtils.validate(validBean);
        assertThat(result.isValid()).isTrue();

        // Invalid bean
        TestBean invalidBean = new TestBean("invalid-email", "", "123"); // all fields invalid
        ValidationResult invalidResult = ValidationUtils.validate(invalidBean);
        assertThat(invalidResult.isValid()).isFalse();
        assertThat(invalidResult.hasFieldErrors()).isTrue();
        assertThat(invalidResult.getErrorCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should validate single property using Jakarta validation")
    void shouldValidateSinglePropertyUsingJakartaValidation() {
        // Valid property
        TestBean bean = new TestBean("test@example.com", "Valid Name", "password123");
        ValidationResult result = ValidationUtils.validateProperty(bean, "email");
        assertThat(result.isValid()).isTrue();

        // Invalid property
        bean.setEmail("invalid-email");
        ValidationResult invalidResult = ValidationUtils.validateProperty(bean, "email");
        assertThat(invalidResult.isValid()).isFalse();
        assertThat(invalidResult.hasFieldError("email")).isTrue();
    }

    @Test
    @DisplayName("Should handle null bean validation gracefully")
    void shouldHandleNullBeanValidationGracefully() {
        ValidationResult result = ValidationUtils.validate(null);
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasGlobalErrors()).isTrue();
    }

    @Test
    @DisplayName("Should validate IP address formats")
    void shouldValidateIpAddressFormats() {
        // Valid IPv4
        assertThat(ValidationUtils.isValidIpAddress("192.168.1.1")).isTrue();
        assertThat(ValidationUtils.isValidIpAddress("10.0.0.1")).isTrue();
        assertThat(ValidationUtils.isValidIpAddress("255.255.255.255")).isTrue();

        // Valid IPv6
        assertThat(ValidationUtils.isValidIpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334")).isTrue();
        assertThat(ValidationUtils.isValidIpAddress("::1")).isTrue(); // localhost

        // Invalid IP addresses
        assertThat(ValidationUtils.isValidIpAddress("256.1.1.1")).isFalse(); // out of range
        assertThat(ValidationUtils.isValidIpAddress("192.168.1")).isFalse(); // incomplete
        assertThat(ValidationUtils.isValidIpAddress("invalid-ip")).isFalse();
        assertThat(ValidationUtils.isValidIpAddress("")).isFalse();
        assertThat(ValidationUtils.isValidIpAddress(null)).isFalse();
    }

    @Test
    @DisplayName("Should validate date formats")
    void shouldValidateDateFormats() {
        // Valid date formats
        assertThat(ValidationUtils.isValidDate("2023-12-25", "yyyy-MM-dd")).isTrue();
        assertThat(ValidationUtils.isValidDate("25/12/2023", "dd/MM/yyyy")).isTrue();
        assertThat(ValidationUtils.isValidDate("Dec 25, 2023", "MMM dd, yyyy")).isTrue();

        // Invalid dates
        assertThat(ValidationUtils.isValidDate("2023-13-25", "yyyy-MM-dd")).isFalse(); // invalid month
        assertThat(ValidationUtils.isValidDate("2023-02-30", "yyyy-MM-dd")).isFalse(); // invalid day
        assertThat(ValidationUtils.isValidDate("invalid-date", "yyyy-MM-dd")).isFalse();
        assertThat(ValidationUtils.isValidDate("", "yyyy-MM-dd")).isFalse();
        assertThat(ValidationUtils.isValidDate(null, "yyyy-MM-dd")).isFalse();
    }

    @Test
    @DisplayName("Should validate file extensions")
    void shouldValidateFileExtensions() {
        String[] allowedExtensions = {"jpg", "png", "pdf", "docx"};

        // Valid extensions
        assertThat(ValidationUtils.hasValidExtension("document.pdf", allowedExtensions)).isTrue();
        assertThat(ValidationUtils.hasValidExtension("image.JPG", allowedExtensions)).isTrue(); // case insensitive
        assertThat(ValidationUtils.hasValidExtension("file.DOCX", allowedExtensions)).isTrue();

        // Invalid extensions
        assertThat(ValidationUtils.hasValidExtension("script.exe", allowedExtensions)).isFalse();
        assertThat(ValidationUtils.hasValidExtension("file.txt", allowedExtensions)).isFalse();
        assertThat(ValidationUtils.hasValidExtension("noextension", allowedExtensions)).isFalse();
        assertThat(ValidationUtils.hasValidExtension("", allowedExtensions)).isFalse();
        assertThat(ValidationUtils.hasValidExtension(null, allowedExtensions)).isFalse();
    }

    @Test
    @DisplayName("Should validate custom regex patterns")
    void shouldValidateCustomRegexPatterns() {
        String alphanumericPattern = "^[a-zA-Z0-9]+$";
        
        // Valid alphanumeric
        assertThat(ValidationUtils.matchesPattern("abc123", alphanumericPattern)).isTrue();
        assertThat(ValidationUtils.matchesPattern("ABC", alphanumericPattern)).isTrue();
        assertThat(ValidationUtils.matchesPattern("123", alphanumericPattern)).isTrue();

        // Invalid alphanumeric
        assertThat(ValidationUtils.matchesPattern("abc-123", alphanumericPattern)).isFalse();
        assertThat(ValidationUtils.matchesPattern("abc 123", alphanumericPattern)).isFalse();
        assertThat(ValidationUtils.matchesPattern("", alphanumericPattern)).isFalse();
        assertThat(ValidationUtils.matchesPattern(null, alphanumericPattern)).isFalse();
    }

    // Test bean for Jakarta validation testing
    public static class TestBean {
        @NotNull
        @Email
        private String email;

        @NotNull
        @Size(min = 2, max = 50)
        private String name;

        @NotNull
        @Size(min = 8)
        private String password;

        public TestBean(String email, String name, String password) {
            this.email = email;
            this.name = name;
            this.password = password;
        }

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}