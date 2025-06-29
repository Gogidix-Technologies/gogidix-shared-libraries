package com.exalt.ecosystem.shared.utilities;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.security.SecureRandom;

/**
 * Comprehensive string utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced string manipulation, validation, formatting, and generation methods.
 * 
 * <p>This utility class handles common string operations across the ecosystem including:
 * validation, formatting, cleaning, conversion, and generation operations.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class StringUtils {
    
    // Common patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[1-9]\\d{1,14}$"
    );
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d*\\.?\\d+$");
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    
    // Character sets for random generation
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String ALPHANUMERIC_CHARS = UPPERCASE_CHARS + LOWERCASE_CHARS + NUMERIC_CHARS;
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private StringUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Checks if a string is null or empty.
     * 
     * @param str string to check
     * @return true if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Checks if a string is not null and not empty.
     * 
     * @param str string to check
     * @return true if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * Checks if a string is null, empty, or contains only whitespace.
     * 
     * @param str string to check
     * @return true if string is blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not blank (not null, not empty, not only whitespace).
     * 
     * @param str string to check
     * @return true if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * Returns the string if not null, otherwise returns empty string.
     * 
     * @param str string to check
     * @return string or empty string if null
     */
    public static String defaultString(String str) {
        return str != null ? str : "";
    }
    
    /**
     * Returns the string if not null, otherwise returns the default value.
     * 
     * @param str string to check
     * @param defaultValue default value if string is null
     * @return string or default value if null
     */
    public static String defaultString(String str, String defaultValue) {
        return str != null ? str : defaultValue;
    }
    
    /**
     * Returns the string if not blank, otherwise returns the default value.
     * 
     * @param str string to check
     * @param defaultValue default value if string is blank
     * @return string or default value if blank
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isNotBlank(str) ? str : defaultValue;
    }
    
    /**
     * Safely trims a string, handling null values.
     * 
     * @param str string to trim
     * @return trimmed string or null if input was null
     */
    public static String trim(String str) {
        return str != null ? str.trim() : null;
    }
    
    /**
     * Safely trims a string to empty, handling null values.
     * 
     * @param str string to trim
     * @return trimmed string or empty string if input was null
     */
    public static String trimToEmpty(String str) {
        return str != null ? str.trim() : "";
    }
    
    /**
     * Safely trims a string to null if result would be empty.
     * 
     * @param str string to trim
     * @return trimmed string or null if result is empty
     */
    public static String trimToNull(String str) {
        String trimmed = trim(str);
        return isEmpty(trimmed) ? null : trimmed;
    }
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str string to capitalize
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Converts a string to camelCase.
     * 
     * @param str string to convert
     * @return camelCase string
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) return str;
        
        String[] words = str.toLowerCase().split("[\\s_-]+");
        StringBuilder result = new StringBuilder(words[0]);
        
        for (int i = 1; i < words.length; i++) {
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }
    
    /**
     * Converts a string to PascalCase.
     * 
     * @param str string to convert
     * @return PascalCase string
     */
    public static String toPascalCase(String str) {
        if (isEmpty(str)) return str;
        
        return Arrays.stream(str.toLowerCase().split("[\\s_-]+"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }
    
    /**
     * Converts a string to snake_case.
     * 
     * @param str string to convert
     * @return snake_case string
     */
    public static String toSnakeCase(String str) {
        if (isEmpty(str)) return str;
        
        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("[\\s-]+", "_")
                .toLowerCase();
    }
    
    /**
     * Converts a string to kebab-case.
     * 
     * @param str string to convert
     * @return kebab-case string
     */
    public static String toKebabCase(String str) {
        if (isEmpty(str)) return str;
        
        return str.replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("[\\s_]+", "-")
                .toLowerCase();
    }
    
    /**
     * Reverses a string.
     * 
     * @param str string to reverse
     * @return reversed string
     */
    public static String reverse(String str) {
        if (isEmpty(str)) return str;
        return new StringBuilder(str).reverse().toString();
    }
    
    /**
     * Truncates a string to the specified length.
     * 
     * @param str string to truncate
     * @param maxLength maximum length
     * @return truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || maxLength < 0) return str;
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
    
    /**
     * Truncates a string to the specified length and adds ellipsis.
     * 
     * @param str string to truncate
     * @param maxLength maximum length (including ellipsis)
     * @return truncated string with ellipsis
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (isEmpty(str) || maxLength < 4) return truncate(str, maxLength);
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Pads a string on the left with the specified character.
     * 
     * @param str string to pad
     * @param size total size after padding
     * @param padChar character to pad with
     * @return left-padded string
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) str = "";
        int padSize = size - str.length();
        if (padSize <= 0) return str;
        
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < padSize; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }
    
    /**
     * Pads a string on the right with the specified character.
     * 
     * @param str string to pad
     * @param size total size after padding
     * @param padChar character to pad with
     * @return right-padded string
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) str = "";
        int padSize = size - str.length();
        if (padSize <= 0) return str;
        
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        for (int i = 0; i < padSize; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }
    
    /**
     * Centers a string within the specified size, padding with spaces.
     * 
     * @param str string to center
     * @param size total size after padding
     * @return centered string
     */
    public static String center(String str, int size) {
        return center(str, size, ' ');
    }
    
    /**
     * Centers a string within the specified size, padding with the specified character.
     * 
     * @param str string to center
     * @param size total size after padding
     * @param padChar character to pad with
     * @return centered string
     */
    public static String center(String str, int size, char padChar) {
        if (str == null) str = "";
        int padSize = size - str.length();
        if (padSize <= 0) return str;
        
        int leftPad = padSize / 2;
        int rightPad = padSize - leftPad;
        
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < leftPad; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        for (int i = 0; i < rightPad; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }
    
    /**
     * Repeats a string the specified number of times.
     * 
     * @param str string to repeat
     * @param repeat number of repetitions
     * @return repeated string
     */
    public static String repeat(String str, int repeat) {
        if (str == null || repeat <= 0) return "";
        
        StringBuilder sb = new StringBuilder(str.length() * repeat);
        for (int i = 0; i < repeat; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * Removes all whitespace from a string.
     * 
     * @param str string to process
     * @return string without whitespace
     */
    public static String removeWhitespace(String str) {
        if (isEmpty(str)) return str;
        return WHITESPACE_PATTERN.matcher(str).replaceAll("");
    }
    
    /**
     * Normalizes whitespace in a string (multiple spaces become single space).
     * 
     * @param str string to normalize
     * @return normalized string
     */
    public static String normalizeWhitespace(String str) {
        if (isEmpty(str)) return str;
        return WHITESPACE_PATTERN.matcher(str.trim()).replaceAll(" ");
    }
    
    /**
     * Removes accents and diacritical marks from a string.
     * 
     * @param str string to process
     * @return string without accents
     */
    public static String removeAccents(String str) {
        if (isEmpty(str)) return str;
        
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    
    /**
     * Converts a string to a URL-friendly slug.
     * 
     * @param str string to convert
     * @return URL-friendly slug
     */
    public static String toSlug(String str) {
        if (isEmpty(str)) return str;
        
        return removeAccents(str)
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
    
    /**
     * Validates if a string is a valid email address.
     * 
     * @param email email string to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates if a string is a valid phone number.
     * 
     * @param phone phone string to validate
     * @return true if valid phone format
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) return false;
        String cleaned = phone.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    /**
     * Validates if a string contains only alphanumeric characters.
     * 
     * @param str string to validate
     * @return true if alphanumeric
     */
    public static boolean isAlphanumeric(String str) {
        return isNotBlank(str) && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }
    
    /**
     * Validates if a string contains only numeric characters.
     * 
     * @param str string to validate
     * @return true if numeric
     */
    public static boolean isNumeric(String str) {
        return isNotBlank(str) && NUMERIC_PATTERN.matcher(str).matches();
    }
    
    /**
     * Validates if a string is a valid decimal number.
     * 
     * @param str string to validate
     * @return true if valid decimal
     */
    public static boolean isDecimal(String str) {
        return isNotBlank(str) && DECIMAL_PATTERN.matcher(str).matches();
    }
    
    /**
     * Validates if a string is a valid UUID.
     * 
     * @param str string to validate
     * @return true if valid UUID format
     */
    public static boolean isValidUuid(String str) {
        return isNotBlank(str) && UUID_PATTERN.matcher(str).matches();
    }
    
    /**
     * Counts the number of occurrences of a character in a string.
     * 
     * @param str string to search
     * @param ch character to count
     * @return number of occurrences
     */
    public static int countOccurrences(String str, char ch) {
        if (isEmpty(str)) return 0;
        
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Counts the number of occurrences of a substring in a string.
     * 
     * @param str string to search
     * @param substring substring to count
     * @return number of occurrences
     */
    public static int countOccurrences(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) return 0;
        
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
    
    /**
     * Generates a random string of the specified length using alphanumeric characters.
     * 
     * @param length length of the string to generate
     * @return random alphanumeric string
     */
    public static String generateRandomString(int length) {
        return generateRandomString(length, ALPHANUMERIC_CHARS);
    }
    
    /**
     * Generates a random string of the specified length using the given character set.
     * 
     * @param length length of the string to generate
     * @param charset character set to use
     * @return random string
     */
    public static String generateRandomString(int length, String charset) {
        if (length <= 0 || isEmpty(charset)) return "";
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(charset.length());
            sb.append(charset.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * Generates a random password with the specified length and complexity.
     * 
     * @param length length of the password
     * @param includeSpecialChars whether to include special characters
     * @return random password
     */
    public static String generateRandomPassword(int length, boolean includeSpecialChars) {
        if (length < 4) throw new IllegalArgumentException("Password length must be at least 4");
        
        String charset = ALPHANUMERIC_CHARS;
        if (includeSpecialChars) {
            charset += SPECIAL_CHARS;
        }
        
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one character from each required type
        password.append(UPPERCASE_CHARS.charAt(RANDOM.nextInt(UPPERCASE_CHARS.length())));
        password.append(LOWERCASE_CHARS.charAt(RANDOM.nextInt(LOWERCASE_CHARS.length())));
        password.append(NUMERIC_CHARS.charAt(RANDOM.nextInt(NUMERIC_CHARS.length())));
        
        if (includeSpecialChars && length > 3) {
            password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));
        }
        
        // Fill the rest with random characters
        int remainingLength = length - password.length();
        for (int i = 0; i < remainingLength; i++) {
            password.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }
        
        // Shuffle the password characters
        return shuffleString(password.toString());
    }
    
    /**
     * Shuffles the characters in a string randomly.
     * 
     * @param str string to shuffle
     * @return shuffled string
     */
    public static String shuffleString(String str) {
        if (isEmpty(str)) return str;
        
        List<Character> characters = str.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        
        Collections.shuffle(characters, RANDOM);
        
        return characters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    
    /**
     * Masks a string by replacing characters with asterisks, leaving some visible.
     * 
     * @param str string to mask
     * @param visibleStart number of characters to keep visible at start
     * @param visibleEnd number of characters to keep visible at end
     * @return masked string
     */
    public static String mask(String str, int visibleStart, int visibleEnd) {
        if (isEmpty(str)) return str;
        if (visibleStart + visibleEnd >= str.length()) return str;
        
        String start = str.substring(0, visibleStart);
        String end = str.substring(str.length() - visibleEnd);
        String middle = repeat("*", str.length() - visibleStart - visibleEnd);
        
        return start + middle + end;
    }
    
    /**
     * Masks an email address for privacy.
     * 
     * @param email email to mask
     * @return masked email (e.g., j***@example.com)
     */
    public static String maskEmail(String email) {
        if (!isValidEmail(email)) return email;
        
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return repeat("*", localPart.length()) + domain;
        }
        
        return localPart.charAt(0) + repeat("*", localPart.length() - 2) + 
               localPart.charAt(localPart.length() - 1) + domain;
    }
    
    /**
     * Masks a phone number for privacy.
     * 
     * @param phone phone number to mask
     * @return masked phone number
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone)) return phone;
        
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (cleaned.length() < 4) return repeat("*", phone.length());
        
        return mask(phone, 2, 2);
    }
    
    /**
     * Converts a string to title case (first letter of each word capitalized).
     * 
     * @param str string to convert
     * @return title case string
     */
    public static String toTitleCase(String str) {
        if (isEmpty(str)) return str;
        
        return Arrays.stream(str.toLowerCase().split("\\s+"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }
    
    /**
     * Checks if a string contains only ASCII characters.
     * 
     * @param str string to check
     * @return true if string contains only ASCII characters
     */
    public static boolean isAscii(String str) {
        if (isEmpty(str)) return true;
        return str.chars().allMatch(c -> c < 128);
    }
    
    /**
     * Gets the length of a string, handling null values.
     * 
     * @param str string to measure
     * @return length of string or 0 if null
     */
    public static int length(String str) {
        return str != null ? str.length() : 0;
    }
    
    /**
     * Joins a collection of strings with the specified delimiter.
     * 
     * @param collection collection of strings
     * @param delimiter delimiter to use
     * @return joined string
     */
    public static String join(Collection<String> collection, String delimiter) {
        if (collection == null || collection.isEmpty()) return "";
        return String.join(delimiter != null ? delimiter : "", collection);
    }
    
    /**
     * Joins an array of strings with the specified delimiter.
     * 
     * @param array array of strings
     * @param delimiter delimiter to use
     * @return joined string
     */
    public static String join(String[] array, String delimiter) {
        if (array == null || array.length == 0) return "";
        return String.join(delimiter != null ? delimiter : "", array);
    }
    
    /**
     * Splits a string and trims each part.
     * 
     * @param str string to split
     * @param delimiter delimiter to split on
     * @return array of trimmed strings
     */
    public static String[] splitAndTrim(String str, String delimiter) {
        if (isEmpty(str)) return new String[0];
        
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
    
    /**
     * Abbreviates a string by replacing the middle part with ellipsis.
     * 
     * @param str string to abbreviate
     * @param maxLength maximum length of result
     * @return abbreviated string
     */
    public static String abbreviate(String str, int maxLength) {
        if (isEmpty(str) || maxLength < 4) return truncate(str, maxLength);
        if (str.length() <= maxLength) return str;
        
        int ellipsisLength = 3; // Length of "..."
        int sideLength = (maxLength - ellipsisLength) / 2;
        int remainingLength = maxLength - ellipsisLength - sideLength;
        
        return str.substring(0, sideLength) + "..." + 
               str.substring(str.length() - remainingLength);
    }
}