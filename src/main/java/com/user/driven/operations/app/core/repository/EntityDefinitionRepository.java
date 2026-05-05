package com.user.driven.operations.app.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.EntityDefinition;

/**
 * Repository interface for managing {@link EntityDefinition} entities.
 * Provides methods to perform CRUD operations and custom queries on entity definitions.
 * This interface extends {@link JpaRepository}, allowing standard Spring Data JPA functionality.
 * 
 * @author: Jatin Raheja
 */
@Repository
public interface EntityDefinitionRepository extends JpaRepository<EntityDefinition, Long> {

	/**
	 * Retrieves a list of entity definitions for a given project ID.
	 *
	 * @param projectId the ID of the project
	 * @return list of entity definitions belonging to the specified project
	 */
	List<EntityDefinition> findByProjectId(Long projectId);

	/**
	 * Finds an entity definition by its name and associated project ID.
	 *
	 * @param name      the name of the entity
	 * @param projectId the ID of the project
	 * @return an Optional containing the found entity definition if present
	 */
	Optional<EntityDefinition> findByNameAndProjectId(String name, Long projectId);

	/**
	 * Retrieves an entity definition along with its associated fields using a fetch
	 * join.
	 *
	 * @param id the ID of the entity
	 * @return an Optional containing the entity definition with fields if present
	 */
	@Query("SELECT DISTINCT e FROM EntityDefinition e LEFT JOIN FETCH e.fields WHERE e.id = :id")
	Optional<EntityDefinition> findByIdWithFields(@Param("id") Long id);


	/**
	 * Retrieves an entity definition along with its associated operations using a
	 * fetch join.
	 *
	 * @param id the ID of the entity
	 * @return an Optional containing the entity definition with operations if
	 *         present
	 */
	@Query("SELECT DISTINCT e FROM EntityDefinition e LEFT JOIN FETCH e.operations WHERE e.id = :id")
	Optional<EntityDefinition> findByIdWithOperations(@Param("id") Long id);

	/**
	 * Retrieves entity definitions by project ID without fetching associated
	 * collections.
	 *
	 * @param projectId the ID of the project
	 * @return list of entity definitions for the specified project
	 */
	@Query("SELECT DISTINCT e FROM EntityDefinition e WHERE e.project.id = :projectId")
	List<EntityDefinition> findByProjectIdWithoutCollections(@Param("projectId") Long projectId);

	/**
	 * Checks if an entity with the specified name already exists within a project.
	 *
	 * @param name      the name of the entity
	 * @param projectId the ID of the project
	 * @return true if such an entity exists, false otherwise
	 */
	boolean existsByNameAndProjectId(String name, Long projectId);
}