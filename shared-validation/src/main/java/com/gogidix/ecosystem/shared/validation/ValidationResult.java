package com.gogidix.ecosystem.shared.validation;

import java.util.*;

/**
 * Validation result container that holds validation errors and status.
 * Provides methods to manage and query validation results.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public class ValidationResult {
    
    private final Map<String, List<String>> fieldErrors;
    private final List<String> globalErrors;
    private boolean valid;
    
    /**
     * Default constructor.
     */
    public ValidationResult() {
        this.fieldErrors = new HashMap<>();
        this.globalErrors = new ArrayList<>();
        this.valid = true;
    }
    
    /**
     * Constructor with initial field errors.
     * 
     * @param fieldErrors Map of field errors
     */
    public ValidationResult(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = new HashMap<>(fieldErrors);
        this.globalErrors = new ArrayList<>();
        this.valid = fieldErrors.isEmpty();
    }
    
    /**
     * Adds a field-specific error.
     * 
     * @param field Field name
     * @param error Error message
     */
    public void addError(String field, String error) {
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
        this.valid = false;
    }
    
    /**
     * Adds multiple errors for a field.
     * 
     * @param field Field name
     * @param errors List of error messages
     */
    public void addErrors(String field, List<String> errors) {
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(errors);
        if (!errors.isEmpty()) {
            this.valid = false;
        }
    }
    
    /**
     * Adds a global error (not field-specific).
     * 
     * @param error Error message
     */
    public void addGlobalError(String error) {
        globalErrors.add(error);
        this.valid = false;
    }
    
    /**
     * Adds multiple global errors.
     * 
     * @param errors List of error messages
     */
    public void addGlobalErrors(List<String> errors) {
        globalErrors.addAll(errors);
        if (!errors.isEmpty()) {
            this.valid = false;
        }
    }
    
    /**
     * Merges another validation result into this one.
     * 
     * @param other Other validation result
     */
    public void merge(ValidationResult other) {
        for (Map.Entry<String, List<String>> entry : other.fieldErrors.entrySet()) {
            addErrors(entry.getKey(), entry.getValue());
        }
        addGlobalErrors(other.globalErrors);
    }
    
    /**
     * Checks if validation passed (no errors).
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return valid && fieldErrors.isEmpty() && globalErrors.isEmpty();
    }
    
    /**
     * Checks if validation failed (has errors).
     * 
     * @return true if invalid
     */
    public boolean hasErrors() {
        return !isValid();
    }
    
    /**
     * Checks if a specific field has errors.
     * 
     * @param field Field name
     * @return true if field has errors
     */
    public boolean hasFieldError(String field) {
        List<String> errors = fieldErrors.get(field);
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * Checks if there are global errors.
     * 
     * @return true if has global errors
     */
    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
    }
    
    /**
     * Gets errors for a specific field.
     * 
     * @param field Field name
     * @return List of error messages
     */
    public List<String> getFieldErrors(String field) {
        return fieldErrors.getOrDefault(field, Collections.emptyList());
    }
    
    /**
     * Gets first error for a specific field.
     * 
     * @param field Field name
     * @return First error message or null
     */
    public String getFirstFieldError(String field) {
        List<String> errors = getFieldErrors(field);
        return errors.isEmpty() ? null : errors.get(0);
    }
    
    /**
     * Gets all field errors.
     * 
     * @return Map of field names to error lists
     */
    public Map<String, List<String>> getAllFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
    
    /**
     * Gets all global errors.
     * 
     * @return List of global error messages
     */
    public List<String> getGlobalErrors() {
        return Collections.unmodifiableList(globalErrors);
    }
    
    /**
     * Gets first global error.
     * 
     * @return First global error message or null
     */
    public String getFirstGlobalError() {
        return globalErrors.isEmpty() ? null : globalErrors.get(0);
    }
    
    /**
     * Gets all errors (field and global) as a flat list.
     * 
     * @return List of all error messages
     */
    public List<String> getAllErrors() {
        List<String> allErrors = new ArrayList<>();
        
        for (List<String> errors : fieldErrors.values()) {
            allErrors.addAll(errors);
        }
        
        allErrors.addAll(globalErrors);
        return allErrors;
    }
    
    /**
     * Gets all field names that have errors.
     * 
     * @return Set of field names with errors
     */
    public Set<String> getErrorFields() {
        return Collections.unmodifiableSet(fieldErrors.keySet());
    }
    
    /**
     * Gets total number of errors.
     * 
     * @return Total error count
     */
    public int getErrorCount() {
        int count = globalErrors.size();
        for (List<String> errors : fieldErrors.values()) {
            count += errors.size();
        }
        return count;
    }
    
    /**
     * Gets number of fields with errors.
     * 
     * @return Number of fields with errors
     */
    public int getErrorFieldCount() {
        return fieldErrors.size();
    }
    
    /**
     * Clears all errors.
     */
    public void clear() {
        fieldErrors.clear();
        globalErrors.clear();
        this.valid = true;
    }
    
    /**
     * Clears errors for a specific field.
     * 
     * @param field Field name
     */
    public void clearFieldErrors(String field) {
        fieldErrors.remove(field);
        updateValidStatus();
    }
    
    /**
     * Clears all global errors.
     */
    public void clearGlobalErrors() {
        globalErrors.clear();
        updateValidStatus();
    }
    
    /**
     * Updates valid status based on current errors.
     */
    private void updateValidStatus() {
        this.valid = fieldErrors.isEmpty() && globalErrors.isEmpty();
    }
    
    /**
     * Converts to a formatted error string.
     * 
     * @return Formatted error string
     */
    public String toErrorString() {
        if (isValid()) {
            return "No errors";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Add field errors
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            String field = entry.getKey();
            for (String error : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(field).append(": ").append(error);
            }
        }
        
        // Add global errors
        for (String error : globalErrors) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(error);
        }
        
        return sb.toString();
    }
    
    /**
     * Converts to a simple error message list.
     * 
     * @return List of error messages with field prefixes
     */
    public List<String> toErrorMessages() {
        List<String> messages = new ArrayList<>();
        
        // Add field errors with field names
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            String field = entry.getKey();
            for (String error : entry.getValue()) {
                messages.add(field + ": " + error);
            }
        }
        
        // Add global errors
        messages.addAll(globalErrors);
        
        return messages;
    }
    
    /**
     * Creates a successful validation result.
     * 
     * @return Valid ValidationResult
     */
    public static ValidationResult success() {
        return new ValidationResult();
    }
    
    /**
     * Creates a validation result with a single field error.
     * 
     * @param field Field name
     * @param error Error message
     * @return ValidationResult with error
     */
    public static ValidationResult fieldError(String field, String error) {
        ValidationResult result = new ValidationResult();
        result.addError(field, error);
        return result;
    }
    
    /**
     * Creates a validation result with a single global error.
     * 
     * @param error Error message
     * @return ValidationResult with error
     */
    public static ValidationResult globalError(String error) {
        ValidationResult result = new ValidationResult();
        result.addGlobalError(error);
        return result;
    }
    
    /**
     * Creates a validation result from field errors map.
     * 
     * @param fieldErrors Map of field errors
     * @return ValidationResult with errors
     */
    public static ValidationResult fromFieldErrors(Map<String, List<String>> fieldErrors) {
        return new ValidationResult(fieldErrors);
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", fieldErrors=" + fieldErrors +
                ", globalErrors=" + globalErrors +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationResult)) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid &&
               Objects.equals(fieldErrors, that.fieldErrors) &&
               Objects.equals(globalErrors, that.globalErrors);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fieldErrors, globalErrors, valid);
    }
}