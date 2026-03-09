package com.cbp.account.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer and consumer configuration for the Account Service.
 *
 * <p>Configures:
 * <ul>
 *   <li>An idempotent producer factory with {@code acks=all} and
 *       {@code enable.idempotence=true} to prevent duplicate event publishing.</li>
 *   <li>A consumer factory with manual offset acknowledgement to guarantee
 *       at-least-once processing semantics — offsets are only committed after
 *       successful business logic execution.</li>
 * </ul>
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Creates a Kafka {@link ProducerFactory} configured for idempotent, reliable delivery.
     *
     * <p>Key settings:
     * <ul>
     *   <li>{@code acks=all} — producer waits for all in-sync replicas to acknowledge.</li>
     *   <li>{@code enable.idempotence=true} — prevents duplicate messages on retry.</li>
     *   <li>{@code retries=3} — retries transient network failures.</li>
     * </ul>
     *
     * @return a configured {@link DefaultKafkaProducerFactory}
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Creates a {@link KafkaTemplate} for sending events to Kafka topics.
     *
     * @return a {@link KafkaTemplate} backed by the {@link #producerFactory()}
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Creates a Kafka {@link ConsumerFactory} with manual acknowledgement mode.
     *
     * @return a configured {@link DefaultKafkaConsumerFactory}
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} with manual
     * acknowledgement to prevent offset advancement on processing errors.
     *
     * @return the configured listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
