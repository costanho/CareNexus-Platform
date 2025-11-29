package com.carenexus.direct.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * ====================================================================
 * MICROSERVICE MODE: SECURITY DISABLED
 * ====================================================================
 *
 * Direct Service runs as a PURE MICROSERVICE:
 * - NO embedded authentication logic
 * - NO JWT validation locally
 * - NO user management
 *
 * All auth is delegated to AUTH SERVICE (port 8082):
 * - Client calls Auth Service for login/register
 * - Auth Service returns JWT tokens
 * - Client includes JWT in subsequent requests to Direct Service
 * - Direct Service validates JWT by calling Auth Service REST API
 *
 * Security is enforced at API Gateway level (future):
 * - Gateway validates JWT with Auth Service
 * - Gateway forwards requests to Direct Service only if authenticated
 *
 * ====================================================================
 */
@Configuration
@EnableMethodSecurity   // 🔥 enables @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    // COMMENTED OUT: JwtAuthenticationFilter removed - no embedded JWT validation
    // private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:3000", "http://127.0.0.1:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // MICROSERVICE MODE: All endpoints are open
                        // Authentication is handled by API Gateway (future)
                        // For now, direct service is internal-only
                        .anyRequest().permitAll()
                )
                // COMMENTED OUT: No embedded JWT filter
                // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                ;

        return http.build();
    }
}
