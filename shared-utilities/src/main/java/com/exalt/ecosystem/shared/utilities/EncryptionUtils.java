package com.exalt.ecosystem.shared.utilities;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;

/**
 * Comprehensive encryption and security utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced encryption, hashing, and security methods with industry-standard algorithms.
 * 
 * <p>This utility class handles security operations across the ecosystem including:
 * password hashing, data encryption/decryption, secure random generation, and data validation.</p>
 * 
 * <p><strong>Security Note:</strong> This class uses industry-standard algorithms and practices.
 * All sensitive operations use SecureRandom and appropriate key sizes.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class EncryptionUtils {
    
    // Algorithm constants
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_SIZE = 12;
    private static final int GCM_TAG_SIZE = 16;
    
    // Hash algorithm constants
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String SHA512_ALGORITHM = "SHA-512";
    private static final String MD5_ALGORITHM = "MD5";
    
    // Salt and iteration constants
    private static final int SALT_SIZE = 32;
    private static final int PBKDF2_ITERATIONS = 100000;
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    private EncryptionUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Generates a cryptographically secure random salt.
     * 
     * @return base64-encoded salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Generates a cryptographically secure random salt with specified size.
     * 
     * @param size salt size in bytes
     * @return base64-encoded salt
     */
    public static String generateSalt(int size) {
        if (size <= 0) size = SALT_SIZE;
        
        byte[] salt = new byte[size];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a password using SHA-256 with salt.
     * 
     * @param password plain text password
     * @param salt salt value
     * @return hashed password as base64 string
     */
    public static String hashPassword(String password, String salt) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(salt)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            
            digest.update(saltBytes);
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Hashes a password using SHA-512 with salt for extra security.
     * 
     * @param password plain text password
     * @param salt salt value
     * @return hashed password as base64 string
     */
    public static String hashPasswordStrong(String password, String salt) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(salt)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA512_ALGORITHM);
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            
            digest.update(saltBytes);
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Verifies a password against its hash.
     * 
     * @param password plain text password to verify
     * @param hashedPassword stored hashed password
     * @param salt salt used for hashing
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(hashedPassword) || StringUtils.isEmpty(salt)) {
            return false;
        }
        
        String newHash = hashPassword(password, salt);
        return constantTimeEquals(hashedPassword, newHash);
    }
    
    /**
     * Verifies a password against its strong hash (SHA-512).
     * 
     * @param password plain text password to verify
     * @param hashedPassword stored hashed password
     * @param salt salt used for hashing
     * @return true if password matches
     */
    public static boolean verifyPasswordStrong(String password, String hashedPassword, String salt) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(hashedPassword) || StringUtils.isEmpty(salt)) {
            return false;
        }
        
        String newHash = hashPasswordStrong(password, salt);
        return constantTimeEquals(hashedPassword, newHash);
    }
    
    /**
     * Performs constant-time string comparison to prevent timing attacks.
     * 
     * @param a first string
     * @param b second string
     * @return true if strings are equal
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        
        return MessageDigest.isEqual(aBytes, bBytes);
    }
    
    /**
     * Generates a secure AES key.
     * 
     * @return base64-encoded AES key
     */
    public static String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Encrypts data using AES-GCM encryption.
     * 
     * @param plainText data to encrypt
     * @param base64Key base64-encoded AES key
     * @return encrypted data as base64 string (IV + encrypted data + tag)
     */
    public static String encryptAES(String plainText, String base64Key) {
        if (StringUtils.isEmpty(plainText) || StringUtils.isEmpty(base64Key)) {
            return null;
        }
        
        try {
            // Decode the key
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_SIZE];
            SECURE_RANDOM.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_SIZE * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV + encrypted data
            byte[] result = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Decrypts data using AES-GCM decryption.
     * 
     * @param encryptedData base64-encoded encrypted data (IV + encrypted data + tag)
     * @param base64Key base64-encoded AES key
     * @return decrypted plain text or null if decryption fails
     */
    public static String decryptAES(String encryptedData, String base64Key) {
        if (StringUtils.isEmpty(encryptedData) || StringUtils.isEmpty(base64Key)) {
            return null;
        }
        
        try {
            // Decode the encrypted data
            byte[] combinedData = Base64.getDecoder().decode(encryptedData);
            
            if (combinedData.length < GCM_IV_SIZE + GCM_TAG_SIZE) {
                return null; // Invalid data length
            }
            
            // Extract IV and encrypted data
            byte[] iv = Arrays.copyOfRange(combinedData, 0, GCM_IV_SIZE);
            byte[] encrypted = Arrays.copyOfRange(combinedData, GCM_IV_SIZE, combinedData.length);
            
            // Decode the key
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_SIZE * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Encrypts sensitive data for database storage.
     * 
     * @param data data to encrypt
     * @param key encryption key
     * @return encrypted data or null if encryption fails
     */
    public static String encryptForStorage(String data, String key) {
        return encryptAES(data, key);
    }
    
    /**
     * Decrypts sensitive data from database storage.
     * 
     * @param encryptedData encrypted data
     * @param key decryption key
     * @return decrypted data or null if decryption fails
     */
    public static String decryptFromStorage(String encryptedData, String key) {
        return decryptAES(encryptedData, key);
    }
    
    /**
     * Computes SHA-256 hash of a string.
     * 
     * @param input input string
     * @return SHA-256 hash as hex string
     */
    public static String sha256(String input) {
        return hash(input, SHA256_ALGORITHM);
    }
    
    /**
     * Computes SHA-512 hash of a string.
     * 
     * @param input input string
     * @return SHA-512 hash as hex string
     */
    public static String sha512(String input) {
        return hash(input, SHA512_ALGORITHM);
    }
    
    /**
     * Computes MD5 hash of a string (for legacy compatibility only).
     * 
     * @param input input string
     * @return MD5 hash as hex string
     * @deprecated MD5 is cryptographically broken, use SHA-256 instead
     */
    @Deprecated
    public static String md5(String input) {
        return hash(input, MD5_ALGORITHM);
    }
    
    /**
     * Computes hash of a string using specified algorithm.
     * 
     * @param input input string
     * @param algorithm hash algorithm
     * @return hash as hex string
     */
    public static String hash(String input, String algorithm) {
        if (StringUtils.isEmpty(input) || StringUtils.isEmpty(algorithm)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    /**
     * Generates a cryptographically secure random token.
     * 
     * @param length token length in bytes
     * @return base64-encoded random token
     */
    public static String generateSecureToken(int length) {
        if (length <= 0) length = 32;
        
        byte[] token = new byte[length];
        SECURE_RANDOM.nextBytes(token);
        return Base64.getEncoder().encodeToString(token);
    }
    
    /**
     * Generates a cryptographically secure random token with default length.
     * 
     * @return base64-encoded random token (32 bytes)
     */
    public static String generateSecureToken() {
        return generateSecureToken(32);
    }
    
    /**
     * Generates a secure alphanumeric token.
     * 
     * @param length token length
     * @return alphanumeric token
     */
    public static String generateAlphanumericToken(int length) {
        if (length <= 0) length = 16;
        
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(charset.length());
            token.append(charset.charAt(index));
        }
        
        return token.toString();
    }
    
    /**
     * Generates a secure numeric token.
     * 
     * @param length token length
     * @return numeric token
     */
    public static String generateNumericToken(int length) {
        if (length <= 0) length = 6;
        
        StringBuilder token = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            token.append(SECURE_RANDOM.nextInt(10));
        }
        
        return token.toString();
    }
    
    /**
     * Masks sensitive data for logging purposes.
     * 
     * @param sensitiveData data to mask
     * @param visibleChars number of characters to keep visible
     * @return masked data
     */
    public static String maskSensitiveData(String sensitiveData, int visibleChars) {
        if (StringUtils.isEmpty(sensitiveData)) return sensitiveData;
        if (visibleChars <= 0) return StringUtils.repeat("*", sensitiveData.length());
        if (visibleChars >= sensitiveData.length()) return sensitiveData;
        
        String visible = sensitiveData.substring(0, visibleChars);
        String masked = StringUtils.repeat("*", sensitiveData.length() - visibleChars);
        
        return visible + masked;
    }
    
    /**
     * Masks sensitive data showing only first and last characters.
     * 
     * @param sensitiveData data to mask
     * @param keepStart number of characters to keep at start
     * @param keepEnd number of characters to keep at end
     * @return masked data
     */
    public static String maskSensitiveData(String sensitiveData, int keepStart, int keepEnd) {
        if (StringUtils.isEmpty(sensitiveData)) return sensitiveData;
        if (keepStart + keepEnd >= sensitiveData.length()) return sensitiveData;
        
        String start = sensitiveData.substring(0, keepStart);
        String end = sensitiveData.substring(sensitiveData.length() - keepEnd);
        String masked = StringUtils.repeat("*", sensitiveData.length() - keepStart - keepEnd);
        
        return start + masked + end;
    }
    
    /**
     * Validates data integrity using checksum.
     * 
     * @param data data to validate
     * @param expectedChecksum expected checksum
     * @return true if data integrity is valid
     */
    public static boolean validateDataIntegrity(String data, String expectedChecksum) {
        if (StringUtils.isEmpty(data) || StringUtils.isEmpty(expectedChecksum)) {
            return false;
        }
        
        String actualChecksum = sha256(data);
        return constantTimeEquals(expectedChecksum, actualChecksum);
    }
    
    /**
     * Computes checksum for data integrity verification.
     * 
     * @param data data to compute checksum for
     * @return SHA-256 checksum
     */
    public static String computeChecksum(String data) {
        return sha256(data);
    }
    
    /**
     * Securely wipes sensitive data from memory (best effort).
     * 
     * @param sensitiveArray character array containing sensitive data
     */
    public static void wipeSensitiveData(char[] sensitiveArray) {
        if (sensitiveArray != null) {
            Arrays.fill(sensitiveArray, '\0');
        }
    }
    
    /**
     * Securely wipes sensitive data from memory (best effort).
     * 
     * @param sensitiveArray byte array containing sensitive data
     */
    public static void wipeSensitiveData(byte[] sensitiveArray) {
        if (sensitiveArray != null) {
            Arrays.fill(sensitiveArray, (byte) 0);
        }
    }
    
    /**
     * Validates password strength based on common criteria.
     * 
     * @param password password to validate
     * @return password strength score (0-100)
     */
    public static int getPasswordStrength(String password) {
        if (StringUtils.isEmpty(password)) return 0;
        
        int score = 0;
        
        // Length score (up to 25 points)
        if (password.length() >= 8) score += 10;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 5;
        
        // Character variety (up to 40 points)
        if (password.matches(".*[a-z].*")) score += 10; // Lowercase
        if (password.matches(".*[A-Z].*")) score += 10; // Uppercase
        if (password.matches(".*[0-9].*")) score += 10; // Numbers
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 10; // Special chars
        
        // Complexity bonus (up to 35 points)
        if (password.length() > 20) score += 5;
        if (!password.toLowerCase().contains("password")) score += 5;
        if (!password.toLowerCase().contains("123456")) score += 5;
        if (!hasRepeatingChars(password)) score += 10;
        if (!hasSequentialChars(password)) score += 10;
        
        return Math.min(score, 100);
    }
    
    /**
     * Checks if password contains repeating characters.
     * 
     * @param password password to check
     * @return true if password has repeating characters
     */
    private static boolean hasRepeatingChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c = password.charAt(i);
            if (password.charAt(i + 1) == c && password.charAt(i + 2) == c) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if password contains sequential characters.
     * 
     * @param password password to check
     * @return true if password has sequential characters
     */
    private static boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();
        String[] sequences = {"abc", "bcd", "cde", "def", "efg", "fgh", "ghi", "hij",
                             "ijk", "jkl", "klm", "lmn", "mno", "nop", "opq", "pqr",
                             "qrs", "rst", "stu", "tuv", "uvw", "vwx", "wxy", "xyz",
                             "123", "234", "345", "456", "567", "678", "789"};
        
        for (String seq : sequences) {
            if (lower.contains(seq)) {
                return true;
            }
        }
        
        return false;
    }
}