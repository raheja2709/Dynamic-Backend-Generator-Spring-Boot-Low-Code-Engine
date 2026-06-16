package com.user.driven.operations.generator.utils;

import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.enums.RelationshipType;

public final class RelationshipGeneratorUtil {

    private RelationshipGeneratorUtil() {
    }

    public static String generateRelationship(FieldDefinition field) {

        RelationshipType type = field.getRelationshipType();

        if (type == null) {
            return "";
        }

        return switch (type) {

            case MANY_TO_ONE -> "@ManyToOne\nprivate "
                    + field.getRelationshipTarget()
                    + " "
                    + field.getName()
                    + ";";

            case ONE_TO_ONE -> "@OneToOne\nprivate "
                    + field.getRelationshipTarget()
                    + " "
                    + field.getName()
                    + ";";

            default -> "";
        };
    }
}