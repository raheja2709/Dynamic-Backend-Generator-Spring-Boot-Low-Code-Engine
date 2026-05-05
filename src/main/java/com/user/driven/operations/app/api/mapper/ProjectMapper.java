package com.user.driven.operations.app.api.mapper;

import com.user.driven.operations.app.api.dto.*;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.FieldType;

import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static ProjectDefinition map(GenerateProjectRequest req) {

        ProjectDefinition project = new ProjectDefinition();
        project.setName(req.getName());
        project.setPackageName(req.getPackageName());
        project.setSecurityEnabled(req.isSecurityEnabled());

        project.setEntities(
                req.getEntities().stream().map(e -> {
                    EntityDefinition entity = new EntityDefinition();
                    entity.setName(e.getName());

                    entity.setFields(
                            e.getFields().stream().map(f -> {
                                FieldDefinition field = new FieldDefinition();
                                field.setName(f.getName());
                                field.setDataType(DataType.valueOf(f.getType().toUpperCase()));
                                field.setFieldType(
                                        Optional.ofNullable(f.getFieldType())
                                                .map(FieldType::valueOf)
                                                .orElse(FieldType.NORMAL)
                                );
                                field.setNullable(Boolean.TRUE.equals(f.getNullable()));
                                field.setReferenceEntity(f.getReferenceEntity());
                                field.setReferenceField(f.getReferenceField());
                                return field;
                            }).toList()
                    );
                    return entity;
                }).collect(Collectors.toList())
        );

        return project;
    }
}