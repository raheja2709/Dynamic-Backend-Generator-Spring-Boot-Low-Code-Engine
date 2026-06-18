package com.user.driven.operations.app.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard API response envelope wrapping all successful and error responses.
 * Provides a consistent structure for API consumers.
 *
 * @param <T> the type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiEnvelope<T>(
    boolean success,
    T data,
    PaginationMeta pagination,
    String error,
    Instant timestamp,
    String requestId
) {

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiEnvelope<T> success(T data, String requestId) {
        return new ApiEnvelope<>(true, data, null, null, Instant.now(), requestId);
    }

    /**
     * Creates a successful response with data and pagination metadata.
     */
    public static <T> ApiEnvelope<T> success(T data, PaginationMeta pagination, String requestId) {
        return new ApiEnvelope<>(true, data, pagination, null, Instant.now(), requestId);
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiEnvelope<T> error(String errorMessage, String requestId) {
        return new ApiEnvelope<>(false, null, null, errorMessage, Instant.now(), requestId);
    }
}
