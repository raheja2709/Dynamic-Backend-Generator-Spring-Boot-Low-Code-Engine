package com.user.driven.operations.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * CORS configuration properties bound from the {@code app.cors.*} namespace.
 *
 * <p>Defaults to deny-all when no allowed origins are configured (empty list).
 * In dev profile, origins can be set to "*" to permit all cross-origin requests.</p>
 */
@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests.
     * Empty list means deny-all (no origins permitted).
     * Use "*" to allow all origins.
     */
    private List<String> allowedOrigins = List.of();

    /**
     * List of allowed HTTP methods for CORS requests.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /**
     * List of allowed headers for CORS requests.
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * Whether credentials (cookies, authorization headers) are allowed in CORS requests.
     */
    private boolean allowCredentials = false;

    /**
     * Max age in seconds for the CORS preflight response cache.
     */
    private long maxAge = 3600;
}
