package com.exalt.ecosystem.shared.testing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * Comprehensive test container configuration for the Exalt Social E-commerce Ecosystem.
 * Provides containerized external dependencies for integration testing.
 * 
 * <p>This configuration provides the following test containers:</p>
 * <ul>
 *   <li>PostgreSQL database for relational data testing</li>
 *   <li>Redis for caching and session testing</li>
 *   <li>Apache Kafka for message broker testing</li>
 *   <li>RabbitMQ for alternative message broker testing</li>
 *   <li>MongoDB for document database testing</li>
 *   <li>Elasticsearch for search and analytics testing</li>
 * </ul>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * {@code
 * @SpringBootTest
 * @Import(TestContainerConfig.class)
 * @Testcontainers
 * class MyIntegrationTest {
 *     // Test containers are automatically started and configured
 * }
 * }
 * </pre>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Slf4j
@TestConfiguration
public class TestContainerConfig {
    
    // Database versions for consistency
    private static final String POSTGRES_VERSION = "15.4-alpine";
    private static final String REDIS_VERSION = "7.2-alpine";
    private static final String KAFKA_VERSION = "7.4.0";
    private static final String RABBITMQ_VERSION = "3.12-management";
    private static final String MONGO_VERSION = "6.0";
    private static final String ELASTICSEARCH_VERSION = "8.9.2";
    
