package com.user.driven.operations.enums;

/**
 * Represents the strategy for representing a relationship in Data Transfer Objects.
 * <ul>
 *   <li>{@code ID_ONLY} - Only include the target entity's primary key</li>
 *   <li>{@code SUMMARY} - Include id and a display label from the target entity</li>
 *   <li>{@code NESTED} - Embed the full target entity DTO one level deep</li>
 *   <li>{@code IGNORE} - Omit the relationship field from the DTO entirely</li>
 * </ul>
 *
 * @author Jatin Raheja
 */
public enum DtoStrategy {

    ID_ONLY,

    SUMMARY,

    NESTED,

    IGNORE
}
