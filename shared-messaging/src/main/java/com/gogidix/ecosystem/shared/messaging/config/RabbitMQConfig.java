package com.gogidix.ecosystem.shared.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * RabbitMQ configuration for the Exalt Social E-commerce Ecosystem messaging.
 * Provides production-ready RabbitMQ producer and consumer configurations.
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "messaging.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConfig {
    
    /**
     * RabbitMQ configuration properties.
     */
    @ConfigurationProperties(prefix = "messaging.rabbitmq")
    public static class RabbitMQProperties {
        private String host = "localhost";
        private int port = 5672;
        private String username = "guest";
        private String password = "guest";
        private String virtualHost = "/";
        private boolean ssl = false;
        private int connectionTimeout = 60000;
        private int requestedHeartbeat = 60;
        private int channelCacheSize = 25;
        private boolean publisherConfirms = true;
        private boolean publisherReturns = true;
        private boolean mandatory = true;
        private Exchange exchange = new Exchange();
        private Queue queue = new Queue();
        private DeadLetter deadLetter = new DeadLetter();
        private Retry retry = new Retry();
        
        public static class Exchange {
            private String name = "exalt.ecosystem.events";
            private String type = "topic";
            private boolean durable = true;
            private boolean autoDelete = false;
            
            // Getters and setters
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public boolean isDurable() { return durable; }
            public void setDurable(boolean durable) { this.durable = durable; }
            public boolean isAutoDelete() { return autoDelete; }
            public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }
        }
        
        public static class Queue {
            private String prefix = "exalt.ecosystem";
            private boolean durable = true;
            private boolean exclusive = false;
            private boolean autoDelete = false;
            private int messageTtl = 300000; // 5 minutes
            private int maxLength = 10000;
            
            // Getters and setters
            public String getPrefix() { return prefix; }
            public void setPrefix(String prefix) { this.prefix = prefix; }
            public boolean isDurable() { return durable; }
            public void setDurable(boolean durable) { this.durable = durable; }
            public boolean isExclusive() { return exclusive; }
            public void setExclusive(boolean exclusive) { this.exclusive = exclusive; }
            public boolean isAutoDelete() { return autoDelete; }
            public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }
            public int getMessageTtl() { return messageTtl; }
            public void setMessageTtl(int messageTtl) { this.messageTtl = messageTtl; }
            public int getMaxLength() { return maxLength; }
            public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        }
        
        public static class DeadLetter {
            private String exchangeName = "exalt.ecosystem.dlx";
            private String queueName = "exalt.ecosystem.dlq";
            private String routingKey = "dead-letter";
            private int ttl = 604800000; // 7 days
            
            // Getters and setters
            public String getExchangeName() { return exchangeName; }
            public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }
            public String getQueueName() { return queueName; }
            public void setQueueName(String queueName) { this.queueName = queueName; }
            public String getRoutingKey() { return routingKey; }
            public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
            public int getTtl() { return ttl; }
            public void setTtl(int ttl) { this.ttl = ttl; }
        }
        
        public static class Retry {
            private boolean enabled = true;
            private int maxAttempts = 3;
            private long initialInterval = 1000;
            private long maxInterval = 10000;
            private double multiplier = 2.0;
            
            // Getters and setters
            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public int getMaxAttempts() { return maxAttempts; }
            public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
            public long getInitialInterval() { return initialInterval; }
            public void setInitialInterval(long initialInterval) { this.initialInterval = initialInterval; }
            public long getMaxInterval() { return maxInterval; }
            public void setMaxInterval(long maxInterval) { this.maxInterval = maxInterval; }
            public double getMultiplier() { return multiplier; }
            public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
        }
        
        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getVirtualHost() { return virtualHost; }
        public void setVirtualHost(String virtualHost) { this.virtualHost = virtualHost; }
        public boolean isSsl() { return ssl; }
        public void setSsl(boolean ssl) { this.ssl = ssl; }
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public int getRequestedHeartbeat() { return requestedHeartbeat; }
        public void setRequestedHeartbeat(int requestedHeartbeat) { this.requestedHeartbeat = requestedHeartbeat; }
        public int getChannelCacheSize() { return channelCacheSize; }
        public void setChannelCacheSize(int channelCacheSize) { this.channelCacheSize = channelCacheSize; }
        public boolean isPublisherConfirms() { return publisherConfirms; }
        public void setPublisherConfirms(boolean publisherConfirms) { this.publisherConfirms = publisherConfirms; }
        public boolean isPublisherReturns() { return publisherReturns; }
        public void setPublisherReturns(boolean publisherReturns) { this.publisherReturns = publisherReturns; }
        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
        public Exchange getExchange() { return exchange; }
        public void setExchange(Exchange exchange) { this.exchange = exchange; }
        public Queue getQueue() { return queue; }
        public void setQueue(Queue queue) { this.queue = queue; }
        public DeadLetter getDeadLetter() { return deadLetter; }
        public void setDeadLetter(DeadLetter deadLetter) { this.deadLetter = deadLetter; }
        public Retry getRetry() { return retry; }
        public void setRetry(Retry retry) { this.retry = retry; }
    }
    
    @Bean
    @ConfigurationProperties(prefix = "messaging.rabbitmq")
    public RabbitMQProperties rabbitMQProperties() {
        return new RabbitMQProperties();
    }
    
    /**
     * RabbitMQ connection factory.
     */
    @Bean
    public ConnectionFactory connectionFactory(RabbitMQProperties properties) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        factory.setVirtualHost(properties.getVirtualHost());
        factory.setConnectionTimeout(properties.getConnectionTimeout());
        factory.setRequestedHeartBeat(properties.getRequestedHeartbeat());
        factory.setChannelCacheSize(properties.getChannelCacheSize());
        factory.setPublisherConfirmType(
            properties.isPublisherConfirms() ? 
            CachingConnectionFactory.ConfirmType.CORRELATED : 
            CachingConnectionFactory.ConfirmType.NONE
        );
        factory.setPublisherReturns(properties.isPublisherReturns());
        
        if (properties.isSsl()) {
            try {
                factory.getRabbitConnectionFactory().useSslProtocol();
            } catch (Exception e) {
                throw new RuntimeException("Failed to enable SSL for RabbitMQ connection", e);
            }
        }
        
        return factory;
    }
    
    /**
     * RabbitMQ template for sending messages.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                       MessageConverter messageConverter,
                                       RabbitMQProperties properties) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(properties.isMandatory());
        template.setExchange(properties.getExchange().getName());
        
        // Configure retry template if enabled
        if (properties.getRetry().isEnabled()) {
            template.setRetryTemplate(createRetryTemplate(properties.getRetry()));
        }
        
        return template;
    }
    
    /**
     * Message converter for JSON serialization.
     */
    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }
    
    /**
     * Main topic exchange for domain events.
     */
    @Bean
    public TopicExchange mainExchange(RabbitMQProperties properties) {
        RabbitMQProperties.Exchange exchangeProps = properties.getExchange();
        ExchangeBuilder builder = ExchangeBuilder.topicExchange(exchangeProps.getName())
            .durable(exchangeProps.isDurable());
        if (exchangeProps.isAutoDelete()) {
            builder = builder.autoDelete();
        }
        return builder.build();
    }
    
    /**
     * Dead letter exchange for failed messages.
     */
    @Bean
    public DirectExchange deadLetterExchange(RabbitMQProperties properties) {
        return ExchangeBuilder
            .directExchange(properties.getDeadLetter().getExchangeName())
            .durable(true)
            .build();
    }
    
    /**
     * Dead letter queue for failed messages.
     */
    @Bean
    public org.springframework.amqp.core.Queue deadLetterQueue(RabbitMQProperties properties) {
        return QueueBuilder
            .durable(properties.getDeadLetter().getQueueName())
            .ttl(properties.getDeadLetter().getTtl())
            .build();
    }
    
    /**
     * Binding for dead letter queue.
     */
    @Bean
    public Binding deadLetterBinding(org.springframework.amqp.core.Queue deadLetterQueue,
                                   DirectExchange deadLetterExchange,
                                   RabbitMQProperties properties) {
        return BindingBuilder
            .bind(deadLetterQueue)
            .to(deadLetterExchange)
            .with(properties.getDeadLetter().getRoutingKey());
    }
    
    /**
     * Rabbit listener container factory.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RabbitMQProperties properties) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // Configure retry template if enabled
        if (properties.getRetry().isEnabled()) {
            factory.setRetryTemplate(createRetryTemplate(properties.getRetry()));
        }
        
        return factory;
    }
    
    /**
     * Creates a retry template based on configuration.
     */
    private RetryTemplate createRetryTemplate(RabbitMQProperties.Retry retryProps) {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryProps.getMaxAttempts());
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProps.getInitialInterval());
        backOffPolicy.setMaxInterval(retryProps.getMaxInterval());
        backOffPolicy.setMultiplier(retryProps.getMultiplier());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
    
    /**
     * Creates a queue with standard configuration.
     */
    public org.springframework.amqp.core.Queue createStandardQueue(String queueName, RabbitMQProperties properties) {
        RabbitMQProperties.Queue queueProps = properties.getQueue();
        RabbitMQProperties.DeadLetter dlProps = properties.getDeadLetter();
        
        QueueBuilder builder = QueueBuilder.durable(queueName);
        if (queueProps.isExclusive()) {
            builder = builder.exclusive();
        }
        if (queueProps.isAutoDelete()) {
            builder = builder.autoDelete();
        }
        if (queueProps.getMessageTtl() > 0) {
            builder = builder.ttl(queueProps.getMessageTtl());
        }
        if (queueProps.getMaxLength() > 0) {
            builder = builder.maxLength(queueProps.getMaxLength());
        }
        if (dlProps.getExchangeName() != null) {
            builder = builder.deadLetterExchange(dlProps.getExchangeName());
        }
        if (dlProps.getRoutingKey() != null) {
            builder = builder.deadLetterRoutingKey(dlProps.getRoutingKey());
        }
        return builder.build();
    }
    
    /**
     * Creates a binding for a queue to the main exchange with a routing key.
     */
    public Binding createBinding(org.springframework.amqp.core.Queue queue, 
                               TopicExchange exchange, 
                               String routingKey) {
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(routingKey);
    }
}