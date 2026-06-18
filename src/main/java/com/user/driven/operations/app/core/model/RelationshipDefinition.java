package com.user.driven.operations.app.core.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.user.driven.operations.enums.DtoStrategy;
import com.user.driven.operations.enums.FetchType;
import com.user.driven.operations.enums.RelationshipType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a JPA relationship definition between two entities within a project.
 * Stores metadata about the relationship type, fetch strategy, cascade behavior,
 * join column configuration, and DTO representation strategy.
 *
 * @author Jatin Raheja
 */
@Entity
@Table(name = "relationship_definitions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RelationshipDefinition {

    /**
     * Unique identifier for the relationship definition.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The entity that owns this relationship definition.
     */
    @NotNull
    @JsonBackReference("entity-relationships")
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    private EntityDefinition entity;

    /**
     * The type of JPA relationship (ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;

    /**
     * The name of the target entity for this relationship.
     */
    @NotBlank(message = "Target entity name is required")
    @Column(name = "target_entity", nullable = false)
    private String targetEntity;

    /**
     * The Java field name to be generated in the source entity.
     */
    @NotBlank(message = "Field name is required")
    @Column(name = "field_name", nullable = false)
    private String fieldName;

    /**
     * The mappedBy attribute for bidirectional relationships.
     * Specifies the field name on the inverse side.
     */
    @Column(name = "mapped_by")
    private String mappedBy;

    /**
     * The JPA fetch type strategy (LAZY or EAGER). Defaults to LAZY.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fetch_type")
    private FetchType fetchType = FetchType.LAZY;

    /**
     * Comma-separated list of cascade types (ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH).
     */
    @Column(name = "cascade_types")
    private String cascadeTypes;

    /**
     * Whether orphan removal is enabled for this relationship. Defaults to false.
     */
    @Column(name = "orphan_removal")
    private boolean orphanRemoval = false;

    /**
     * The name of the join column for @JoinColumn annotation.
     */
    @Column(name = "join_column")
    private String joinColumn;

    /**
     * The name of the join table for @JoinTable annotation (used in ManyToMany).
     */
    @Column(name = "join_table_name")
    private String joinTableName;

    /**
     * The name of the inverse join column for @JoinTable annotation.
     */
    @Column(name = "inverse_join_column")
    private String inverseJoinColumn;

    /**
     * Whether the relationship is nullable. Defaults to true.
     */
    @Column(name = "nullable")
    private boolean nullable = true;

    /**
     * The DTO strategy for representing this relationship (ID_ONLY, SUMMARY, NESTED, IGNORE).
     * Defaults to ID_ONLY.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dto_strategy")
    private DtoStrategy dtoStrategy = DtoStrategy.ID_ONLY;

    /**
     * The JSON serialization handling strategy (e.g., BACK_REFERENCE, MANAGED_REFERENCE).
     * Defaults to BACK_REFERENCE.
     */
    @Column(name = "json_handling")
    private String jsonHandling = "BACK_REFERENCE";

    /**
     * Timestamp of when the relationship definition was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Lifecycle hook to set creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
