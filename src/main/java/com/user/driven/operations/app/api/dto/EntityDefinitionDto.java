package com.user.driven.operations.app.api.dto;

import java.util.List;

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
public class EntityDefinitionDto {

	/**
	 * The ID of the entity (optional for creation, required for updates).
	 */
	private Long id;

	/**
	 * The name of the entity. This field is mandatory.
	 */
	@NotBlank(message = "Entity name is required")
	private String name;

	/**
	 * A human-readable description of the entity.
	 */
	private String description;

	/**
	 * A list of field definitions associated with this entity.
	 */
	private List<FieldDefinitionDto> fields;

	/**
	 * A list of operation configurations (e.g., CREATE, READ, UPDATE, DELETE)
	 * supported by this entity.
	 */
	private List<OperationConfigDto> operations;
}