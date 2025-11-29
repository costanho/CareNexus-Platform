package com.carenexus.direct.service;

import com.carenexus.auth.dto.UserInfoResponse;
import com.carenexus.direct.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ====================================================================
 * Remote User Service - Auth Service Integration
 * ====================================================================
 *
 * Provides access to user information from Auth Service.
 * Uses AuthServiceClient for REST calls instead of raw RestTemplate.
 *
 * Methods:
 * - getUserInfo(token): Get user info from Auth Service
 * - validateToken(token): Validate JWT token with Auth Service
 *
 * ====================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteUserService {

    private final AuthServiceClient authServiceClient;

    /**
     * Get user information from Auth Service using JWT token
     * @param jwtToken JWT access token from Authorization header
     * @return UserInfoResponse containing user details
     */
    public UserInfoResponse getUserInfo(String jwtToken) {
        log.info("[RemoteUserService] Fetching user info from Auth Service");
        return authServiceClient.getUserInfo(jwtToken);
    }

    /**
     * Validate JWT token with Auth Service
     * @param jwtToken JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String jwtToken) {
        log.info("[RemoteUserService] Validating token with Auth Service");
        var response = authServiceClient.validateToken(jwtToken);
        return response != null && response.isValid();
    }

    /**
     * DEPRECATED: Old method using raw RestTemplate
     * @deprecated Use getUserInfo(jwtToken) instead
     */
    @Deprecated
    public Long getUserIdByEmail(String email) {
        log.warn("[RemoteUserService] getUserIdByEmail is deprecated, use getUserInfo instead");
        // This is no longer supported - user info comes with JWT token
        throw new UnsupportedOperationException("Use getUserInfo(jwtToken) instead");
    }
}
