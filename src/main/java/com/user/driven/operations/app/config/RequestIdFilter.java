package com.user.driven.operations.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that generates a unique X-Request-Id for every incoming request.
 * The ID is:
 * 1. Added to the response headers
 * 2. Put into SLF4J MDC so all log entries include it
 * 3. Available via request attribute for downstream components
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";
    public static final String REQUEST_ATTRIBUTE_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Check if client sent a request ID, otherwise generate one
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Set in MDC for log correlation
        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        // Set as request attribute for access in controllers/services
        request.setAttribute(REQUEST_ATTRIBUTE_KEY, requestId);

        // Add to response header
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC to prevent thread pool contamination
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }
}
