package com.gogidix.ecosystem.shared.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic audit logging in the Exalt Social E-commerce Ecosystem.
 * When applied to a method, it triggers automatic creation and logging of audit events.
 * 
 * <p>This annotation can be used at the method level to automatically capture audit information
 * about business operations without manual audit logging code.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Auditable(
 *     action = AuditEvent.AuditAction.CREATE,
 *     resourceType = "Product",
 *     description = "Create new product in catalog",
 *     category = "PRODUCT_MANAGEMENT"
 * )
 * public Product createProduct(CreateProductRequest request) {
 *     // Method implementation
 *     return product;
 * }
 * }
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * The audit action being performed.
     * This is required and defines what type of operation is being audited.
     * 
     * @return the audit action
     */
    AuditEvent.AuditAction action();
    
    /**
     * The type of resource being operated on.
     * This helps categorize audit events by the domain object type.
     * 
     * <p>Examples: "User", "Product", "Order", "Payment", "Warehouse", "Shipment"</p>
     * 
     * @return the resource type
     */
    String resourceType() default "";
    
    /**
     * Expression to extract the resource ID from method parameters or return value.
     * Supports SpEL (Spring Expression Language) expressions.
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "#request.productId"} - Extract from method parameter</li>
     *   <li>{@code "#result.id"} - Extract from return value</li>
     *   <li>{@code "#user.userId"} - Extract from parameter object</li>
     * </ul>
     * 
     * @return SpEL expression to extract resource ID
     */
    String resourceIdExpression() default "";
    
    /**
     * Expression to extract the user ID from method parameters or security context.
     * Supports SpEL expressions and can access security context.
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "#request.userId"} - Extract from method parameter</li>
     *   <li>{@code "authentication.name"} - Extract from security context</li>
     *   <li>{@code "#userId"} - Direct parameter reference</li>
     * </ul>
     * 
     * @return SpEL expression to extract user ID
     */
    String userIdExpression() default "authentication?.name";
    
    /**
     * Description of the operation being audited.
     * This provides human-readable context for the audit event.
     * 
     * @return description of the operation
     */
    String description() default "";
    
    /**
     * Category for the audit event to help with classification and filtering.
     * 
     * <p>Common categories:</p>
     * <ul>
     *   <li>AUTHENTICATION</li>
     *   <li>AUTHORIZATION</li>
     *   <li>DATA_ACCESS</li>
     *   <li>BUSINESS_PROCESS</li>
     *   <li>CONFIGURATION</li>
     *   <li>SECURITY</li>
     *   <li>COMPLIANCE</li>
     * </ul>
     * 
     * @return the audit category
     */
    String category() default "";
    
    /**
     * Severity level for the audit event.
     * Defaults to INFO for normal operations.
     * 
     * @return the audit severity level
     */
    AuditEvent.AuditSeverity severity() default AuditEvent.AuditSeverity.INFO;
    
    /**
     * Whether to include method parameters in the audit metadata.
     * When true, method parameters are serialized and included in the audit event.
     * 
     * <p><strong>Warning:</strong> Be careful with sensitive data. Parameters containing
     * passwords, tokens, or PII should not be logged.</p>
     * 
     * @return true to include parameters in audit metadata
     */
    boolean includeParameters() default false;
    
    /**
     * Whether to include the method return value in the audit metadata.
     * When true, the return value is serialized and included in the audit event.
     * 
     * <p><strong>Warning:</strong> Be careful with sensitive data. Return values containing
     * sensitive information should not be logged.</p>
     * 
     * @return true to include return value in audit metadata
     */
    boolean includeReturnValue() default false;
    
    /**
     * List of parameter names to exclude from audit logging.
     * Use this to prevent sensitive parameters from being logged when includeParameters is true.
     * 
     * <p>Examples: {"password", "token", "creditCard", "ssn"}</p>
     * 
     * @return array of parameter names to exclude
     */
    String[] excludeParameters() default {};
    
    /**
     * Whether to audit the operation asynchronously.
     * When true, audit logging is performed in a separate thread to avoid impacting performance.
     * 
     * @return true for asynchronous audit logging
     */
    boolean async() default true;
    
    /**
     * Whether to audit only successful operations or all operations (including failures).
     * When true, audit events are only created for successful method executions.
     * When false, audit events are created for both successful and failed executions.
     * 
     * @return true to audit only successful operations
     */
    boolean auditOnSuccessOnly() default false;
    
    /**
     * Custom tags to be applied to the audit event.
     * Tags help with categorization and filtering of audit events.
     * 
     * <p>Format: "key1=value1,key2=value2"</p>
     * <p>Example: "module=payments,criticality=high"</p>
     * 
     * @return comma-separated key=value pairs for tags
     */
    String tags() default "";
    
    /**
     * Compliance types that this audit event relates to.
     * Used for compliance reporting and retention policies.
     * 
     * <p>Examples: {"GDPR", "PCI_DSS", "SOX", "HIPAA"}</p>
     * 
     * @return array of compliance types
     */
    String[] complianceTypes() default {};
    
    /**
     * Custom retention period in days for this audit event.
     * Overrides the default retention policy for compliance or business requirements.
     * 
     * <p>Use -1 to use the default retention policy.</p>
     * <p>Use 0 for permanent retention (not recommended).</p>
     * 
     * @return retention period in days, or -1 for default
     */
    int retentionDays() default -1;
    
    /**
     * Condition expression that determines whether to create an audit event.
     * The audit event is only created if this SpEL expression evaluates to true.
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "#request.amount > 1000"} - Only audit high-value transactions</li>
     *   <li>{@code "#user.role == 'ADMIN'"} - Only audit admin operations</li>
     *   <li>{@code "true"} - Always audit (default behavior)</li>
     * </ul>
     * 
     * @return SpEL expression for conditional auditing
     */
    String condition() default "";
    
    /**
     * Whether this is a security-sensitive operation that requires special handling.
     * Security-sensitive operations are logged synchronously and with higher priority.
     * 
     * @return true if this is a security-sensitive operation
     */
    boolean securitySensitive() default false;
    
    /**
     * Expression to extract old values for UPDATE operations.
     * This is typically used to capture the state before an update operation.
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "@productService.findById(#request.productId)"} - Call service method</li>
     *   <li>{@code "#existingProduct"} - Reference existing object parameter</li>
     * </ul>
     * 
     * @return SpEL expression to extract old values
     */
    String oldValuesExpression() default "";
    
    /**
     * Expression to extract new values for CREATE/UPDATE operations.
     * This is typically used to capture the state after a create or update operation.
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "#result"} - Use the method return value</li>
     *   <li>{@code "#request"} - Use the request object</li>
     * </ul>
     * 
     * @return SpEL expression to extract new values
     */
    String newValuesExpression() default "";
    
    /**
     * Custom metadata expressions to include in the audit event.
     * Each expression should evaluate to a Map&lt;String, Object&gt; or individual values.
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "{'ip': #request.remoteAddr, 'userAgent': #request.userAgent}"}</li>
     *   <li>{@code "@metadataService.getContextData()"}</li>
     * </ul>
     * 
     * @return array of SpEL expressions for custom metadata
     */
    String[] metadataExpressions() default {};
}