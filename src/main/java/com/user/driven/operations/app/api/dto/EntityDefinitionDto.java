package com.user.driven.operations.app.api.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object for entity definitions.
 * Encapsulates entity metadata including name, description, fields, and operations.
 * Used to transfer data between the client and backend during entity creation and update.
 * 
 * @author: Jatin Raheja
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Entity definition request containing entity metadata, fields, and operations")
public class EntityDefinitionDto {

	/**
	 * The ID of the entity (optional for creation, required for updates).
	 */
	@Schema(description = "Entity ID (used for updates)", example = "1")
	private Long id;

	/**
	 * The name of the entity. This field is mandatory.
	 */
	@NotBlank(message = "Entity name is required")
	@Schema(description = "Name of the entity (used as class name in generated code)", example = "Product", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	/**
	 * A human-readable description of the entity.
	 */
	@Schema(description = "Human-readable description of the entity", example = "Represents a product in the catalog")
	private String description;

	/**
	 * A list of field definitions associated with this entity.
	 */
	@Schema(description = "List of field definitions for this entity")
	private List<FieldDefinitionDto> fields;

	/**
	 * A list of operation configurations (e.g., CREATE, READ, UPDATE, DELETE)
	 * supported by this entity.
	 */
	@Schema(description = "List of operation configurations supported by this entity")
	private List<OperationConfigDto> operations;
}