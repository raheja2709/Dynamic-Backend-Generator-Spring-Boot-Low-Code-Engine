package com.user.driven.operations.app.common.util;

/**
 * A utility class that holds constant URL path mappings used across the
 * application. These constants are typically used for mapping REST API
 * endpoints in controller classes.
 * 
 * <p>
 * Helps to maintain consistency and avoid hardcoding URL strings across the
 * codebase.
 * </p>
 * 
 * @author Jatin Raheja
 */
public class AppConstants {

	/** Endpoint for entity definitions under a project */
	public static final String entityDefination = "/api/projects/{projectId}/entities";

	/** Endpoint suffix for referencing by ID */
	public static final String Id = "/{id}";

	/**
	 * Endpoint to fetch detailed entity information including fields and operations
	 */
	public static final String getDetails = "/{id}/details";

	/** Base endpoint for project-related operations */
	public static final String projects = "/api/projects";

	/** Endpoint to trigger Spring Boot project generation */
	public static final String generateProject = "/{id}/generate";

	/** Endpoint to download a generated project as a ZIP file */
	public static final String downloadProject = "/{id}/download";
}
