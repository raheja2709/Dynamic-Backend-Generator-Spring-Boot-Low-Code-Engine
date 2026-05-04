package com.user.driven.operations.core.service;

import java.io.IOException;

/**
 * Service interface for generating and downloading Spring Boot projects based
 * on project definitions. Provides methods to generate project files and
 * download the generated project as a ZIP.
 * 
 * @author Jatin Raheja
 */
public interface ProjectGenerationService {

	/**
	 * Generates a Spring Boot project structure and files based on the provided
	 * project ID.
	 *
	 * @param projectId the ID of the project definition
	 * @return the file path where the project was generated
	 * @throws IOException if an error occurs during project generation
	 */
	String generateProject(Long projectId) throws IOException;

	/**
	 * Downloads the generated project as a ZIP file.
	 *
	 * @param projectId the ID of the project definition
	 * @return a byte array representing the ZIP file content
	 * @throws IOException if an error occurs while creating the ZIP archive
	 */
	byte[] downloadProject(Long projectId) throws IOException;
}