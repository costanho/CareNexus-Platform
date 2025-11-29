package com.carenexus.direct.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentKafkaListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * ====================================================================
 * Kafka Consumer Configuration for Direct Service
 * ====================================================================
 *
 * Configures Kafka consumers for listening to events from Auth Service.
 *
 * Events Consumed:
 * - user.registered     → Create doctor/patient records
 * - user.loggedIn       → Update user status
 * - user.loggedOut      → Clear user sessions
 * - token.refreshed     → Log token refresh events
 *
 * Consumer Settings:
 * - Group ID: direct-service-group
 * - Auto offset reset: earliest (start from beginning if group is new)
 * - Max poll records: 500 (fetch up to 500 records per poll)
 * - Session timeout: 30s
 *
 * Processing:
 * - Acknowledgment mode: MANUAL (explicit commit after processing)
 * - Allows retry on processing failure
 *
 * ====================================================================
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Configure Kafka consumer factory.
     *
     * Settings:
     * - Group ID: direct-service-group
     * - Key deserializer: StringDeserializer
     * - Value deserializer: StringDeserializer (for JSON messages)
     * - Auto offset reset: earliest
     * - Max poll records: 500
     *
     * @return ConsumerFactory<String, String>
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        log.info("[KafkaConsumerConfig] Configuring Kafka consumer factory");

        Map<String, Object> props = new HashMap<>();

        // Bootstrap servers
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "direct-service-group");

        // Deserialization
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Consumer behavior
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // Start from beginning
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);           // Fetch 500 records per poll
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);       // 30 second session
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);   // 10 second heartbeat

        // Avoid consumer rebalancing during long processing
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);   // 5 minute max poll interval

        log.debug("[KafkaConsumerConfig] Consumer config: group=direct-service-group, auto_offset=earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Create Kafka listener container factory.
     *
     * Configures:
     * - Concurrency: 3 threads for parallel processing
     * - Acknowledgment mode: MANUAL (explicit commit)
     * - Poll timeout: 10 seconds
     *
     * @param consumerFactory ConsumerFactory
     * @return KafkaListenerContainerFactory
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentKafkaListenerContainer<String, String>>
    kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        log.info("[KafkaConsumerConfig] Creating Kafka listener container factory");

        org.springframework.kafka.listener.ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new org.springframework.kafka.listener.ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);  // Process 3 messages in parallel
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);  // Manual commit
        factory.getContainerProperties().setPollTimeout(10000);  // 10 second poll timeout

        log.debug("[KafkaConsumerConfig] Listener factory: concurrency=3, ack_mode=MANUAL");
        return factory;
    }
}
