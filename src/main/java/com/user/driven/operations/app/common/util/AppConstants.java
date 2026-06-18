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

	// --- V1 API paths ---

	/** Base endpoint for project-related operations (v1) */
	public static final String PROJECTS_V1 = "/api/v1/projects";

	/** Endpoint for entity definitions under a project (v1) */
	public static final String ENTITIES_V1 = "/api/v1/projects/{projectId}/entities";

	/** Endpoint for generator operations (v1) */
	public static final String GENERATOR_V1 = "/api/v1/generator";

	// --- Legacy paths (kept for backward compatibility) ---

	/** @deprecated Use {@link #ENTITIES_V1} instead */
	@Deprecated
	public static final String entityDefination = "/api/projects/{projectId}/entities";

	/** @deprecated Use {@link #PROJECTS_V1} instead */
	@Deprecated
	public static final String projects = "/api/projects";

	// --- Common path segments ---

	/** Endpoint suffix for referencing by ID */
	public static final String Id = "/{id}";

	/**
	 * Endpoint to fetch detailed entity information including fields and operations
	 */
	public static final String getDetails = "/{id}/details";

	/** Endpoint to trigger Spring Boot project generation */
	public static final String generateProject = "/{id}/generate";

	/** Endpoint to download a generated project as a ZIP file */
	public static final String downloadProject = "/{id}/download";
}
