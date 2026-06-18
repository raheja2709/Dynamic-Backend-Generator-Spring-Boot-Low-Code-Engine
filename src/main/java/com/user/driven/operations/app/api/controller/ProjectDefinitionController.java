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
import com.user.driven.operations.app.api.dto.PaginationMeta;
import com.user.driven.operations.app.api.dto.ProjectDefinitionDto;
import com.user.driven.operations.app.common.exception.ProjectNotFoundException;
import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.common.util.AppConstants;
import com.user.driven.operations.app.common.util.MessageConstants;
import com.user.driven.operations.app.common.util.RequestContext;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.service.ProjectDefinitionService;
import com.user.driven.operations.enums.DatabaseType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for managing project definitions.
 * Provides RESTful endpoints to create, read, update, and delete projects.
 *
 * @author Jatin Raheja
 */
@RestController
@RequestMapping(AppConstants.PROJECTS_V1)
@Tag(name = "Project Management", description = "APIs for managing project definitions")
@RequiredArgsConstructor
public class ProjectDefinitionController {

    private final ProjectDefinitionService projectService;

    /**
     * Creates a new project.
     *
     * @param projectDto the project definition data
     * @return the created project wrapped in ApiEnvelope
     */
    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project definition with the specified configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
            @ApiResponse(responseCode = "409", description = "Project name already exists")
    })
    public ResponseEntity<ApiEnvelope<ProjectDefinition>> createProject(
            @Valid @RequestBody ProjectDefinitionDto projectDto) {
        ProjectDefinition project = projectService.createProject(projectDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiEnvelope.success(project, RequestContext.getRequestId()));
    }

    /**
     * Retrieves all projects with pagination and optional filtering support.
     *
     * @param page         the page number (zero-based, default 0)
     * @param size         the page size (default 20, min 1, max 100)
     * @param sort         the sort parameter in format "fieldName,asc|desc" (default: createdAt,desc)
     * @param name         optional name filter (case-insensitive partial match)
     * @param databaseType optional database type filter (exact match)
     * @return paginated list of projects wrapped in ApiEnvelope
     */
    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieves a paginated list of all projects with optional filtering by name and database type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    })
    public ResponseEntity<ApiEnvelope<List<ProjectDefinition>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) DatabaseType databaseType) {

        if (page < 0) {
            throw new ValidationException(MessageConstants.PAGE_MUST_BE_NON_NEGATIVE);
        }
        if (size < 1 || size > 100) {
            throw new ValidationException(MessageConstants.SIZE_OUT_OF_RANGE);
        }

        Pageable pageable = PageRequest.of(page, size, parseSortParam(sort));
        Page<ProjectDefinition> result = projectService.getAllProjects(name, databaseType, pageable);
        PaginationMeta pagination = PaginationMeta.of(result);

        return ResponseEntity.ok(
                ApiEnvelope.success(result.getContent(), pagination, RequestContext.getRequestId()));
    }

    /**
     * Retrieves a project by its ID.
     *
     * @param id the ID of the project
     * @return the project wrapped in ApiEnvelope
     */
    @GetMapping(AppConstants.Id)
    @Operation(summary = "Get project by ID", description = "Retrieves a project by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiEnvelope<ProjectDefinition>> getProjectById(@PathVariable Long id) {
        ProjectDefinition project = projectService.getProjectById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project", id.toString()));
        return ResponseEntity.ok(ApiEnvelope.success(project, RequestContext.getRequestId()));
    }

    /**
     * Retrieves a project with its associated entities by ID.
     *
     * @param id the ID of the project
     * @return the project with entity details wrapped in ApiEnvelope
     */
    @GetMapping(AppConstants.getDetails)
    @Operation(summary = "Get project with entities by ID", description = "Retrieves a project along with its associated entity definitions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project with entities retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiEnvelope<ProjectDefinition>> getProjectWithEntities(@PathVariable Long id) {
        ProjectDefinition project = projectService.getProjectByIdWithEntities(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project", id.toString()));
        return ResponseEntity.ok(ApiEnvelope.success(project, RequestContext.getRequestId()));
    }

    /**
     * Updates an existing project by ID.
     *
     * @param id         the ID of the project
     * @param projectDto the updated project data
     * @return the updated project wrapped in ApiEnvelope
     */
    @PutMapping(AppConstants.Id)
    @Operation(summary = "Update project", description = "Updates an existing project definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Project name already exists")
    })
    public ResponseEntity<ApiEnvelope<ProjectDefinition>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDefinitionDto projectDto) {
        ProjectDefinition updatedProject = projectService.updateProject(id, projectDto);
        return ResponseEntity.ok(ApiEnvelope.success(updatedProject, RequestContext.getRequestId()));
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id the ID of the project
     * @return no-content response wrapped in ApiEnvelope
     */
    @DeleteMapping(AppConstants.Id)
    @Operation(summary = "Delete project", description = "Deletes a project and all its associated entities")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiEnvelope<Void>> deleteProject(@PathVariable Long id) {
        // Verify project exists before deleting
        projectService.getProjectById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project", id.toString()));
        projectService.deleteProject(id);
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
