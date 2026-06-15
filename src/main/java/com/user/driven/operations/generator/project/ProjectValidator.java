package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.FieldType;
import org.springframework.stereotype.Component;

@Component
public class ProjectValidator {

    public void validate(ProjectDefinition project) {

        if (project.getEntities().isEmpty()) {
            throw new RuntimeException("At least one entity required");
        }

        project.getEntities().forEach(entity -> {

            if (entity.getFields().isEmpty()) {
                throw new RuntimeException("Entity must have fields: " + entity.getName());
            }

            boolean hasPK = entity.getFields().stream()
                    .anyMatch(f -> f.getFieldType() == FieldType.PRIMARY_KEY);

            if (!hasPK) {
                throw new RuntimeException("Primary key missing in entity: " + entity.getName());
            }
        });
    }
}