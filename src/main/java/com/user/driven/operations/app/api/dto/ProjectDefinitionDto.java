package com.user.driven.operations.app.api.dto;

import java.util.List;

import com.user.driven.operations.enums.DatabaseType;
import com.user.driven.operations.enums.SecurityType;

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
public class ProjectDefinitionDto {

	/**
	 * Unique identifier of the project (used mainly for update scenarios).
	 */
	private Long id;

	/**
	 * The name of the project. This field is mandatory.
	 */
	@NotBlank(message = "Project name is required")
	private String name;

	/**
	 * Description of the project.
	 */
	private String description;

	/**
	 * Base package name used for code generation. This field is mandatory.
	 */
	@NotBlank(message = "Package name is required")
	private String packageName;

	/**
	 * Type of database used in the project. Defaults to H2.
	 */
	private DatabaseType databaseType = DatabaseType.H2;

	/**
	 * Indicates whether security is enabled in the project.
	 */
	private boolean securityEnabled = false;

	/**
	 * The type of security used in the project (e.g., JWT, BASIC, SESSION).
	 */
	private SecurityType securityType;

	/**
	 * Indicates whether caching is enabled in the project.
	 */
	private boolean cachingEnabled = false;

	/**
	 * Indicates whether Swagger (OpenAPI documentation) is enabled. Defaults to
	 * true.
	 */
	private boolean swaggerEnabled = true;

	/**
	 * Custom configuration string or script, if any.
	 */
	private String customConfiguration;

	/**
	 * A list of entity definitions associated with the project.
	 */
	private List<EntityDefinitionDto> entities;
}