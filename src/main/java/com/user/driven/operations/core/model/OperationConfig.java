package com.user.driven.operations.core.model;

import com.user.driven.operations.enums.OperationType;

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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an operation configuration for an entity, such as CREATE, READ,
 * UPDATE, or DELETE. Each configuration defines the type of operation, whether
 * it's enabled, and any custom logic or parameters. This entity is associated
 * with an {@link EntityDefinition}.
 *
 * @author: Jatin Raheja
 */
@Entity
@Table(name = "operation_configs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class OperationConfig {

	/**
	 * Unique identifier for the operation configuration.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The type of operation (e.g., CREATE, READ, UPDATE, DELETE).
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type")
	private OperationType operationType;

	/**
	 * Indicates whether the operation is enabled for the entity.
	 */
	private boolean enabled = true;

	/**
	 * Optional field to specify custom logic (e.g., method names or code snippets).
	 */
	private String customLogic;

	/**
	 * Optional parameters for the operation, if needed.
	 */
	private String parameters;

	/**
	 * The entity to which this operation configuration belongs.
	 */
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "entity_id")
	private EntityDefinition entity;
}