package com.user.driven.operations.app.api.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.driven.operations.app.api.dto.EntityDefinitionDto;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.service.EntityDefinitionService;
import com.user.driven.operations.app.common.util.AppConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


/**
 * Controller for managing entity definitions within a project.
 * Provides endpoints to create, retrieve, update, and delete entities.
 * 
 * @author Jatin Raheja
 */
@RestController
@RequestMapping(AppConstants.entityDefination)
@Tag(name = "Entity Management", description = "APIs for managing entity definitions")
@RequiredArgsConstructor
public class EntityDefinitionController {

	private final EntityDefinitionService entityService;

	/**
	 * Creates a new entity for a given project.
	 *
	 * @param projectId the ID of the project
	 * @param entityDto the entity definition to create
	 * @return the created entity
	 */
	@PostMapping
	@Operation(summary = "Create a new entity")
	public ResponseEntity<EntityDefinition> createEntity(@PathVariable Long projectId,
			@Valid @RequestBody EntityDefinitionDto entityDto) {
		EntityDefinition entity = entityService.createEntity(projectId, entityDto);
		return new ResponseEntity<>(entity, HttpStatus.CREATED);
	}

	/**
	 * Retrieves all entities for a specific project.
	 *
	 * @param projectId the ID of the project
	 * @return list of entity definitions
	 */
	@GetMapping
	@Operation(summary = "Get all entities for a project")
	public ResponseEntity<List<EntityDefinition>> getEntitiesByProject(@PathVariable Long projectId) {
		List<EntityDefinition> entities = entityService.getEntitiesByProjectId(projectId);
		return ResponseEntity.ok(entities);
	}

	/**
	 * Retrieves an entity by its ID.
	 *
	 * @param projectId the ID of the project
	 * @param id        the ID of the entity
	 * @return the entity definition, or 404 if not found
	 */
	@GetMapping(AppConstants.Id)
	@Operation(summary = "Get entity by ID")
	public ResponseEntity<EntityDefinition> getEntityById(@PathVariable Long projectId, @PathVariable Long id) {
		return entityService.getEntityById(id).map(entity -> ResponseEntity.ok(entity))
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Retrieves an entity along with its fields and operations.
	 *
	 * @param projectId the ID of the project
	 * @param id        the ID of the entity
	 * @return the detailed entity definition, or 404 if not found
	 */
	@GetMapping(AppConstants.getDetails)
	@Operation(summary = "Get entity with fields and operations")
	public ResponseEntity<EntityDefinition> getEntityWithDetails(@PathVariable Long projectId, @PathVariable Long id) {
		return entityService.getEntityByIdWithFieldsAndOperations(id).map(entity -> ResponseEntity.ok(entity))
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Updates an existing entity.
	 *
	 * @param projectId the ID of the project
	 * @param id        the ID of the entity
	 * @param entityDto the updated entity data
	 * @return the updated entity, or 404 if not found
	 */
	@PutMapping(AppConstants.Id)
	@Operation(summary = "Update entity")
	public ResponseEntity<EntityDefinition> updateEntity(@PathVariable Long projectId, @PathVariable Long id,
			@Valid @RequestBody EntityDefinitionDto entityDto) {
		try {
			EntityDefinition updatedEntity = entityService.updateEntity(id, entityDto);
			return ResponseEntity.ok(updatedEntity);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Deletes an entity by its ID.
	 *
	 * @param projectId the ID of the project
	 * @param id        the ID of the entity
	 * @return no content response if successful, 404 if not found
	 */
	@DeleteMapping(AppConstants.Id)
	@Operation(summary = "Delete entity")
	public ResponseEntity<Void> deleteEntity(@PathVariable Long projectId, @PathVariable Long id) {
		try {
			entityService.deleteEntity(id);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}
}