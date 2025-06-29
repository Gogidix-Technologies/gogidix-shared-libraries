package com.exalt.ecosystem.shared.utilities;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Comprehensive file utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced file operations, validation, reading, writing, and manipulation methods.
 * 
 * <p>This utility class handles common file operations across the ecosystem including:
 * file reading/writing, validation, copying, moving, and security operations.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class FileUtils {
    
    // Common file extensions
    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
    public static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    public static final Set<String> ARCHIVE_EXTENSIONS = Set.of("zip", "rar", "7z", "tar", "gz", "bz2");
    public static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm");
    public static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "flac", "aac", "ogg", "wma");
    
    // Safe file size limits (in bytes)
    public static final long MAX_FILE_SIZE_MB = 100 * 1024 * 1024; // 100MB
    public static final long MAX_IMAGE_SIZE_MB = 10 * 1024 * 1024;  // 10MB
    public static final long MAX_DOCUMENT_SIZE_MB = 50 * 1024 * 1024; // 50MB
    
    private FileUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Checks if a file exists.
     * 
     * @param filePath file path to check
     * @return true if file exists
     */
    public static boolean exists(String filePath) {
        return filePath != null && Files.exists(Paths.get(filePath));
    }
    
    /**
     * Checks if a path is a directory.
     * 
     * @param path path to check
     * @return true if path is a directory
     */
    public static boolean isDirectory(String path) {
        return path != null && Files.isDirectory(Paths.get(path));
    }
    
    /**
     * Checks if a path is a regular file.
     * 
     * @param path path to check
     * @return true if path is a regular file
     */
    public static boolean isFile(String path) {
        return path != null && Files.isRegularFile(Paths.get(path));
    }
    
    /**
     * Gets the file extension from a filename.
     * 
     * @param filename filename to process
     * @return file extension (without dot) or empty string
     */
    public static String getExtension(String filename) {
        if (StringUtils.isEmpty(filename)) return "";
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";
        
        return filename.substring(lastDot + 1).toLowerCase();
    }
    
    /**
     * Gets the filename without extension.
     * 
     * @param filename filename to process
     * @return filename without extension
     */
    public static String getNameWithoutExtension(String filename) {
        if (StringUtils.isEmpty(filename)) return "";
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return filename;
        
        return filename.substring(0, lastDot);
    }
    
    /**
     * Gets the parent directory path from a file path.
     * 
     * @param filePath file path
     * @return parent directory path or null
     */
    public static String getParentPath(String filePath) {
        if (StringUtils.isEmpty(filePath)) return null;
        
        Path path = Paths.get(filePath).getParent();
        return path != null ? path.toString() : null;
    }
    
    /**
     * Gets the filename from a file path.
     * 
     * @param filePath file path
     * @return filename or empty string
     */
    public static String getFilename(String filePath) {
        if (StringUtils.isEmpty(filePath)) return "";
        
        Path path = Paths.get(filePath).getFileName();
        return path != null ? path.toString() : "";
    }
    
    /**
     * Gets the file size in bytes.
     * 
     * @param filePath file path
     * @return file size in bytes or -1 if file doesn't exist
     */
    public static long getFileSize(String filePath) {
        if (!exists(filePath)) return -1;
        
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return -1;
        }
    }
    
    /**
     * Gets the file size in a human-readable format.
     * 
     * @param filePath file path
     * @return human-readable file size or "Unknown"
     */
    public static String getFileSizeFormatted(String filePath) {
        long size = getFileSize(filePath);
        if (size == -1) return "Unknown";
        
        return formatFileSize(size);
    }
    
    /**
     * Formats a file size in bytes to human-readable format.
     * 
     * @param size size in bytes
     * @return formatted size string
     */
    public static String formatFileSize(long size) {
        if (size < 0) return "Unknown";
        if (size == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }
    
    /**
     * Reads the entire content of a file as a string.
     * 
     * @param filePath file path to read
     * @return file content as string or null if error
     */
    public static String readFileToString(String filePath) {
        if (!exists(filePath)) return null;
        
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Reads all lines from a file.
     * 
     * @param filePath file path to read
     * @return list of lines or empty list if error
     */
    public static List<String> readLines(String filePath) {
        if (!exists(filePath)) return new ArrayList<>();
        
        try {
            return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Reads a file as bytes.
     * 
     * @param filePath file path to read
     * @return file content as byte array or null if error
     */
    public static byte[] readFileToBytes(String filePath) {
        if (!exists(filePath)) return null;
        
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Writes a string to a file.
     * 
     * @param filePath file path to write to
     * @param content content to write
     * @return true if successful
     */
    public static boolean writeStringToFile(String filePath, String content) {
        if (StringUtils.isEmpty(filePath) || content == null) return false;
        
        try {
            // Create parent directories if they don't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Writes lines to a file.
     * 
     * @param filePath file path to write to
     * @param lines lines to write
     * @return true if successful
     */
    public static boolean writeLinesToFile(String filePath, List<String> lines) {
        if (StringUtils.isEmpty(filePath) || lines == null) return false;
        
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            Files.write(path, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Writes bytes to a file.
     * 
     * @param filePath file path to write to
     * @param data bytes to write
     * @return true if successful
     */
    public static boolean writeBytesToFile(String filePath, byte[] data) {
        if (StringUtils.isEmpty(filePath) || data == null) return false;
        
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            Files.write(path, data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Appends a string to a file.
     * 
     * @param filePath file path to append to
     * @param content content to append
     * @return true if successful
     */
    public static boolean appendToFile(String filePath, String content) {
        if (StringUtils.isEmpty(filePath) || content == null) return false;
        
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            Files.writeString(path, content, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Copies a file to another location.
     * 
     * @param sourcePath source file path
     * @param targetPath target file path
     * @return true if successful
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        if (!exists(sourcePath) || StringUtils.isEmpty(targetPath)) return false;
        
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Moves a file to another location.
     * 
     * @param sourcePath source file path
     * @param targetPath target file path
     * @return true if successful
     */
    public static boolean moveFile(String sourcePath, String targetPath) {
        if (!exists(sourcePath) || StringUtils.isEmpty(targetPath)) return false;
        
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Deletes a file.
     * 
     * @param filePath file path to delete
     * @return true if successful
     */
    public static boolean deleteFile(String filePath) {
        if (!exists(filePath)) return true; // Already deleted
        
        try {
            Files.delete(Paths.get(filePath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Creates a directory (including parent directories).
     * 
     * @param directoryPath directory path to create
     * @return true if successful
     */
    public static boolean createDirectory(String directoryPath) {
        if (StringUtils.isEmpty(directoryPath)) return false;
        
        try {
            Files.createDirectories(Paths.get(directoryPath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Lists files in a directory.
     * 
     * @param directoryPath directory path
     * @return list of file names or empty list
     */
    public static List<String> listFiles(String directoryPath) {
        if (!isDirectory(directoryPath)) return new ArrayList<>();
        
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Lists all files in a directory recursively.
     * 
     * @param directoryPath directory path
     * @return list of file paths or empty list
     */
    public static List<String> listAllFiles(String directoryPath) {
        if (!isDirectory(directoryPath)) return new ArrayList<>();
        
        try (Stream<Path> stream = Files.walk(Paths.get(directoryPath))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Lists directories in a directory.
     * 
     * @param directoryPath directory path
     * @return list of directory names or empty list
     */
    public static List<String> listDirectories(String directoryPath) {
        if (!isDirectory(directoryPath)) return new ArrayList<>();
        
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Validates if a filename is safe (no path traversal).
     * 
     * @param filename filename to validate
     * @return true if filename is safe
     */
    public static boolean isSafeFilename(String filename) {
        if (StringUtils.isEmpty(filename)) return false;
        
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        
        // Check for reserved characters
        String reservedChars = "<>:\"|?*";
        for (char c : reservedChars.toCharArray()) {
            if (filename.indexOf(c) != -1) {
                return false;
            }
        }
        
        // Check for reserved names (Windows)
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                 "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                 "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        
        String nameWithoutExt = getNameWithoutExtension(filename).toUpperCase();
        for (String reserved : reservedNames) {
            if (reserved.equals(nameWithoutExt)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates if a file extension is allowed.
     * 
     * @param filename filename to check
     * @param allowedExtensions set of allowed extensions
     * @return true if extension is allowed
     */
    public static boolean isAllowedExtension(String filename, Set<String> allowedExtensions) {
        if (StringUtils.isEmpty(filename) || allowedExtensions == null) return false;
        
        String extension = getExtension(filename);
        return allowedExtensions.contains(extension.toLowerCase());
    }
    
    /**
     * Checks if a file is an image based on extension.
     * 
     * @param filename filename to check
     * @return true if file is an image
     */
    public static boolean isImage(String filename) {
        return isAllowedExtension(filename, IMAGE_EXTENSIONS);
    }
    
    /**
     * Checks if a file is a document based on extension.
     * 
     * @param filename filename to check
     * @return true if file is a document
     */
    public static boolean isDocument(String filename) {
        return isAllowedExtension(filename, DOCUMENT_EXTENSIONS);
    }
    
    /**
     * Checks if a file is an archive based on extension.
     * 
     * @param filename filename to check
     * @return true if file is an archive
     */
    public static boolean isArchive(String filename) {
        return isAllowedExtension(filename, ARCHIVE_EXTENSIONS);
    }
    
    /**
     * Validates file size against limits.
     * 
     * @param filePath file path
     * @param maxSizeBytes maximum allowed size in bytes
     * @return true if file size is within limits
     */
    public static boolean isValidFileSize(String filePath, long maxSizeBytes) {
        long size = getFileSize(filePath);
        return size != -1 && size <= maxSizeBytes;
    }
    
    /**
     * Validates image file size.
     * 
     * @param filePath file path
     * @return true if image size is within limits
     */
    public static boolean isValidImageSize(String filePath) {
        return isValidFileSize(filePath, MAX_IMAGE_SIZE_MB);
    }
    
    /**
     * Validates document file size.
     * 
     * @param filePath file path
     * @return true if document size is within limits
     */
    public static boolean isValidDocumentSize(String filePath) {
        return isValidFileSize(filePath, MAX_DOCUMENT_SIZE_MB);
    }
    
    /**
     * Calculates MD5 hash of a file.
     * 
     * @param filePath file path
     * @return MD5 hash as hex string or null if error
     */
    public static String calculateMD5(String filePath) {
        return calculateHash(filePath, "MD5");
    }
    
    /**
     * Calculates SHA-256 hash of a file.
     * 
     * @param filePath file path
     * @return SHA-256 hash as hex string or null if error
     */
    public static String calculateSHA256(String filePath) {
        return calculateHash(filePath, "SHA-256");
    }
    
    /**
     * Calculates hash of a file using specified algorithm.
     * 
     * @param filePath file path
     * @param algorithm hash algorithm (MD5, SHA-1, SHA-256, etc.)
     * @return hash as hex string or null if error
     */
    public static String calculateHash(String filePath, String algorithm) {
        if (!exists(filePath) || StringUtils.isEmpty(algorithm)) return null;
        
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] fileBytes = readFileToBytes(filePath);
            if (fileBytes == null) return null;
            
            byte[] hashBytes = digest.digest(fileBytes);
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
     * Gets a temporary file path with the specified extension.
     * 
     * @param extension file extension (without dot)
     * @return temporary file path
     */
    public static String getTempFilePath(String extension) {
        try {
            String fileName = "temp_" + System.currentTimeMillis();
            if (StringUtils.isNotEmpty(extension)) {
                fileName += "." + extension;
            }
            return Files.createTempFile("exalt_", fileName).toString();
        } catch (IOException e) {
            return System.getProperty("java.io.tmpdir") + File.separator + 
                   "exalt_temp_" + System.currentTimeMillis() + 
                   (StringUtils.isNotEmpty(extension) ? "." + extension : "");
        }
    }
    
    /**
     * Cleans up temporary files older than specified age.
     * 
     * @param tempDir temporary directory path
     * @param maxAgeHours maximum age in hours
     * @return number of files cleaned up
     */
    public static int cleanupTempFiles(String tempDir, int maxAgeHours) {
        if (!isDirectory(tempDir) || maxAgeHours < 0) return 0;
        
        long cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60L * 60L * 1000L);
        int cleanedCount = 0;
        
        try (Stream<Path> stream = Files.list(Paths.get(tempDir))) {
            List<Path> oldFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            for (Path file : oldFiles) {
                try {
                    Files.delete(file);
                    cleanedCount++;
                } catch (IOException e) {
                    // Continue with next file
                }
            }
        } catch (IOException e) {
            // Return what we managed to clean
        }
        
        return cleanedCount;
    }
    
    /**
     * Generates a unique filename by appending a number if file already exists.
     * 
     * @param directoryPath directory path
     * @param baseFilename base filename
     * @return unique filename
     */
    public static String generateUniqueFilename(String directoryPath, String baseFilename) {
        if (StringUtils.isEmpty(directoryPath) || StringUtils.isEmpty(baseFilename)) {
            return baseFilename;
        }
        
        String name = getNameWithoutExtension(baseFilename);
        String extension = getExtension(baseFilename);
        String fullPath = Paths.get(directoryPath, baseFilename).toString();
        
        if (!exists(fullPath)) {
            return baseFilename;
        }
        
        int counter = 1;
        String newFilename;
        
        do {
            newFilename = name + "_" + counter;
            if (StringUtils.isNotEmpty(extension)) {
                newFilename += "." + extension;
            }
            fullPath = Paths.get(directoryPath, newFilename).toString();
            counter++;
        } while (exists(fullPath) && counter < 1000); // Prevent infinite loop
        
        return newFilename;
    }
}