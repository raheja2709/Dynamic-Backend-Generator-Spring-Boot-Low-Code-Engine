package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.RelationshipType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SUMMARY strategy: generates a SummaryDto reference with id and the first
 * String field of the target entity as a label. For collections, generates
 * a List of SummaryDto references.
 *
 * @author Jatin Raheja
 */
@Component
public class SummaryStrategyGenerator implements DtoStrategyGenerator {

    @Override
    public String generateField(RelationshipDefinition rel) {
        String targetName = rel.getTargetEntity();
        String fieldName = rel.getFieldName();

        if (isCollectionType(rel.getRelationshipType())) {
            return "    private List<" + targetName + "SummaryDto> " + fieldName + ";";
        }
        return "    private " + targetName + "SummaryDto " + fieldName + ";";
    }

    @Override
    public String generateImports(RelationshipDefinition rel) {
        if (isCollectionType(rel.getRelationshipType())) {
            return "java.util.List";
        }
        return "";
    }

    /**
     * Resolves the label field name from the target entity.
     * Returns the name of the first String field, or "name" as fallback.
     *
     * @param targetEntity the target entity definition (may be null if not resolved)
     * @return the label field name
     */
    public static String resolveLabelField(EntityDefinition targetEntity) {
        if (targetEntity == null) {
            return "name";
        }

        List<FieldDefinition> fields = targetEntity.getFields();
        if (fields != null) {
            for (FieldDefinition field : fields) {
                if (field.getDataType() == DataType.STRING
                        && field.getRelationshipType() == null) {
                    return field.getName();
                }
            }
        }
        return "name";
    }

    private boolean isCollectionType(RelationshipType type) {
        return type == RelationshipType.ONE_TO_MANY || type == RelationshipType.MANY_TO_MANY;
    }
}
