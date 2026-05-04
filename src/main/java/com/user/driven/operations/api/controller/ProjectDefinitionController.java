package com.user.driven.operations.api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.driven.operations.api.dto.ProjectDefinitionDto;
import com.user.driven.operations.core.model.ProjectDefinition;
import com.user.driven.operations.core.service.ProjectDefinitionService;
import com.user.driven.operations.core.service.ProjectGenerationService;
import com.user.driven.operations.common.util.AppConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for managing project definitions.
 * Provides RESTful endpoints to create, read, update, delete, generate, and download projects.
 * 
 * @author Jatin Raheja
 */
@RestController
@RequestMapping(AppConstants.projects)
@Tag(name = "Project Management", description = "APIs for managing project definitions")
public class ProjectDefinitionController {

	@Autowired
	private ProjectDefinitionService projectService;

	@Autowired
	private ProjectGenerationService generationService;

	/**
	 * Creates a new project.
	 *
	 * @param projectDto the project definition data
	 * @return the created project
	 */
	@PostMapping
	@Operation(summary = "Create a new project")
	public ResponseEntity<ProjectDefinition> createProject(@Valid @RequestBody ProjectDefinitionDto projectDto) {
		ProjectDefinition project = projectService.createProject(projectDto);
		return new ResponseEntity<>(project, HttpStatus.CREATED);
	}

	/**
	 * Retrieves all projects.
	 *
	 * @return list of all projects
	 */
	@GetMapping
	@Operation(summary = "Get all projects")
	public ResponseEntity<List<ProjectDefinition>> getAllProjects() {
		List<ProjectDefinition> projects = projectService.getAllProjects();
		return ResponseEntity.ok(projects);
	}

	/**
	 * Retrieves a project by its ID.
	 *
	 * @param id the ID of the project
	 * @return the project if found, otherwise 404
	 */
	@GetMapping(AppConstants.Id)
	@Operation(summary = "Get project by ID")
	public ResponseEntity<ProjectDefinition> getProjectById(@PathVariable Long id) {
		return projectService.getProjectById(id).map(project -> ResponseEntity.ok(project))
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Retrieves a project with its associated entities by ID.
	 *
	 * @param id the ID of the project
	 * @return the project with entity details if found, otherwise 404
	 */
	@GetMapping(AppConstants.getDetails)
	@Operation(summary = "Get project with entities by ID")
	public ResponseEntity<ProjectDefinition> getProjectWithEntities(@PathVariable Long id) {
		return projectService.getProjectByIdWithEntities(id).map(project -> ResponseEntity.ok(project))
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Updates an existing project by ID.
	 *
	 * @param id         the ID of the project
	 * @param projectDto the updated project data
	 * @return the updated project or 404 if not found
	 */
	@PutMapping(AppConstants.Id)
	@Operation(summary = "Update project")
	public ResponseEntity<ProjectDefinition> updateProject(@PathVariable Long id,
			@Valid @RequestBody ProjectDefinitionDto projectDto) {
		try {
			ProjectDefinition updatedProject = projectService.updateProject(id, projectDto);
			return ResponseEntity.ok(updatedProject);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Deletes a project by its ID.
	 *
	 * @param id the ID of the project
	 * @return no content if deleted, or 404 if not found
	 */
	@DeleteMapping(AppConstants.Id)
	@Operation(summary = "Delete project")
	public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
		try {
			projectService.deleteProject(id);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Generates the Spring Boot project based on the given project ID.
	 *
	 * @param id the ID of the project to generate
	 * @return the file path where the project was generated
	 */
	@PostMapping(AppConstants.generateProject)
	@Operation(summary = "Generate Spring Boot project")
	public ResponseEntity<String> generateProject(@PathVariable Long id) {
		try {
			String projectPath = generationService.generateProject(id);
			return ResponseEntity.ok("Project generated successfully at: " + projectPath);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error generating project: " + e.getMessage());
		}
	}

	/**
	 * Downloads the generated Spring Boot project as a ZIP file.
	 *
	 * @param id the ID of the project to download
	 * @return a ZIP file of the generated project
	 */
	@GetMapping(AppConstants.downloadProject)
	@Operation(summary = "Download generated project as ZIP")
	public ResponseEntity<ByteArrayResource> downloadProject(@PathVariable Long id) {
		try {
			byte[] zipData = generationService.downloadProject(id);
			ByteArrayResource resource = new ByteArrayResource(zipData);

			ProjectDefinition project = projectService.getProjectById(id)
					.orElseThrow(() -> new RuntimeException("Project not found"));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + project.getName() + ".zip")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(zipData.length).body(resource);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}