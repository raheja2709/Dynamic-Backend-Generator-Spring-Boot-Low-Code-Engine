package com.user.driven.operations.app.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.FieldDefinition;

/**
 * Repository interface for managing {@link FieldDefinition} entities.
 * Provides methods to perform CRUD operations and custom queries on field definitions.
 * This interface extends {@link JpaRepository}, allowing standard Spring Data JPA functionality.
 * 
 * @author: Jatin Raheja
 */
@Repository
public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, Long> {

	/**
	 * Retrieves all field definitions associated with a given entity ID.
	 *
	 * @param entityId the ID of the entity
	 * @return list of field definitions belonging to the specified entity
	 */
	List<FieldDefinition> findByEntityId(Long entityId);


	/**
	 * Checks if a field with the specified name exists within a given entity.
	 *
	 * @param name     the name of the field
	 * @param entityId the ID of the entity
	 * @return true if such a field exists, false otherwise
	 */
	boolean existsByNameAndEntityId(String name, Long entityId);
}
