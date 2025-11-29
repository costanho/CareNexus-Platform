package com.carenexus.direct.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.rebalance.ConsumerSeekToCurrentErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * ====================================================================
 * Auth Event Listener - Kafka Consumer
 * ====================================================================
 *
 * Listens for and processes authentication events from Auth Service.
 *
 * Events Consumed:
 * - user.registered     → Create corresponding doctor/patient record
 * - user.loggedIn       → Update user last login timestamp
 * - user.loggedOut      → Clear user sessions/cache
 * - token.refreshed     → Log token refresh events
 *
 * Processing Flow:
 * 1. Receive event from Kafka topic
 * 2. Deserialize JSON to event object
 * 3. Process the event (create records, update status, etc)
 * 4. Manually acknowledge successful processing
 * 5. On error, message is retried (offset not committed)
 *
 * Error Handling:
 * - If processing fails, message stays in queue for retry
 * - Failed messages logged with full context
 * - Can implement Dead Letter Queue for persistent failures
 *
 * Concurrency:
 * - Each listener method runs in its own thread
 * - Multiple events can be processed in parallel (configured in KafkaConsumerConfig)
 *
 * ====================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventListener {

    private final ObjectMapper objectMapper;

    /**
     * Listen for user registration events.
     *
     * Topic: user.registered
     * Triggered when: New user creates account in Auth Service
     *
     * Action: Create corresponding Doctor or Patient record in Direct Service
     *
     * Event Payload:
     * {
     *   "userId": 1,
     *   "email": "john@example.com",
     *   "fullName": "John Doe",
     *   "role": "ROLE_DOCTOR",
     *   "timestamp": "2025-11-29T12:34:56"
     * }
     *
     * @param eventJson    JSON string from Kafka
     * @param partition    Kafka partition number
     * @param offset       Message offset
     * @param ack          Manual acknowledgment handler
     */
    @KafkaListener(
            topics = "user.registered",
            groupId = "direct-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserRegistered(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[AuthEventListener] Received user.registered event (partition={}, offset={})", partition, offset);
        log.debug("[AuthEventListener] Event JSON: {}", eventJson);

        try {
            // Deserialize JSON to event object
            UserRegisteredEvent event = objectMapper.readValue(eventJson, UserRegisteredEvent.class);
            log.info("[AuthEventListener] Processing registration for user: {} (role: {})",
                    event.getEmail(), event.getRole());

            // Process the event
            // TODO: Implement business logic
            // - Create Doctor record if role == ROLE_DOCTOR
            // - Create Patient record if role == ROLE_PATIENT
            // - Send welcome notification

            log.info("[AuthEventListener] ✓ User registration processed: {}", event.getEmail());

            // Manually acknowledge successful processing
            ack.acknowledge();
            log.debug("[AuthEventListener] ✓ Message acknowledged for offset {}", offset);

        } catch (Exception e) {
            log.error("[AuthEventListener] ✗ Failed to process user.registered event: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
            // Or send to dead letter queue
        }
    }

    /**
     * Listen for user login events.
     *
     * Topic: user.loggedIn
     * Triggered when: User successfully logs in
     *
     * Action: Update user's last login timestamp, log activity
     *
     * @param eventJson    JSON string from Kafka
     * @param partition    Kafka partition number
     * @param offset       Message offset
     * @param ack          Manual acknowledgment handler
     */
    @KafkaListener(
            topics = "user.loggedIn",
            groupId = "direct-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserLoggedIn(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[AuthEventListener] Received user.loggedIn event (partition={}, offset={})", partition, offset);

        try {
            UserLoggedInEvent event = objectMapper.readValue(eventJson, UserLoggedInEvent.class);
            log.info("[AuthEventListener] User logged in: {}", event.getEmail());

            // Process the event
            // TODO: Implement business logic
            // - Update last login timestamp
            // - Clear lockout status if any
            // - Log activity

            log.info("[AuthEventListener] ✓ Login event processed: {}", event.getEmail());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("[AuthEventListener] ✗ Failed to process user.loggedIn event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for user logout events.
     *
     * Topic: user.loggedOut
     * Triggered when: User logs out or session expires
     *
     * Action: Clear user sessions, invalidate temporary data
     *
     * @param eventJson    JSON string from Kafka
     * @param partition    Kafka partition number
     * @param offset       Message offset
     * @param ack          Manual acknowledgment handler
     */
    @KafkaListener(
            topics = "user.loggedOut",
            groupId = "direct-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserLoggedOut(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[AuthEventListener] Received user.loggedOut event (partition={}, offset={})", partition, offset);

        try {
            UserLoggedOutEvent event = objectMapper.readValue(eventJson, UserLoggedOutEvent.class);
            log.info("[AuthEventListener] User logged out: {}", event.getEmail());

            // Process the event
            // TODO: Implement business logic
            // - Invalidate sessions
            // - Clear temporary cache entries
            // - Log activity

            log.info("[AuthEventListener] ✓ Logout event processed: {}", event.getEmail());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("[AuthEventListener] ✗ Failed to process user.loggedOut event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for token refresh events.
     *
     * Topic: token.refreshed
     * Triggered when: User refreshes JWT token
     *
     * Action: Log token refresh events for security audit
     *
     * @param eventJson    JSON string from Kafka
     * @param partition    Kafka partition number
     * @param offset       Message offset
     * @param ack          Manual acknowledgment handler
     */
    @KafkaListener(
            topics = "token.refreshed",
            groupId = "direct-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTokenRefreshed(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[AuthEventListener] Received token.refreshed event (partition={}, offset={})", partition, offset);

        try {
            TokenRefreshedEvent event = objectMapper.readValue(eventJson, TokenRefreshedEvent.class);
            log.debug("[AuthEventListener] Token refreshed for user: {}", event.getEmail());

            // Process the event
            // TODO: Implement business logic
            // - Log for audit trail
            // - Monitor token refresh frequency

            log.debug("[AuthEventListener] ✓ Token refresh event processed: {}", event.getEmail());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("[AuthEventListener] ✗ Failed to process token.refreshed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Inner classes representing events from Kafka.
     *
     * These match the event classes in auth-service/event package.
     */

    public static class UserRegisteredEvent {
        public Long userId;
        public String email;
        public String fullName;
        public String role;
        public String timestamp;
    }

    public static class UserLoggedInEvent {
        public Long userId;
        public String email;
        public String timestamp;
        public String ipAddress;
    }

    public static class UserLoggedOutEvent {
        public Long userId;
        public String email;
        public String timestamp;
    }

    public static class TokenRefreshedEvent {
        public Long userId;
        public String email;
        public String timestamp;
    }
}
