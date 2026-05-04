package com.user.driven.operations.core.service;

import java.util.List;
import java.util.Optional;

import com.user.driven.operations.api.dto.ProjectDefinitionDto;
import com.user.driven.operations.core.model.ProjectDefinition;

/**
 * Service interface for managing {@link ProjectDefinition}. Provides methods to
 * create, retrieve, update, delete, and validate project definitions.
 *
 * @author Jatin Raheja
 */
public interface ProjectDefinitionService {

	/**
	 * Creates a new project based on the provided DTO.
	 *
	 * @param projectDto the project definition data
	 * @return the created {@link ProjectDefinition}
	 */
	ProjectDefinition createProject(ProjectDefinitionDto projectDto);

	/**
	 * Retrieves a project by its ID.
	 *
	 * @param id the project ID
	 * @return an {@link Optional} containing the found project or empty if not found
	 */
	Optional<ProjectDefinition> getProjectById(Long id);

	/**
	 * Retrieves a project by its ID along with its associated entities.
	 *
	 * @param id the project ID
	 * @return an {@link Optional} containing the found project with entities or empty if not found
	 */
	Optional<ProjectDefinition> getProjectByIdWithEntities(Long id);

	/**
	 * Retrieves all available projects.
	 *
	 * @return a list of {@link ProjectDefinition}
	 */
	List<ProjectDefinition> getAllProjects();

	/**
	 * Updates an existing project with new data.
	 *
	 * @param id         the project ID to update
	 * @param projectDto the updated project data
	 * @return the updated {@link ProjectDefinition}
	 */
	ProjectDefinition updateProject(Long id, ProjectDefinitionDto projectDto);

	/**
	 * Deletes a project by its ID.
	 *
	 * @param id the project ID
	 */
	void deleteProject(Long id);

	/**
	 * Checks if a project with the given name already exists.
	 *
	 * @param name the project name
	 * @return true if a project with the name exists, false otherwise
	 */
	boolean existsByName(String name);
}
