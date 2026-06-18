package com.user.driven.operations.app.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.user.driven.operations.app.common.util.MessageConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting filter that restricts authenticated users to a configurable number
 * of requests per time window on the generation endpoint.
 * <p>
 * Uses a sliding window approach with an in-memory ConcurrentHashMap to track
 * request timestamps per user. When the limit is exceeded, returns HTTP 429.
 * </p>
 *
 * @author Jatin Raheja
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ConcurrentHashMap<String, Deque<Instant>> requestCounts = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !isAnonymous(auth)) {
            String userKey = auth.getName();

            if (isRateLimited(userKey)) {
                log.warn("Rate limit exceeded for user '{}' on endpoint '{}'", userKey, request.getRequestURI());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String message = String.format(MessageConstants.RATE_LIMIT_EXCEEDED, maxRequests, windowSeconds);
                response.getWriter().write(
                        "{\"timestamp\":\"" + Instant.now() + "\"," +
                        "\"status\":429," +
                        "\"errorType\":\"RATE_LIMITED\"," +
                        "\"message\":\"" + message + "\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Checks whether the user has exceeded the rate limit using a sliding window.
     * Expired timestamps are pruned before checking the count.
     *
     * @param userKey the unique user identifier (email)
     * @return true if the user has exceeded the rate limit
     */
    private boolean isRateLimited(String userKey) {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowSeconds);

        Deque<Instant> timestamps = requestCounts.computeIfAbsent(userKey, k -> new ConcurrentLinkedDeque<>());

        // Remove expired entries outside the sliding window
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            return true;
        }

        timestamps.addLast(now);
        return false;
    }

    /**
     * Only apply rate limiting to the generation endpoint.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().contains("/generator/generate");
    }

    /**
     * Checks if the authentication represents an anonymous user.
     */
    private boolean isAnonymous(Authentication auth) {
        return auth.getPrincipal() instanceof String
                && "anonymousUser".equals(auth.getPrincipal());
    }
}
