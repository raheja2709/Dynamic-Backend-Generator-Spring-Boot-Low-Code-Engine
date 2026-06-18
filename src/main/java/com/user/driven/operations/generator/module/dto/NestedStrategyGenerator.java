package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.RelationshipType;
import org.springframework.stereotype.Component;

/**
 * NESTED strategy: embeds the full DetailDto of the target entity one level deep.
 * Nested relationships within the embedded DTO default to ID_ONLY to prevent
 * infinite recursion.
 *
 * @author Jatin Raheja
 */
@Component
public class NestedStrategyGenerator implements DtoStrategyGenerator {

    @Override
    public String generateField(RelationshipDefinition rel) {
        String targetName = rel.getTargetEntity();
        String fieldName = rel.getFieldName();

        if (isCollectionType(rel.getRelationshipType())) {
            return "    private List<" + targetName + "DetailDto> " + fieldName + ";";
        }
        return "    private " + targetName + "DetailDto " + fieldName + ";";
    }

    @Override
    public String generateImports(RelationshipDefinition rel) {
        if (isCollectionType(rel.getRelationshipType())) {
            return "java.util.List";
        }
        return "";
    }

    private boolean isCollectionType(RelationshipType type) {
        return type == RelationshipType.ONE_TO_MANY || type == RelationshipType.MANY_TO_MANY;
    }
}
