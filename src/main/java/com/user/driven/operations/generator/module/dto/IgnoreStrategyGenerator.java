package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.app.core.model.RelationshipDefinition;
import org.springframework.stereotype.Component;

/**
 * IGNORE strategy: omits the relationship field entirely from the DTO.
 *
 * @author Jatin Raheja
 */
@Component
public class IgnoreStrategyGenerator implements DtoStrategyGenerator {

    @Override
    public String generateField(RelationshipDefinition rel) {
        return "";
    }

    @Override
    public String generateImports(RelationshipDefinition rel) {
        return "";
    }
}
