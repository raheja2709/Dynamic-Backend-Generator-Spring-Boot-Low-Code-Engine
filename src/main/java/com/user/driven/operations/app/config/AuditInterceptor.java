package com.user.driven.operations.app.config;

import com.user.driven.operations.app.core.model.AuditLog;
import com.user.driven.operations.app.core.repository.AuditLogRepository;
import com.user.driven.operations.app.common.util.MessageConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that creates an audit log entry for every API request.
 * Records method, endpoint, response status, duration, client IP, and user agent.
 *
 * <p>In preHandle, it records the request start time as a request attribute.
 * In afterCompletion, it calculates the duration and persists the audit log entry.</p>
 *
 * <p>Request body capture is intentionally skipped here as it requires a
 * ContentCachingRequestWrapper filter. Only URL, method, status, duration,
 * IP, and user-agent are captured.</p>
 */
@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

    private static final String START_TIME_ATTRIBUTE = "auditStartTime";
    private static final int MAX_ENDPOINT_LENGTH = 500;
    private static final int MAX_REQUEST_BODY_LENGTH = 10_000;

    private final AuditLogRepository auditLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            long startTime = getStartTime(request);
            long durationMs = System.currentTimeMillis() - startTime;

            String requestId = getRequestId(request);
            String method = request.getMethod();
            String endpoint = truncate(buildEndpoint(request), MAX_ENDPOINT_LENGTH);
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            AuditLog auditLog = new AuditLog();
            auditLog.setRequestId(requestId);
            auditLog.setMethod(method);
            auditLog.setEndpoint(endpoint);
            auditLog.setResponseStatus(response.getStatus());
            auditLog.setDurationMs(durationMs);
            auditLog.setIpAddress(clientIp);
            auditLog.setUserAgent(truncate(userAgent, 500));

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Requirement 5.7: If the AuditInterceptor fails to persist a log record,
            // log the failure to console and continue without interrupting the request.
            log.error(MessageConstants.AUDIT_PERSIST_FAILED, e.getMessage(), e);
        }
    }

    /**
     * Retrieves the start time from the request attribute.
     */
    private long getStartTime(HttpServletRequest request) {
        Object startTimeAttr = request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTimeAttr instanceof Long) {
            return (Long) startTimeAttr;
        }
        return System.currentTimeMillis();
    }

    /**
     * Retrieves the request ID from MDC (set by RequestIdFilter).
     */
    private String getRequestId(HttpServletRequest request) {
        String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
        if (requestId == null || requestId.isBlank()) {
            // Fallback to request attribute
            Object attr = request.getAttribute(RequestIdFilter.REQUEST_ATTRIBUTE_KEY);
            requestId = attr != null ? attr.toString() : "unknown";
        }
        return requestId;
    }

    /**
     * Builds the full endpoint string including query parameters.
     */
    private String buildEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            return uri + "?" + queryString;
        }
        return uri;
    }

    /**
     * Extracts the client IP, checking X-Forwarded-For header for proxied requests.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Truncates a string to the specified maximum length.
     *
     * @param value     the string to truncate
     * @param maxLength the maximum allowed length
     * @return the truncated string, or null if the input is null
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
