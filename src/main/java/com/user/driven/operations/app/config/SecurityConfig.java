package com.user.driven.operations.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.user.driven.operations.app.security.ApiKeyAuthenticationFilter;
import com.user.driven.operations.app.security.JwtAuthenticationFilter;
import com.user.driven.operations.app.security.RateLimitFilter;

import lombok.RequiredArgsConstructor;

/**
 * Security configuration class for setting up HTTP security filters.
 * <p>
 * Configures stateless authentication with a filter chain ordered as:
 * ApiKey → JWT → Authorization. Public endpoints (auth, swagger, actuator, H2 console)
 * are permitted without authentication. The /api/v1/** paths (except auth) require
 * authentication, while legacy /api/** paths remain open until the API versioning
 * migration in Phase 9.
 * </p>
 * <p>
 * Enables method-level security via {@code @EnableMethodSecurity} for future
 * {@code @PreAuthorize} annotations on controllers to enforce role-based access
 * (USER restricted to own projects, ADMIN access to all resources).
 * </p>
 *
 * @author Jatin Raheja
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    /**
     * Configures the security filter chain with API key and JWT authentication.
     * <p>
     * Filter order: ApiKey → JWT → Spring Security Authorization.
     * </p>
     * <p>
     * Access rules:
     * <ul>
     *   <li>Public: /api/v1/auth/**, /swagger-ui/**, /api-docs/**, /actuator/**, /h2-console/**</li>
     *   <li>Legacy (pre-versioning): /api/projects/**, /api/generator/** — permitted until Phase 9 migration</li>
     *   <li>Authenticated: /api/v1/** (except auth) — requires valid JWT or API key</li>
     *   <li>All other requests: permitted (catch-all for unmapped paths)</li>
     * </ul>
     * </p>
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/projects/**", "/api/generator/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a BCrypt password encoder for hashing user passwords.
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager bean for use in the AuthController.
     *
     * @param authConfiguration the authentication configuration
     * @return the AuthenticationManager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

}
