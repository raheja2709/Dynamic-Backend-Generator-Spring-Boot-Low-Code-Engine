package com.user.driven.operations.app.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.OperationConfig;

/**
 * Repository interface for managing {@link OperationConfig} entities.
 * Provides methods to perform CRUD operations and custom queries on operation configurations.
 *
 * @author: Jatin Raheja
 */
@Repository
public interface OperationConfigRepository extends JpaRepository<OperationConfig, Long> {

	/**
	 * Retrieves all operation configurations associated with a given entity ID.
	 *
	 * @param entityId the ID of the entity
	 * @return list of operation configurations for the specified entity
	 */
	List<OperationConfig> findByEntityId(Long entityId);

	/**
	 * Retrieves all operation configurations associated with a given entity ID and
	 * enabled status.
	 *
	 * @param entityId the ID of the entity
	 * @param enabled  flag indicating whether to fetch only enabled or disabled operations
	 * @return list of operation configurations matching the criteria
	 */
	List<OperationConfig> findByEntityIdAndEnabled(Long entityId, boolean enabled);
}