package com.user.driven.operations.app.api.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.driven.operations.app.api.dto.ApiEnvelope;
import com.user.driven.operations.app.api.dto.EntityDefinitionDto;
import com.user.driven.operations.app.api.dto.PaginationMeta;
import com.user.driven.operations.app.common.exception.ProjectNotFoundException;
import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.common.util.AppConstants;
import com.user.driven.operations.app.common.util.MessageConstants;
import com.user.driven.operations.app.common.util.RequestContext;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.service.EntityDefinitionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for managing entity definitions within a project.
 * Provides endpoints to create, retrieve, update, and delete entities.
 *
 * @author Jatin Raheja
 */
@RestController
@RequestMapping(AppConstants.ENTITIES_V1)
@Tag(name = "Entity Management", description = "APIs for managing entity definitions")
@RequiredArgsConstructor
public class EntityDefinitionController {

    private final EntityDefinitionService entityService;

    /**
     * Creates a new entity for a given project.
     *
     * @param projectId the ID of the project
     * @param entityDto the entity definition to create
     * @return the created entity wrapped in ApiEnvelope
     */
    @PostMapping
    @Operation(summary = "Create a new entity", description = "Creates a new entity definition for the specified project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
            @ApiResponse(responseCode = "409", description = "Entity name already exists in project")
    })
    public ResponseEntity<ApiEnvelope<EntityDefinition>> createEntity(
            @PathVariable Long projectId,
            @Valid @RequestBody EntityDefinitionDto entityDto) {
        EntityDefinition entity = entityService.createEntity(projectId, entityDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.success(entity, RequestContext.getRequestId()));
    }

    /**
     * Retrieves all entities for a specific project with pagination support.
     *
     * @param projectId the ID of the project
     * @param page the page number (zero-based, default 0)
     * @param size the page size (default 20, min 1, max 100)
     * @param sort the sort parameter in format "fieldName,asc|desc" (default: createdAt,desc)
     * @return paginated list of entity definitions wrapped in ApiEnvelope
     */
    @GetMapping
    @Operation(summary = "Get all entities for a project", description = "Retrieves a paginated list of entities belonging to the specified project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entities retrieved successfully")
    })
    public ResponseEntity<ApiEnvelope<List<EntityDefinition>>> getEntitiesByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        if (page < 0) {
            throw new ValidationException(MessageConstants.PAGE_MUST_BE_NON_NEGATIVE);
        }
        if (size < 1 || size > 100) {
            throw new ValidationException(MessageConstants.SIZE_OUT_OF_RANGE);
        }

        Pageable pageable = PageRequest.of(page, size, parseSortParam(sort));
        Page<EntityDefinition> result = entityService.getEntitiesByProjectId(projectId, pageable);
        PaginationMeta pagination = PaginationMeta.of(result);

        return ResponseEntity.ok(
                ApiEnvelope.success(result.getContent(), pagination, RequestContext.getRequestId()));
    }

    /**
     * Retrieves an entity by its ID.
     *
     * @param projectId the ID of the project
     * @param id        the ID of the entity
     * @return the entity definition wrapped in ApiEnvelope
     */
    @GetMapping(AppConstants.Id)
    @Operation(summary = "Get entity by ID", description = "Retrieves an entity definition by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<ApiEnvelope<EntityDefinition>> getEntityById(
            @PathVariable Long projectId, @PathVariable Long id) {
        EntityDefinition entity = entityService.getEntityById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Entity", id.toString()));
        return ResponseEntity.ok(ApiEnvelope.success(entity, RequestContext.getRequestId()));
    }

    /**
     * Retrieves an entity along with its fields and operations.
     *
     * @param projectId the ID of the project
     * @param id        the ID of the entity
     * @return the detailed entity definition wrapped in ApiEnvelope
     */
    @GetMapping(AppConstants.getDetails)
    @Operation(summary = "Get entity with fields and operations", description = "Retrieves an entity along with its field definitions and operation configurations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity with details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<ApiEnvelope<EntityDefinition>> getEntityWithDetails(
            @PathVariable Long projectId, @PathVariable Long id) {
        EntityDefinition entity = entityService.getEntityByIdWithFieldsAndOperations(id)
                .orElseThrow(() -> new ProjectNotFoundException("Entity", id.toString()));
        return ResponseEntity.ok(ApiEnvelope.success(entity, RequestContext.getRequestId()));
    }

    /**
     * Updates an existing entity.
     *
     * @param projectId the ID of the project
     * @param id        the ID of the entity
     * @param entityDto the updated entity data
     * @return the updated entity wrapped in ApiEnvelope
     */
    @PutMapping(AppConstants.Id)
    @Operation(summary = "Update entity", description = "Updates an existing entity definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<ApiEnvelope<EntityDefinition>> updateEntity(
            @PathVariable Long projectId, @PathVariable Long id,
            @Valid @RequestBody EntityDefinitionDto entityDto) {
        EntityDefinition updatedEntity = entityService.updateEntity(id, entityDto);
        return ResponseEntity.ok(ApiEnvelope.success(updatedEntity, RequestContext.getRequestId()));
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param projectId the ID of the project
     * @param id        the ID of the entity
     * @return success response wrapped in ApiEnvelope
     */
    @DeleteMapping(AppConstants.Id)
    @Operation(summary = "Delete entity", description = "Deletes an entity and all its associated fields and operations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<ApiEnvelope<Void>> deleteEntity(
            @PathVariable Long projectId, @PathVariable Long id) {
        // Verify entity exists before deleting
        entityService.getEntityById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Entity", id.toString()));
        entityService.deleteEntity(id);
        return ResponseEntity.ok(ApiEnvelope.success(null, RequestContext.getRequestId()));
    }

    /**
     * Parses a sort parameter string in the format "fieldName,asc|desc".
     *
     * @param sort the sort string (e.g., "createdAt,desc")
     * @return a Sort object representing the requested sort order
     */
    private Sort parseSortParam(String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
