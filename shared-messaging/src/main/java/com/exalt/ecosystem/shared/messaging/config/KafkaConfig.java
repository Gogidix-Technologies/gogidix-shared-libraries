package com.exalt.ecosystem.shared.messaging.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the Exalt Social E-commerce Ecosystem messaging.
 * Provides production-ready Kafka producer and consumer configurations.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "messaging.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {
    
    /**
     * Kafka configuration properties.
     */
    @ConfigurationProperties(prefix = "messaging.kafka")
    public static class KafkaProperties {
        private String bootstrapServers = "localhost:9092";
        private String groupId = "exalt-ecosystem";
        private String clientId = "exalt-messaging";
        private boolean enableAutoCommit = false;
        private int sessionTimeoutMs = 30000;
        private int heartbeatIntervalMs = 3000;
        private int maxPollRecords = 500;
        private long maxPollIntervalMs = 300000;
        private String autoOffsetReset = "latest";
        private int retries = 3;
        private long retryBackoffMs = 1000;
        private int batchSize = 16384;
        private int lingerMs = 5;
        private long bufferMemory = 33554432;
        private String acks = "all";
        private boolean enableIdempotence = true;
        private int maxInFlightRequestsPerConnection = 5;
        private Security security = new Security();
        private Schema schema = new Schema();
        
        public static class Security {
            private String protocol = "PLAINTEXT";
            private String mechanism = "";
            private String username = "";
            private String password = "";
            private String keystore = "";
            private String keystorePassword = "";
            private String truststore = "";
            private String truststorePassword = "";
            
            // Getters and setters
            public String getProtocol() { return protocol; }
            public void setProtocol(String protocol) { this.protocol = protocol; }
            public String getMechanism() { return mechanism; }
            public void setMechanism(String mechanism) { this.mechanism = mechanism; }
            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }
            public String getKeystore() { return keystore; }
            public void setKeystore(String keystore) { this.keystore = keystore; }
            public String getKeystorePassword() { return keystorePassword; }
            public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
            public String getTruststore() { return truststore; }
            public void setTruststore(String truststore) { this.truststore = truststore; }
            public String getTruststorePassword() { return truststorePassword; }
            public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
        }
        
        public static class Schema {
            private String registryUrl = "";
            private String basicAuthUserInfo = "";
            private boolean autoRegisterSchemas = true;
            
            // Getters and setters
            public String getRegistryUrl() { return registryUrl; }
            public void setRegistryUrl(String registryUrl) { this.registryUrl = registryUrl; }
            public String getBasicAuthUserInfo() { return basicAuthUserInfo; }
            public void setBasicAuthUserInfo(String basicAuthUserInfo) { this.basicAuthUserInfo = basicAuthUserInfo; }
            public boolean isAutoRegisterSchemas() { return autoRegisterSchemas; }
            public void setAutoRegisterSchemas(boolean autoRegisterSchemas) { this.autoRegisterSchemas = autoRegisterSchemas; }
        }
        
        // Getters and setters
        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public boolean isEnableAutoCommit() { return enableAutoCommit; }
        public void setEnableAutoCommit(boolean enableAutoCommit) { this.enableAutoCommit = enableAutoCommit; }
        public int getSessionTimeoutMs() { return sessionTimeoutMs; }
        public void setSessionTimeoutMs(int sessionTimeoutMs) { this.sessionTimeoutMs = sessionTimeoutMs; }
        public int getHeartbeatIntervalMs() { return heartbeatIntervalMs; }
        public void setHeartbeatIntervalMs(int heartbeatIntervalMs) { this.heartbeatIntervalMs = heartbeatIntervalMs; }
        public int getMaxPollRecords() { return maxPollRecords; }
        public void setMaxPollRecords(int maxPollRecords) { this.maxPollRecords = maxPollRecords; }
        public long getMaxPollIntervalMs() { return maxPollIntervalMs; }
        public void setMaxPollIntervalMs(long maxPollIntervalMs) { this.maxPollIntervalMs = maxPollIntervalMs; }
        public String getAutoOffsetReset() { return autoOffsetReset; }
        public void setAutoOffsetReset(String autoOffsetReset) { this.autoOffsetReset = autoOffsetReset; }
        public int getRetries() { return retries; }
        public void setRetries(int retries) { this.retries = retries; }
        public long getRetryBackoffMs() { return retryBackoffMs; }
        public void setRetryBackoffMs(long retryBackoffMs) { this.retryBackoffMs = retryBackoffMs; }
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        public int getLingerMs() { return lingerMs; }
        public void setLingerMs(int lingerMs) { this.lingerMs = lingerMs; }
        public long getBufferMemory() { return bufferMemory; }
        public void setBufferMemory(long bufferMemory) { this.bufferMemory = bufferMemory; }
        public String getAcks() { return acks; }
        public void setAcks(String acks) { this.acks = acks; }
        public boolean isEnableIdempotence() { return enableIdempotence; }
        public void setEnableIdempotence(boolean enableIdempotence) { this.enableIdempotence = enableIdempotence; }
        public int getMaxInFlightRequestsPerConnection() { return maxInFlightRequestsPerConnection; }
        public void setMaxInFlightRequestsPerConnection(int maxInFlightRequestsPerConnection) { this.maxInFlightRequestsPerConnection = maxInFlightRequestsPerConnection; }
        public Security getSecurity() { return security; }
        public void setSecurity(Security security) { this.security = security; }
        public Schema getSchema() { return schema; }
        public void setSchema(Schema schema) { this.schema = schema; }
    }
    
    @Bean
    @ConfigurationProperties(prefix = "messaging.kafka")
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }
    
    /**
     * Kafka producer factory for string messages.
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory(KafkaProperties properties) {
        Map<String, Object> configProps = createProducerProperties(properties);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * Kafka producer factory for JSON messages.
     */
    @Bean
    public ProducerFactory<String, Object> jsonProducerFactory(KafkaProperties properties) {
        Map<String, Object> configProps = createProducerProperties(properties);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * Kafka template for string messages.
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    /**
     * Kafka template for JSON messages.
     */
    @Bean
    public KafkaTemplate<String, Object> jsonKafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    /**
     * Consumer factory for string messages.
     */
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory(KafkaProperties properties) {
        Map<String, Object> configProps = createConsumerProperties(properties);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Consumer factory for JSON messages.
     */
    @Bean
    public ConsumerFactory<String, Object> jsonConsumerFactory(KafkaProperties properties) {
        Map<String, Object> configProps = createConsumerProperties(properties);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.exalt.ecosystem.*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Kafka listener container factory for string messages.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
    
    /**
     * Kafka listener container factory for JSON messages.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> jsonKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
    
    /**
     * Creates common producer properties.
     */
    private Map<String, Object> createProducerProperties(KafkaProperties properties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, properties.getClientId());
        configProps.put(ProducerConfig.RETRIES_CONFIG, properties.getRetries());
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, properties.getRetryBackoffMs());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, properties.getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, properties.getBufferMemory());
        configProps.put(ProducerConfig.ACKS_CONFIG, properties.getAcks());
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, properties.isEnableIdempotence());
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, properties.getMaxInFlightRequestsPerConnection());
        
        // Add security properties if configured
        addSecurityProperties(configProps, properties.getSecurity());
        
        // Add schema registry properties if configured
        addSchemaRegistryProperties(configProps, properties.getSchema());
        
        return configProps;
    }
    
    /**
     * Creates common consumer properties.
     */
    private Map<String, Object> createConsumerProperties(KafkaProperties properties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getGroupId());
        configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, properties.getClientId());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, properties.getSessionTimeoutMs());
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, properties.getHeartbeatIntervalMs());
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, properties.getMaxPollIntervalMs());
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        
        // Add security properties if configured
        addSecurityProperties(configProps, properties.getSecurity());
        
        // Add schema registry properties if configured
        addSchemaRegistryProperties(configProps, properties.getSchema());
        
        return configProps;
    }
    
    /**
     * Adds security properties if configured.
     */
    private void addSecurityProperties(Map<String, Object> configProps, KafkaProperties.Security security) {
        if (!"PLAINTEXT".equals(security.getProtocol())) {
            configProps.put("security.protocol", security.getProtocol());
            
            if (!security.getMechanism().isEmpty()) {
                configProps.put("sasl.mechanism", security.getMechanism());
            }
            
            if (!security.getUsername().isEmpty() && !security.getPassword().isEmpty()) {
                configProps.put("sasl.jaas.config", 
                    String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                        security.getUsername(), security.getPassword()));
            }
            
            if (!security.getKeystore().isEmpty()) {
                configProps.put("ssl.keystore.location", security.getKeystore());
                configProps.put("ssl.keystore.password", security.getKeystorePassword());
            }
            
            if (!security.getTruststore().isEmpty()) {
                configProps.put("ssl.truststore.location", security.getTruststore());
                configProps.put("ssl.truststore.password", security.getTruststorePassword());
            }
        }
    }
    
    /**
     * Adds schema registry properties if configured.
     */
    private void addSchemaRegistryProperties(Map<String, Object> configProps, KafkaProperties.Schema schema) {
        if (!schema.getRegistryUrl().isEmpty()) {
            configProps.put("schema.registry.url", schema.getRegistryUrl());
            configProps.put("auto.register.schemas", schema.isAutoRegisterSchemas());
            
            if (!schema.getBasicAuthUserInfo().isEmpty()) {
                configProps.put("basic.auth.credentials.source", "USER_INFO");
                configProps.put("basic.auth.user.info", schema.getBasicAuthUserInfo());
            }
        }
    }
}