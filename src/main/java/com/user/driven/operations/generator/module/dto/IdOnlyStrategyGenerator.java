package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.RelationshipType;
import org.springframework.stereotype.Component;

/**
 * ID_ONLY strategy: generates a Long field for single references (ManyToOne, OneToOne)
 * and a List&lt;Long&gt; for collection references (OneToMany, ManyToMany).
 *
 * @author Jatin Raheja
 */
@Component
public class IdOnlyStrategyGenerator implements DtoStrategyGenerator {

    @Override
    public String generateField(RelationshipDefinition rel) {
        if (isCollectionType(rel.getRelationshipType())) {
            return "    private List<Long> " + rel.getFieldName() + "Ids;";
        }
        return "    private Long " + rel.getFieldName() + "Id;";
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
