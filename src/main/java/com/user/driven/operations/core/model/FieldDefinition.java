package com.user.driven.operations.core.model;

import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.FieldType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the definition of a field within an entity in a user-driven
 * project. Includes metadata such as name, data type, constraints,
 * relationships, and default values. Linked to an {@link EntityDefinition}.
 * 
 * Author: Jatin Raheja
 */
@Entity
@Table(name = "field_definitions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FieldDefinition {

	/**
	 * Unique identifier for the field.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Name of the field. Cannot be blank.
	 */
	@NotBlank(message = "Field name is required")
	private String name;

	/**
	 * Optional description providing more detail about the field.
	 */
	private String description;

	/**
	 * The data type of the field (e.g., STRING, INTEGER, DATE).
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "data_type")
	private DataType dataType;

	/**
	 * The field type (e.g., PRIMARY_KEY, FOREIGN_KEY, REGULAR).
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "field_type")
	private FieldType fieldType;

	/**
	 * Validation rules expressed as a string (e.g., min/max length, regex).
	 */
	private String validationRules;

	/**
	 * Specifies the type of relationship (e.g., OneToMany, ManyToOne).
	 */
	private String relationshipType;

	/**
	 * Specifies the target entity of the relationship, if any.
	 */
	private String relationshipTarget;

	/**
	 * Indicates whether this field can be null.
	 */
	private boolean nullable = true;

	/**
	 * Default value for this field.
	 */
	private String defaultValue;

	/**
	 * The entity to which this field belongs.
	 */
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "entity_id")
	private EntityDefinition entity;
}