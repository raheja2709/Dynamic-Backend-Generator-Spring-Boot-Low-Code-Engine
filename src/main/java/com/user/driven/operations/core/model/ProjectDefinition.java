package com.user.driven.operations.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.user.driven.operations.enums.DatabaseType;
import com.user.driven.operations.enums.SecurityType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Spring Boot project definition including metadata such as name,
 * package, database type, and configuration options like security, caching, and
 * Swagger. Contains a list of associated entity definitions.
 *
 * This entity is the root configuration for generating a Spring Boot
 * application dynamically.
 *
 * @author Jatin Raheja
 */
@Entity
@Table(name = "project_definitions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProjectDefinition {

	/**
	 * Unique identifier for the project.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Name of the project. Must be unique and not blank.
	 */
	@NotBlank(message = "Project name is required")
	@Column(unique = true)
	private String name;

	/**
	 * Optional project description.
	 */
	private String description;

	/**
	 * Base package name used for code generation. Must not be blank.
	 */
	@NotBlank(message = "Package name is required")
	private String packageName;

	/**
	 * Type of database to use (e.g., H2, MYSQL).
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "database_type", columnDefinition = "varchar(50)")
	private DatabaseType databaseType = DatabaseType.H2;

	/**
	 * Whether security is enabled for the generated project.
	 */
	private boolean securityEnabled = false;

	/**
	 * Type of security mechanism (e.g., JWT, BASIC, SESSION).
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "security_type", columnDefinition = "varchar(50)")
	private SecurityType securityType;

	/**
	 * Whether caching is enabled for the generated application.
	 */
	private boolean cachingEnabled = false;

	/**
	 * Whether Swagger is enabled for API documentation.
	 */
	private boolean swaggerEnabled = true;

	/**
	 * Optional custom configuration block in text or JSON.
	 */
	private String customConfiguration;

	/**
	 * List of entities associated with this project.
	 */
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<EntityDefinition> entities = new ArrayList<>();

	/**
	 * Timestamp for when the project was created.
	 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * Timestamp for when the project was last updated.
	 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Automatically sets timestamps before the project is persisted.
	 */
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	/**
	 * Automatically updates the timestamp when the project is updated.
	 */
	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

}