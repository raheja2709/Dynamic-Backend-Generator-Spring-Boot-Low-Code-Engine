package com.user.driven.operations.app.api.dto;

import org.springframework.data.domain.Page;

/**
 * Pagination metadata included in list responses.
 * Contains page number, page size, total elements, and total pages.
 */
public record PaginationMeta(
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    /**
     * Creates PaginationMeta from a Spring Data Page result.
     *
     * @param page the Spring Data Page to extract metadata from
     * @return a PaginationMeta instance with the page's metadata
     */
    public static PaginationMeta of(Page<?> page) {
        return new PaginationMeta(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
