package com.user.driven.operations.app.common.util;

import org.slf4j.MDC;

/**
 * Utility class for accessing request-scoped context values.
 * Provides access to the current X-Request-Id set by the RequestIdFilter via MDC.
 */
public final class RequestContext {

    private static final String MDC_REQUEST_ID_KEY = "requestId";

    private RequestContext() {
        // Utility class - prevent instantiation
    }

    /**
     * Retrieves the current request ID from the MDC context.
     *
     * @return the request ID or "unknown" if not available
     */
    public static String getRequestId() {
        String requestId = MDC.get(MDC_REQUEST_ID_KEY);
        return requestId != null ? requestId : "unknown";
    }
}
