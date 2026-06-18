package com.user.driven.operations.app.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.RelationshipDefinition;

/**
 * Repository interface for {@link RelationshipDefinition} entity.
 * Provides methods for performing CRUD operations and custom queries
 * related to entity relationship definitions.
 *
 * @author Jatin Raheja
 */
@Repository
public interface RelationshipDefinitionRepository extends JpaRepository<RelationshipDefinition, Long> {

    /**
     * Finds all relationship definitions for a given entity.
     *
     * @param entityId the ID of the entity
     * @return a list of relationship definitions belonging to the entity
     */
    List<RelationshipDefinition> findByEntityId(Long entityId);

    /**
     * Deletes all relationship definitions for a given entity.
     *
     * @param entityId the ID of the entity
     */
    void deleteByEntityId(Long entityId);

    /**
     * Counts the number of relationship definitions for a given entity.
     *
     * @param entityId the ID of the entity
     * @return the count of relationship definitions
     */
    long countByEntityId(Long entityId);
}
