package com.user.driven.operations.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * Audit logging is now handled by {@link LoggingAspect} via AOP.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // No interceptors needed - AOP handles all logging
}
