package com.user.driven.operations.app.api.dto;

import com.user.driven.operations.enums.OperationType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing configuration for a specific operation
 * (such as CREATE, READ, UPDATE, DELETE) on an entity.
 * 
 * Includes optional flags for enabling/disabling operations and supporting
 * custom logic.
 * 
 * @author: Jatin Raheja
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class OperationConfigDto {

	/**
	 * Unique identifier for the operation configuration (useful for updates).
	 */
	private Long id;

	/**
	 * The type of operation this configuration applies to. This is a required field
	 * and should not be null.
	 */
	@NotNull
	private OperationType operationType;

	/**
	 * Indicates whether this operation is enabled for the entity. Defaults to true.
	 */
	private boolean enabled = true;

	/**
	 * Optional field to define custom logic for this operation. Could include
	 * service or logic layer references.
	 */
	private String customLogic;

	/**
	 * Optional parameters required by the operation or its custom logic.
	 */
	private String parameters;
}