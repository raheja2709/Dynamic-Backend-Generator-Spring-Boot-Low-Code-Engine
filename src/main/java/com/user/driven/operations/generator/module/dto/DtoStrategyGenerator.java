package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.app.core.model.RelationshipDefinition;

/**
 * Interface for generating DTO field representations based on relationship strategy.
 * Each implementation handles a specific DtoStrategy (ID_ONLY, SUMMARY, NESTED, IGNORE).
 *
 * @author Jatin Raheja
 */
public interface DtoStrategyGenerator {

    /**
     * Generates the DTO field declaration(s) for a relationship.
     *
     * @param rel the relationship definition
     * @return the Java field declaration(s) as a string, or empty string for IGNORE
     */
    String generateField(RelationshipDefinition rel);

    /**
     * Returns any additional imports required by this strategy.
     *
     * @param rel the relationship definition
     * @return import statements (without "import " prefix), or empty string
     */
    String generateImports(RelationshipDefinition rel);
}
