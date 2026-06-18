package com.user.driven.operations.app.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an entity definition within a user-driven project. This entity
 * contains metadata about the entity, its fields, and operations. It is linked
 * to a {@link ProjectDefinition} and maintains timestamps for creation and
 * updates.
 *
 * @author: Jatin Raheja
 */
@Entity
@Table(name = "entity_definitions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EntityDefinition {

	/**
	 * Unique identifier for the entity.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Name of the entity. Must be unique and not blank.
	 */
	@NotBlank(message = "Entity name is required")
	@Column(unique = true)
	private String name;

	/**
	 * Optional description providing additional context about the entity.
	 */
	private String description;

	/**
	 * The project to which this entity belongs.
	 */
	@NotNull
	@JsonBackReference("project-entities")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private ProjectDefinition project;

	/**
	 * List of fields defined for this entity. Mapped with cascading and orphan
	 * removal.
	 */
	@JsonManagedReference("entity-fields")
	@OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<FieldDefinition> fields = new ArrayList<>();

	/**
	 * List of relationship definitions for this entity. Mapped with cascading and orphan removal.
	 */
	@JsonManagedReference("entity-relationships")
	@OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<RelationshipDefinition> relationships = new ArrayList<>();

	/**
	 * List of operations configured for this entity (e.g., CREATE, UPDATE). Mapped
	 * with cascading and orphan removal.
	 */
	@JsonManagedReference("entity-operations")
	@OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<OperationConfig> operations = new ArrayList<>();

	/**
	 * Timestamp of when the entity was created.
	 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * Timestamp of when the entity was last updated.
	 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Lifecycle hook to set creation and update timestamps before persisting.
	 */
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	/**
	 * Lifecycle hook to update the timestamp before updating the entity.
	 */
	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
