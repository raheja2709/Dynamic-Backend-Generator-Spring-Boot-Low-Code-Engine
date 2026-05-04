package com.user.driven.operations.core.service;

import java.util.List;
import java.util.Optional;

import com.user.driven.operations.api.dto.EntityDefinitionDto;
import com.user.driven.operations.core.model.EntityDefinition;

/**
 * Service interface for managing {@link EntityDefinition} operations. Defines
 * methods for creating, retrieving, updating, and deleting entities, as well as
 * checking their existence by name within a specific project.
 * 
 * This service acts as an abstraction layer between the controller and
 * repository layers.
 * 
 * @author: Jatin Raheja
 */
public interface EntityDefinitionService {

	/**
	 * Creates a new entity definition for the specified project.
	 *
	 * @param projectId the ID of the project to which the entity belongs
	 * @param entityDto the DTO containing entity details
	 * @return the created {@link EntityDefinition}
	 */
	EntityDefinition createEntity(Long projectId, EntityDefinitionDto entityDto);

	/**
	 * Retrieves an entity by its ID.
	 *
	 * @param id the ID of the entity
	 * @return an {@link Optional} containing the entity if found, or empty otherwise
	 */
	Optional<EntityDefinition> getEntityById(Long id);

	/**
	 * Retrieves an entity along with its associated fields and operations.
	 *
	 * @param id the ID of the entity
	 * @return an {@link Optional} containing the full entity if found, or empty otherwise
	 */
	Optional<EntityDefinition> getEntityByIdWithFieldsAndOperations(Long id);

	/**
	 * Retrieves all entities associated with a given project ID.
	 *
	 * @param projectId the ID of the project
	 * @return a list of {@link EntityDefinition}
	 */
	List<EntityDefinition> getEntitiesByProjectId(Long projectId);

	/**
	 * Updates an existing entity with new details.
	 *
	 * @param id        the ID of the entity to update
	 * @param entityDto the DTO containing updated entity details
	 * @return the updated {@link EntityDefinition}
	 */
	EntityDefinition updateEntity(Long id, EntityDefinitionDto entityDto);

	/**
	 * Deletes an entity by its ID.
	 *
	 * @param id the ID of the entity to delete
	 */
	void deleteEntity(Long id);

	/**
	 * Checks if an entity with the specified name exists within a project.
	 *
	 * @param name      the name of the entity
	 * @param projectId the ID of the project
	 * @return true if the entity exists, false otherwise
	 */
	boolean existsByNameAndProjectId(String name, Long projectId);
}