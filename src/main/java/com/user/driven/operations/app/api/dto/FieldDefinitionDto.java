package com.user.driven.operations.app.api.dto;

import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.FieldType;

import com.user.driven.operations.enums.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing a field definition for an entity.
 * Includes metadata such as name, data type, field type, and additional validation or relationship properties.
 * 
 * @author: Jatin Raheja
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FieldDefinitionDto {

	/**
	 * The unique identifier of the field (used for updates).
	 */
	private Long id;

	/**
	 * The name of the field. This is a required field.
	 */
	@NotBlank(message = "Field name is required")
	private String name;

	/**
	 * A human-readable description of the field.
	 */
	private String description;

	/**
	 * The data type of the field (e.g., STRING, INTEGER). This is a required field.
	 */
	@NotNull
	private DataType dataType;

	/**
	 * The type of the field (e.g., PRIMARY_KEY, FOREIGN_KEY, NORMAL). This is a
	 * required field.
	 */
	@NotNull
	private FieldType fieldType;

	/**
	 * A string containing validation rules (e.g., regex or length constraints).
	 */
	private String validationRules;

	/**
	 * The type of relationship if the field is a foreign key (e.g., ONE_TO_MANY).
	 */
    private RelationshipType relationshipType;

	/**
	 * The target entity name for the relationship (used with relationshipType).
	 */
	private String relationshipTarget;

	/**
	 * Indicates whether the field can be null. Defaults to true.
	 */
	private boolean nullable = true;


	/**
	 * The default value for the field, if any.
	 */
	private String defaultValue;
}