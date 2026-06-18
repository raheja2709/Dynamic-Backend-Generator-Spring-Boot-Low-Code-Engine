package com.user.driven.operations.app.api.dto;

import java.util.List;

import com.user.driven.operations.enums.CascadeType;
import com.user.driven.operations.enums.DtoStrategy;
import com.user.driven.operations.enums.FetchType;
import com.user.driven.operations.enums.RelationshipType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing a relationship definition between entities.
 * Includes metadata about the relationship type, fetch strategy, cascade behavior,
 * and DTO representation strategy.
 *
 * @author Jatin Raheja
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RelationshipDefinitionDto {

    /**
     * The unique identifier of the relationship definition (used for updates).
     */
    private Long id;

    /**
     * The type of JPA relationship. Required.
     */
    @NotNull(message = "Relationship type is required")
    private RelationshipType relationshipType;

    /**
     * The name of the target entity. Required.
     */
    @NotBlank(message = "Target entity name is required")
    private String targetEntity;

    /**
     * The Java field name in the generated entity. Required.
     */
    @NotBlank(message = "Field name is required")
    private String fieldName;

    /**
     * The mappedBy attribute for bidirectional relationships.
     */
    private String mappedBy;

    /**
     * The JPA fetch type strategy. Defaults to LAZY if not specified.
     */
    private FetchType fetchType;

    /**
     * The list of cascade types for this relationship.
     */
    private List<CascadeType> cascadeTypes;

    /**
     * Whether orphan removal is enabled. Defaults to false.
     */
    private boolean orphanRemoval;

    /**
     * The name of the join column.
     */
    private String joinColumn;

    /**
     * The name of the join table (used for ManyToMany relationships).
     */
    private String joinTableName;

    /**
     * The name of the inverse join column (used for ManyToMany relationships).
     */
    private String inverseJoinColumn;

    /**
     * Whether the relationship is nullable. Defaults to true.
     */
    private boolean nullable = true;

    /**
     * The DTO strategy for representing this relationship. Defaults to ID_ONLY.
     */
    private DtoStrategy dtoStrategy;
}
