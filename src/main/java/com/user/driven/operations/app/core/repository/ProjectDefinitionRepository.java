package com.user.driven.operations.app.core.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.DatabaseType;

/**
 * Repository interface for {@link ProjectDefinition} entity. Provides methods
 * for performing CRUD operations and custom queries related to project
 * definitions.
 * 
 * @author Jatin Raheja
 */
@Repository
public interface ProjectDefinitionRepository extends JpaRepository<ProjectDefinition, Long> {

	/**
	 * Finds a project by its name.
	 *
	 * @param name the name of the project
	 * @return an {@link Optional} containing the project if found, or empty otherwise
	 */
	Optional<ProjectDefinition> findByName(String name);

	/**
	 * Checks whether a project with the given name exists.
	 *
	 * @param name the name of the project
	 * @return true if the project exists, false otherwise
	 */
	boolean existsByName(String name);

	/**
	 * Retrieves a project along with its associated entities using eager fetch.
	 *
	 * @param id the ID of the project
	 * @return an {@link Optional} containing the project with entities if found, or empty otherwise
	 */
	@Query("SELECT DISTINCT p FROM ProjectDefinition p LEFT JOIN FETCH p.entities WHERE p.id = :id")
	Optional<ProjectDefinition> findByIdWithEntities(@Param("id") Long id);

	/**
	 * Finds projects filtered by optional name (case-insensitive partial match)
	 * and optional databaseType (exact match).
	 *
	 * @param name         optional name filter (partial, case-insensitive)
	 * @param databaseType optional database type filter (exact match)
	 * @param pageable     pagination and sorting parameters
	 * @return a {@link Page} of matching {@link ProjectDefinition}
	 */
	@Query("SELECT p FROM ProjectDefinition p WHERE " +
			"(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
			"(:databaseType IS NULL OR p.databaseType = :databaseType)")
	Page<ProjectDefinition> findByFilters(
			@Param("name") String name,
			@Param("databaseType") DatabaseType databaseType,
			Pageable pageable);

}
