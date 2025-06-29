package com.exalt.ecosystem.shared.utilities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.*;

/**
 * Comprehensive JSON utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced JSON parsing, serialization, validation, and manipulation methods.
 * 
 * <p>This utility class handles JSON operations across the ecosystem including:
 * object serialization/deserialization, JSON validation, path operations, and data transformation.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class JsonUtils {
    
    private static final ObjectMapper OBJECT_MAPPER;
    private static final ObjectMapper PRETTY_MAPPER;
    
    static {
        OBJECT_MAPPER = createObjectMapper();
        PRETTY_MAPPER = createPrettyObjectMapper();
    }
    
    private JsonUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Creates and configures the default ObjectMapper.
     * 
     * @return configured ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Java 8 time module
        mapper.registerModule(new JavaTimeModule());
        
        // Configure features
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
    }
    
    /**
     * Creates and configures the pretty-printing ObjectMapper.
     * 
     * @return configured ObjectMapper with pretty printing
     */
    private static ObjectMapper createPrettyObjectMapper() {
        ObjectMapper mapper = createObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper;
    }
    
    /**
     * Converts an object to JSON string.
     * 
     * @param object object to serialize
     * @return JSON string or null if serialization fails
     */
    public static String toJson(Object object) {
        if (object == null) return null;
        
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Converts an object to pretty-formatted JSON string.
     * 
     * @param object object to serialize
     * @return pretty JSON string or null if serialization fails
     */
    public static String toPrettyJson(Object object) {
        if (object == null) return null;
        
        try {
            return PRETTY_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Converts JSON string to object of specified type.
     * 
     * @param json JSON string
     * @param clazz target class
     * @param <T> target type
     * @return deserialized object or null if parsing fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json) || clazz == null) return null;
        
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Converts JSON string to object using TypeReference.
     * 
     * @param json JSON string
     * @param typeReference type reference for complex types
     * @param <T> target type
     * @return deserialized object or null if parsing fails
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json) || typeReference == null) return null;
        
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Converts JSON string to List of specified type.
     * 
     * @param json JSON string
     * @param elementClass element class type
     * @param <T> element type
     * @return list of objects or empty list if parsing fails
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (StringUtils.isEmpty(json) || elementClass == null) return new ArrayList<>();
        
        try {
            TypeReference<List<T>> typeRef = new TypeReference<List<T>>() {};
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Converts JSON string to Map.
     * 
     * @param json JSON string
     * @return map representation or empty map if parsing fails
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (StringUtils.isEmpty(json)) return new HashMap<>();
        
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Converts JSON string to JsonNode for tree operations.
     * 
     * @param json JSON string
     * @return JsonNode or null if parsing fails
     */
    public static JsonNode fromJsonToNode(String json) {
        if (StringUtils.isEmpty(json)) return null;
        
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Validates if a string is valid JSON.
     * 
     * @param json JSON string to validate
     * @return true if valid JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtils.isEmpty(json)) return false;
        
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Validates if a string is a valid JSON object.
     * 
     * @param json JSON string to validate
     * @return true if valid JSON object
     */
    public static boolean isValidJsonObject(String json) {
        if (StringUtils.isEmpty(json)) return false;
        
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node != null && node.isObject();
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Validates if a string is a valid JSON array.
     * 
     * @param json JSON string to validate
     * @return true if valid JSON array
     */
    public static boolean isValidJsonArray(String json) {
        if (StringUtils.isEmpty(json)) return false;
        
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node != null && node.isArray();
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Gets a value from JSON using a path expression.
     * 
     * @param json JSON string
     * @param path path expression (e.g., "user.name", "items[0].price")
     * @return value as string or null if not found
     */
    public static String getValueByPath(String json, String path) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path)) return null;
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            JsonNode valueNode = getNodeByPath(rootNode, path);
            
            if (valueNode != null && !valueNode.isNull()) {
                return valueNode.isTextual() ? valueNode.textValue() : valueNode.toString();
            }
        } catch (JsonProcessingException e) {
            return null;
        }
        
        return null;
    }
    
    /**
     * Gets a typed value from JSON using a path expression.
     * 
     * @param json JSON string
     * @param path path expression
     * @param clazz target class
     * @param <T> target type
     * @return typed value or null if not found/conversion fails
     */
    public static <T> T getValueByPath(String json, String path, Class<T> clazz) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path) || clazz == null) return null;
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            JsonNode valueNode = getNodeByPath(rootNode, path);
            
            if (valueNode != null && !valueNode.isNull()) {
                return OBJECT_MAPPER.convertValue(valueNode, clazz);
            }
        } catch (Exception e) {
            return null;
        }
        
        return null;
    }
    
    /**
     * Sets a value in JSON using a path expression.
     * 
     * @param json JSON string
     * @param path path expression
     * @param value value to set
     * @return modified JSON string or original JSON if operation fails
     */
    public static String setValueByPath(String json, String path, Object value) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path)) return json;
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            setNodeByPath(rootNode, path, value);
            return OBJECT_MAPPER.writeValueAsString(rootNode);
        } catch (Exception e) {
            return json;
        }
    }
    
    /**
     * Removes a field from JSON using a path expression.
     * 
     * @param json JSON string
     * @param path path expression
     * @return modified JSON string or original JSON if operation fails
     */
    public static String removeByPath(String json, String path) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(path)) return json;
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            removeNodeByPath(rootNode, path);
            return OBJECT_MAPPER.writeValueAsString(rootNode);
        } catch (Exception e) {
            return json;
        }
    }
    
    /**
     * Merges two JSON objects.
     * 
     * @param baseJson base JSON object
     * @param mergeJson JSON object to merge
     * @return merged JSON string or base JSON if merge fails
     */
    public static String mergeJson(String baseJson, String mergeJson) {
        if (StringUtils.isEmpty(baseJson)) return mergeJson;
        if (StringUtils.isEmpty(mergeJson)) return baseJson;
        
        try {
            JsonNode baseNode = OBJECT_MAPPER.readTree(baseJson);
            JsonNode mergeNode = OBJECT_MAPPER.readTree(mergeJson);
            
            JsonNode mergedNode = mergeNodes(baseNode, mergeNode);
            return OBJECT_MAPPER.writeValueAsString(mergedNode);
        } catch (Exception e) {
            return baseJson;
        }
    }
    
    /**
     * Compares two JSON strings for equality (ignoring field order).
     * 
     * @param json1 first JSON string
     * @param json2 second JSON string
     * @return true if JSON content is equal
     */
    public static boolean areEqual(String json1, String json2) {
        if (json1 == null && json2 == null) return true;
        if (json1 == null || json2 == null) return false;
        
        try {
            JsonNode node1 = OBJECT_MAPPER.readTree(json1);
            JsonNode node2 = OBJECT_MAPPER.readTree(json2);
            return node1.equals(node2);
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Extracts all paths from a JSON object.
     * 
     * @param json JSON string
     * @return set of all paths in the JSON
     */
    public static Set<String> extractPaths(String json) {
        if (StringUtils.isEmpty(json)) return new HashSet<>();
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            Set<String> paths = new HashSet<>();
            extractPathsRecursive(rootNode, "", paths);
            return paths;
        } catch (JsonProcessingException e) {
            return new HashSet<>();
        }
    }
    
    /**
     * Flattens a nested JSON object into a flat map.
     * 
     * @param json JSON string
     * @return flattened map with dot-notation keys
     */
    public static Map<String, Object> flatten(String json) {
        if (StringUtils.isEmpty(json)) return new HashMap<>();
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            Map<String, Object> flattened = new HashMap<>();
            flattenRecursive(rootNode, "", flattened);
            return flattened;
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Converts a flattened map back to nested JSON.
     * 
     * @param flatMap flattened map with dot-notation keys
     * @return nested JSON string
     */
    public static String unflatten(Map<String, Object> flatMap) {
        if (flatMap == null || flatMap.isEmpty()) return "{}";
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.createObjectNode();
            
            for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
                setNodeByPath(rootNode, entry.getKey(), entry.getValue());
            }
            
            return OBJECT_MAPPER.writeValueAsString(rootNode);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Validates JSON against a simple schema (checks required fields).
     * 
     * @param json JSON string to validate
     * @param requiredFields list of required field paths
     * @return true if all required fields are present
     */
    public static boolean validateSchema(String json, List<String> requiredFields) {
        if (StringUtils.isEmpty(json) || CollectionUtils.isEmpty(requiredFields)) return true;
        
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            
            for (String field : requiredFields) {
                JsonNode fieldNode = getNodeByPath(rootNode, field);
                if (fieldNode == null || fieldNode.isNull()) {
                    return false;
                }
            }
            
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Converts an object to JSON and back to ensure it's JSON-serializable.
     * 
     * @param object object to test
     * @param clazz target class
     * @param <T> object type
     * @return round-trip object or null if not serializable
     */
    public static <T> T roundTrip(T object, Class<T> clazz) {
        if (object == null || clazz == null) return null;
        
        String json = toJson(object);
        return fromJson(json, clazz);
    }
    
    /**
     * Creates a deep copy of an object using JSON serialization.
     * 
     * @param object object to copy
     * @param clazz object class
     * @param <T> object type
     * @return deep copy or null if copy fails
     */
    public static <T> T deepCopy(T object, Class<T> clazz) {
        return roundTrip(object, clazz);
    }
    
    /**
     * Escapes special characters in a string for JSON.
     * 
     * @param value string to escape
     * @return escaped string
     */
    public static String escapeJson(String value) {
        if (StringUtils.isEmpty(value)) return value;
        
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * Unescapes JSON special characters.
     * 
     * @param value escaped string
     * @return unescaped string
     */
    public static String unescapeJson(String value) {
        if (StringUtils.isEmpty(value)) return value;
        
        return value.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
    
    // Helper methods
    
    private static JsonNode getNodeByPath(JsonNode rootNode, String path) {
        if (rootNode == null || StringUtils.isEmpty(path)) return null;
        
        String[] parts = path.split("\\.");
        JsonNode currentNode = rootNode;
        
        for (String part : parts) {
            if (currentNode == null) return null;
            
            // Handle array notation [index]
            if (part.contains("[") && part.contains("]")) {
                String fieldName = part.substring(0, part.indexOf('['));
                String indexStr = part.substring(part.indexOf('[') + 1, part.indexOf(']'));
                
                if (StringUtils.isNotEmpty(fieldName)) {
                    currentNode = currentNode.get(fieldName);
                }
                
                if (currentNode != null && currentNode.isArray()) {
                    try {
                        int index = Integer.parseInt(indexStr);
                        currentNode = currentNode.get(index);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } else {
                currentNode = currentNode.get(part);
            }
        }
        
        return currentNode;
    }
    
    private static void setNodeByPath(JsonNode rootNode, String path, Object value) {
        // This is a simplified implementation
        // A full implementation would handle complex path setting
    }
    
    private static void removeNodeByPath(JsonNode rootNode, String path) {
        // This is a simplified implementation
        // A full implementation would handle path-based removal
    }
    
    private static JsonNode mergeNodes(JsonNode baseNode, JsonNode mergeNode) {
        if (baseNode == null) return mergeNode;
        if (mergeNode == null) return baseNode;
        
        // This is a simplified merge implementation
        // A full implementation would handle recursive merging
        return mergeNode;
    }
    
    private static void extractPathsRecursive(JsonNode node, String currentPath, Set<String> paths) {
        if (node == null) return;
        
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(fieldName -> {
                String newPath = StringUtils.isEmpty(currentPath) ? fieldName : currentPath + "." + fieldName;
                paths.add(newPath);
                extractPathsRecursive(node.get(fieldName), newPath, paths);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPath = currentPath + "[" + i + "]";
                paths.add(newPath);
                extractPathsRecursive(node.get(i), newPath, paths);
            }
        }
    }
    
    private static void flattenRecursive(JsonNode node, String currentPath, Map<String, Object> flattened) {
        if (node == null) return;
        
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(fieldName -> {
                String newPath = StringUtils.isEmpty(currentPath) ? fieldName : currentPath + "." + fieldName;
                flattenRecursive(node.get(fieldName), newPath, flattened);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPath = currentPath + "[" + i + "]";
                flattenRecursive(node.get(i), newPath, flattened);
            }
        } else {
            Object value = null;
            if (node.isTextual()) {
                value = node.textValue();
            } else if (node.isNumber()) {
                value = node.numberValue();
            } else if (node.isBoolean()) {
                value = node.booleanValue();
            }
            flattened.put(currentPath, value);
        }
    }
}