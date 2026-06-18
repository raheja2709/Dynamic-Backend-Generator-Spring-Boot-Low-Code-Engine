package com.user.driven.operations.app.api.dto;

import java.util.List;

import com.user.driven.operations.enums.DatabaseType;
import com.user.driven.operations.enums.SecurityType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object representing the structure and configuration of a
 * project. This includes metadata like name, package, and features such as
 * database type, security configuration, caching, Swagger, and associated
 * entities.
 * 
 * @author: Jatin Raheja
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Project definition request containing project configuration and metadata")
public class ProjectDefinitionDto {

	/**
	 * Unique identifier of the project (used mainly for update scenarios).
	 */
	@Schema(description = "Project ID (used for updates)", example = "1")
	private Long id;

	/**
	 * The name of the project. This field is mandatory.
	 */
	@NotBlank(message = "Project name is required")
	@Schema(description = "Unique name of the project", example = "ECommerceApp", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	/**
	 * Description of the project.
	 */
	@Schema(description = "Human-readable description of the project", example = "An e-commerce backend application")
	private String description;

	/**
	 * Base package name used for code generation. This field is mandatory.
	 */
	@NotBlank(message = "Package name is required")
	@Schema(description = "Base Java package name for code generation", example = "com.example.ecommerce", requiredMode = Schema.RequiredMode.REQUIRED)
	private String packageName;

	/**
	 * Type of database used in the project. Defaults to H2.
	 */
	@Schema(description = "Database type for the generated project", example = "POSTGRESQL")
	private DatabaseType databaseType = DatabaseType.H2;

	/**
	 * Indicates whether security is enabled in the project.
	 */
	@Schema(description = "Whether security is enabled in the generated project", example = "true")
	private boolean securityEnabled = false;

	/**
	 * The type of security used in the project (e.g., JWT, BASIC, SESSION).
	 */
	@Schema(description = "Security type when security is enabled (JWT, BASIC_AUTH, OAUTH2, SESSION_BASED)", example = "JWT")
	private SecurityType securityType;

	/**
	 * Indicates whether caching is enabled in the project.
	 */
	@Schema(description = "Whether caching is enabled in the generated project", example = "false")
	private boolean cachingEnabled = false;

	/**
	 * Indicates whether Swagger (OpenAPI documentation) is enabled. Defaults to
	 * true.
	 */
	@Schema(description = "Whether Swagger/OpenAPI documentation is enabled", example = "true")
	private boolean swaggerEnabled = true;

	/**
	 * Custom configuration string or script, if any.
	 */
	@Schema(description = "Custom configuration JSON or script")
	private String customConfiguration;

	/**
	 * A list of entity definitions associated with the project.
	 */
	@Schema(description = "List of entity definitions for the project")
	private List<EntityDefinitionDto> entities;
}