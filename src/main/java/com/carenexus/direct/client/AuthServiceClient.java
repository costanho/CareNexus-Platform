package com.carenexus.direct.client;

import com.carenexus.auth.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * ====================================================================
 * Auth Service Client - Inter-Service Communication
 * ====================================================================
 *
 * This client encapsulates all HTTP calls from Direct Service to Auth Service.
 *
 * Responsibilities:
 * - Validate JWT tokens with Auth Service
 * - Fetch user information from Auth Service
 * - Handle service-to-service communication failures gracefully
 *
 * Configuration:
 * - Auth Service URL: http://localhost:8082 (configurable via application.yml)
 * - Base endpoint: /api/auth
 *
 * Usage:
 *   @Autowired
 *   private AuthServiceClient authClient;
 *
 *   UserInfoResponse user = authClient.getUserInfo(jwtToken);
 *   boolean isValid = authClient.validateToken(jwtToken);
 *
 * ====================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    private static final String AUTH_BASE = "/api/auth";
    private static final String VALIDATE_ENDPOINT = AUTH_BASE + "/validate";
    private static final String USER_INFO_ENDPOINT = AUTH_BASE + "/me";

    /**
     * Fetch current user information from Auth Service.
     *
     * HTTP GET /api/auth/me
     * Authorization: Bearer <jwt-token>
     *
     * @param jwtToken JWT token extracted from Authorization header
     * @return UserInfoResponse containing user details (id, email, fullName, role)
     * @throws RuntimeException if Auth Service is unreachable or token is invalid
     */
    public UserInfoResponse getUserInfo(String jwtToken) {
        log.info("[AuthServiceClient] Fetching user info from Auth Service");
        String url = authServiceUrl + USER_INFO_ENDPOINT;

        try {
            log.debug("[AuthServiceClient] GET {} with token: {}...{}", url,
                    jwtToken.substring(0, Math.min(20, jwtToken.length())),
                    jwtToken.substring(Math.max(0, jwtToken.length() - 10)));

            HttpHeaders headers = createAuthHeaders(jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserInfoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserInfoResponse.class
            );

            if (response.getBody() == null) {
                log.warn("[AuthServiceClient] ✗ Auth Service returned null user info");
                throw new RuntimeException("Auth Service returned empty user info");
            }

            log.info("[AuthServiceClient] ✓ User info fetched: {} ({})",
                    response.getBody().getEmail(),
                    response.getBody().getRole());
            return response.getBody();

        } catch (RestClientException e) {
            log.error("[AuthServiceClient] ✗ Failed to fetch user info from Auth Service: {}",
                    e.getMessage());
            throw new RuntimeException("Auth Service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Validate JWT token with Auth Service.
     *
     * HTTP GET /api/auth/validate
     * Authorization: Bearer <jwt-token>
     *
     * Returns:
     * {
     *   "valid": true,
     *   "userId": 1,
     *   "email": "user@example.com"
     * }
     *
     * @param jwtToken JWT token to validate
     * @return TokenValidationResponse with validity status
     * @throws RuntimeException if Auth Service is unreachable
     */
    public TokenValidationResponse validateToken(String jwtToken) {
        log.info("[AuthServiceClient] Validating token with Auth Service");
        String url = authServiceUrl + VALIDATE_ENDPOINT;

        try {
            log.debug("[AuthServiceClient] GET {} with token validation", url);

            HttpHeaders headers = createAuthHeaders(jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<TokenValidationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    TokenValidationResponse.class
            );

            TokenValidationResponse validation = response.getBody();
            if (validation == null) {
                log.warn("[AuthServiceClient] ✗ Auth Service returned null validation response");
                return new TokenValidationResponse(false, null, null);
            }

            log.info("[AuthServiceClient] ✓ Token validation result: {}", validation.isValid());
            return validation;

        } catch (RestClientException e) {
            log.error("[AuthServiceClient] ✗ Token validation failed: {}", e.getMessage());
            return new TokenValidationResponse(false, null, null);
        }
    }

    /**
     * Create HTTP headers with JWT token for authentication.
     *
     * @param jwtToken JWT token to include
     * @return HttpHeaders with Authorization: Bearer <token> and Content-Type
     */
    private HttpHeaders createAuthHeaders(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        headers.set("Content-Type", "application/json");
        log.debug("[AuthServiceClient] Created auth headers with JWT token");
        return headers;
    }

    /**
     * Inner class for token validation response.
     *
     * Matches Auth Service response format from /api/auth/validate endpoint.
     */
    public static class TokenValidationResponse {
        private boolean valid;
        private Long userId;
        private String email;

        public TokenValidationResponse(boolean valid, Long userId, String email) {
            this.valid = valid;
            this.userId = userId;
            this.email = email;
        }

        // Getters
        public boolean isValid() { return valid; }
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }

        // Setters for Jackson deserialization
        public void setValid(boolean valid) { this.valid = valid; }
        public void setUserId(Long userId) { this.userId = userId; }
        public void setEmail(String email) { this.email = email; }
    }
}
