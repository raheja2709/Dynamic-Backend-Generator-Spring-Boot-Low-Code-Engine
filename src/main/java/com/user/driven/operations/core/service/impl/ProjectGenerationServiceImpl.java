package com.user.driven.operations.core.service.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.user.driven.operations.generator.service.impl.CodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.user.driven.operations.core.model.ProjectDefinition;
import com.user.driven.operations.core.service.ProjectDefinitionService;
import com.user.driven.operations.core.service.ProjectGenerationService;
import com.user.driven.operations.common.util.FileUtils;

/**
 * Implementation of {@link ProjectGenerationService} for generating and downloading
 * Spring Boot project source code based on the provided project definition.
 * 
 * This service handles generating the directory structure, writing code files, and
 * compressing the project for download.
 * 
 * @author Jatin Raheja
 */
@Service
public class ProjectGenerationServiceImpl implements ProjectGenerationService {

	@Autowired
	private ProjectDefinitionService projectService;

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private FileUtils fileUtils;

	@Value("${app.generated-projects.directory}")
	private String generatedProjectsDirectory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateProject(Long projectId) throws IOException {
		ProjectDefinition project = projectService.getProjectByIdWithEntities(projectId)
				.orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

		Path projectPath = Paths.get(generatedProjectsDirectory, project.getName());

		// Generate project structure and files
		codeGenerator.generateProject(project, projectPath);

		return projectPath.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] downloadProject(Long projectId) throws IOException {
		String projectPath = generateProject(projectId);
		return fileUtils.createZipFile(Paths.get(projectPath));
	}
}