    /**
     * PostgreSQL test container for relational database testing.
     * Configured with optimal settings for integration tests.
     */
    @Bean
    public static PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:" + POSTGRES_VERSION)
        )
        .withDatabaseName("exalt_test_db")
        .withUsername("test_user")
        .withPassword("test_password")
        .withInitScript("test-data.sql")
        .withCommand("postgres", "-c", "fsync=off", "-c", "synchronous_commit=off")
        .withStartupTimeout(Duration.ofMinutes(3))
        .waitingFor(Wait.forListeningPort());
        
        postgres.start();
        
        log.info("PostgreSQL test container started: {}:{}", 
                postgres.getHost(), postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
        
        return postgres;
    }
    
    /**
     * Redis test container for caching and session testing.
     * Configured for high-performance testing scenarios.
     */
    @Bean
    public static GenericContainer<?> redisContainer() {
        GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:" + REDIS_VERSION)
        )
        .withExposedPorts(6379)
        .withCommand("redis-server", "--requirepass", "test_password", "--appendonly", "no")
        .withStartupTimeout(Duration.ofMinutes(2))
        .waitingFor(Wait.forListeningPort());
        
        redis.start();
        
        log.info("Redis test container started: {}:{}", 
                redis.getHost(), redis.getMappedPort(6379));
        
        return redis;
    }
    
    /**
     * Apache Kafka test container for message broker testing.
     * Includes Zookeeper and optimized for test scenarios.
     */
    @Bean
    public static KafkaContainer kafkaContainer() {
        KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:" + KAFKA_VERSION)
        )
        .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
        .withEnv("KAFKA_NUM_PARTITIONS", "3")
        .withEnv("KAFKA_DEFAULT_REPLICATION_FACTOR", "1")
        .withStartupTimeout(Duration.ofMinutes(3))
        .waitingFor(Wait.forListeningPort());
        
        kafka.start();
        
        log.info("Kafka test container started: {}", kafka.getBootstrapServers());
        
        return kafka;
    }
    
    /**
     * RabbitMQ test container for alternative message broker testing.
     * Includes management plugin for testing and debugging.
     */
    @Bean
    public static RabbitMQContainer rabbitMQContainer() {
        RabbitMQContainer rabbitmq = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:" + RABBITMQ_VERSION)
        )
        .withUser("test_user", "test_password")
        .withVhost("test_vhost")
        .withPermission("test_vhost", "test_user", ".*", ".*", ".*")
        .withStartupTimeout(Duration.ofMinutes(3))
        .waitingFor(Wait.forListeningPort());
        
        rabbitmq.start();
        
        log.info("RabbitMQ test container started: {}:{}", 
                rabbitmq.getHost(), rabbitmq.getMappedPort(5672));
        log.info("RabbitMQ Management UI: http://{}:{}", 
                rabbitmq.getHost(), rabbitmq.getMappedPort(15672));
        
        return rabbitmq;
    }
    
    /**
     * MongoDB test container for document database testing.
     * Optimized for development and testing scenarios.
     */
    @Bean
    public static MongoDBContainer mongoContainer() {
        MongoDBContainer mongo = new MongoDBContainer(
            DockerImageName.parse("mongo:" + MONGO_VERSION)
        )
        .withStartupTimeout(Duration.ofMinutes(3))
        .waitingFor(Wait.forListeningPort());
        
        mongo.start();
        
        log.info("MongoDB test container started: {}", mongo.getReplicaSetUrl());
        
        return mongo;
    }
    
    /**
     * Elasticsearch test container for search and analytics testing.
     * Configured with security disabled for easier testing.
     */
    @Bean
    public static ElasticsearchContainer elasticsearchContainer() {
        ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:" + ELASTICSEARCH_VERSION)
        )
        .withEnv("discovery.type", "single-node")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
        .withStartupTimeout(Duration.ofMinutes(3))
        .waitingFor(Wait.forHttp("/").forPort(9200));
        
        elasticsearch.start();
        
        log.info("Elasticsearch test container started: http://{}:{}", 
                elasticsearch.getHost(), elasticsearch.getMappedPort(9200));
        
        return elasticsearch;
    }
    
    /**
     * Dynamic property configuration for test containers.
     * Automatically configures Spring properties based on container endpoints.
     * 
     * @param registry Spring's dynamic property registry
     */
    @DynamicPropertySource
    public static void configureTestProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        PostgreSQLContainer<?> postgres = postgresContainer();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        
        // Redis configuration
        GenericContainer<?> redis = redisContainer();
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.redis.password", () -> "test_password");
        registry.add("spring.redis.timeout", () -> "2000ms");
        
        // Kafka configuration
        KafkaContainer kafka = kafkaContainer();
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        
        // RabbitMQ configuration
        RabbitMQContainer rabbitmq = rabbitMQContainer();
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitmq.getMappedPort(5672));
        registry.add("spring.rabbitmq.username", () -> "test_user");
        registry.add("spring.rabbitmq.password", () -> "test_password");
        registry.add("spring.rabbitmq.virtual-host", () -> "test_vhost");
        
        // MongoDB configuration
        MongoDBContainer mongo = mongoContainer();
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "test_db");
        
        // Elasticsearch configuration
        ElasticsearchContainer elasticsearch = elasticsearchContainer();
        registry.add("spring.elasticsearch.uris", () -> 
            "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
        
        log.info("Test container properties configured successfully");
    }
    
    /**
     * Test-specific DataSource configuration for scenarios where custom DB setup is needed.
     * 
     * @return configured test DataSource
     */
    @Bean
    @Primary
    public DataSource testDataSource() {
        PostgreSQLContainer<?> postgres = postgresContainer();
        
        org.springframework.boot.jdbc.DataSourceBuilder<?> dataSourceBuilder = 
            org.springframework.boot.jdbc.DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword());
        
        log.info("Test DataSource configured for PostgreSQL container");
        
        return dataSourceBuilder.build();
    }
    
    /**
     * Utility methods for container management
     */
    
    /**
     * Waits for all containers to be healthy and ready.
     * Useful for coordinating test execution.
     */
    public static void waitForContainersReady() {
        log.info("Waiting for all test containers to be ready...");
        
        // Verify PostgreSQL
        PostgreSQLContainer<?> postgres = postgresContainer();
        if (!postgres.isRunning()) {
            throw new IllegalStateException("PostgreSQL container is not running");
        }
        
        // Verify Redis
        GenericContainer<?> redis = redisContainer();
        if (!redis.isRunning()) {
            throw new IllegalStateException("Redis container is not running");
        }
        
        // Verify Kafka
        KafkaContainer kafka = kafkaContainer();
        if (!kafka.isRunning()) {
            throw new IllegalStateException("Kafka container is not running");
        }
        
        log.info("All test containers are ready and healthy");
    }
    
    /**
     * Gets connection details for all containers.
     * Useful for debugging and manual testing.
     * 
     * @return map of container connection details
     */
    public static java.util.Map<String, String> getContainerDetails() {
        java.util.Map<String, String> details = new java.util.HashMap<>();
        
        PostgreSQLContainer<?> postgres = postgresContainer();
        details.put("postgres.url", postgres.getJdbcUrl());
        details.put("postgres.host", postgres.getHost() + ":" + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
        
        GenericContainer<?> redis = redisContainer();
        details.put("redis.host", redis.getHost() + ":" + redis.getMappedPort(6379));
        
        KafkaContainer kafka = kafkaContainer();
        details.put("kafka.servers", kafka.getBootstrapServers());
        
        RabbitMQContainer rabbitmq = rabbitMQContainer();
        details.put("rabbitmq.host", rabbitmq.getHost() + ":" + rabbitmq.getMappedPort(5672));
        details.put("rabbitmq.management", "http://" + rabbitmq.getHost() + ":" + rabbitmq.getMappedPort(15672));
        
        MongoDBContainer mongo = mongoContainer();
        details.put("mongo.uri", mongo.getReplicaSetUrl());
        
        ElasticsearchContainer elasticsearch = elasticsearchContainer();
        details.put("elasticsearch.url", "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
        
        return details;
    }
}