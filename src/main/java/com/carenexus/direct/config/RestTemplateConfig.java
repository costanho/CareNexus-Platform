package com.carenexus.direct.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * ====================================================================
 * REST Template Configuration for Inter-Service Communication
 * ====================================================================
 *
 * This configuration sets up RestTemplate beans for calling other
 * microservices (like Auth Service) from the Direct Service.
 *
 * Features:
 * - Connection timeout: 5 seconds
 * - Read timeout: 10 seconds
 * - Automatic request/response buffering for retry logic
 * - Centralized bean for easy modification
 *
 * Usage:
 *   @Autowired
 *   private RestTemplate restTemplate;
 *
 *   restTemplate.getForObject("http://localhost:8082/api/auth/me", UserInfoResponse.class);
 *
 * ====================================================================
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean for making HTTP calls to other services.
     *
     * Configuration:
     * - Connection timeout: 5 seconds (time to establish connection)
     * - Read timeout: 10 seconds (time to read response)
     * - Buffering: Enabled for retry logic
     *
     * @param restTemplateBuilder Spring's RestTemplateBuilder
     * @return Configured RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        log.info("[RestTemplateConfig] Creating RestTemplate bean for inter-service communication");

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))   // Connection timeout
                .setReadTimeout(Duration.ofSeconds(10))     // Read timeout
                .requestFactory(this::clientHttpRequestFactory)  // Buffering for retries
                .build();

        log.debug("[RestTemplateConfig] ✓ RestTemplate configured with 5s connection, 10s read timeout");
        return restTemplate;
    }

    /**
     * Creates a buffered HTTP request factory.
     *
     * Buffering allows request/response bodies to be read multiple times,
     * which is useful for:
     * - Logging request/response bodies
     * - Retry logic (re-reading response)
     * - Error handling
     *
     * @return BufferingClientHttpRequestFactory wrapped around SimpleClientHttpRequestFactory
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        log.debug("[RestTemplateConfig] Creating buffered HTTP request factory");
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);    // 5 seconds
        factory.setReadTimeout(10000);      // 10 seconds
        return new BufferingClientHttpRequestFactory(factory);
    }
}
