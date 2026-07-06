package com.user.driven.operations.app.api.mapper;

import com.user.driven.operations.app.api.dto.*;
import com.user.driven.operations.app.common.util.MessageConstants;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.FieldType;
import com.user.driven.operations.enums.SecurityType;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectMapper {

    private static final Map<String, DataType> TYPE_ALIASES = Map.ofEntries(
            Map.entry("STRING", DataType.STRING),
            Map.entry("LONG", DataType.LONG),
            Map.entry("INTEGER", DataType.INTEGER),
            Map.entry("INT", DataType.INTEGER),
            Map.entry("DOUBLE", DataType.DOUBLE),
            Map.entry("FLOAT", DataType.FLOAT),
            Map.entry("BOOLEAN", DataType.BOOLEAN),
            Map.entry("BIGDECIMAL", DataType.DECIMAL),
            Map.entry("DECIMAL", DataType.DECIMAL),
            Map.entry("LOCALDATETIME", DataType.DATETIME),
            Map.entry("DATETIME", DataType.DATETIME),
            Map.entry("LOCALDATE", DataType.DATE),
            Map.entry("DATE", DataType.DATE),
            Map.entry("TEXT", DataType.TEXT),
            Map.entry("UUID", DataType.UUID),
            Map.entry("JSON", DataType.JSON),
            Map.entry("ENUM", DataType.ENUM)
    );

    public static ProjectDefinition map(GenerateProjectRequest req) {

        ProjectDefinition project = new ProjectDefinition();
        project.setName(req.getName());
        project.setPackageName(req.getPackageName());
        project.setSecurityEnabled(req.isSecurityEnabled());

        // Map database type
        if (req.getDatabaseType() != null && !req.getDatabaseType().isBlank()) {
            try {
                project.setDatabaseType(com.user.driven.operations.enums.DatabaseType.valueOf(req.getDatabaseType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Default to H2 if invalid
                project.setDatabaseType(com.user.driven.operations.enums.DatabaseType.H2);
            }
        }

        // Map Java version
        if (req.getJavaVersion() != null && !req.getJavaVersion().isBlank()) {
            project.setJavaVersion(req.getJavaVersion());
        }

        // Map Spring Boot version
        if (req.getSpringBootVersion() != null && !req.getSpringBootVersion().isBlank()) {
            project.setSpringBootVersion(req.getSpringBootVersion());
        }

        // Map security type string to enum
        if (req.getSecurityType() != null && !req.getSecurityType().isBlank()) {
            try {
                project.setSecurityType(SecurityType.valueOf(req.getSecurityType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        String.format(MessageConstants.UNSUPPORTED_SECURITY_TYPE, req.getSecurityType()));
            }
        }

        project.setEntities(
                req.getEntities().stream().map(e -> {
                    EntityDefinition entity = new EntityDefinition();
                    entity.setName(e.getName());

                    entity.setFields(
                            e.getFields().stream().map(f -> {
                                FieldDefinition field = new FieldDefinition();
                                field.setName(f.getName());
                                field.setDataType(resolveDataType(f.getType()));
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

                    // Map operations
                    if (e.getOperations() != null && !e.getOperations().isEmpty()) {
                        entity.setOperations(
                                e.getOperations().stream()
                                        .filter(op -> op.isEnabled())
                                        .map(op -> {
                                            var config = new com.user.driven.operations.app.core.model.OperationConfig();
                                            config.setOperationType(
                                                    com.user.driven.operations.enums.OperationType.valueOf(op.getOperationType()));
                                            config.setEnabled(true);
                                            return config;
                                        }).collect(Collectors.toList())
                        );
                    } else {
                        // Default: CRUD operations
                        entity.setOperations(java.util.List.of(
                                createOp(com.user.driven.operations.enums.OperationType.CREATE),
                                createOp(com.user.driven.operations.enums.OperationType.READ),
                                createOp(com.user.driven.operations.enums.OperationType.UPDATE),
                                createOp(com.user.driven.operations.enums.OperationType.DELETE)
                        ));
                    }

                    return entity;
                }).collect(Collectors.toList())
        );

        return project;
    }

    private static com.user.driven.operations.app.core.model.OperationConfig createOp(
            com.user.driven.operations.enums.OperationType type) {
        var config = new com.user.driven.operations.app.core.model.OperationConfig();
        config.setOperationType(type);
        config.setEnabled(true);
        return config;
    }

    private static DataType resolveDataType(String type) {
        if (type == null || type.isBlank()) {
            return DataType.STRING;
        }
        String normalized = type.toUpperCase().trim();
        DataType resolved = TYPE_ALIASES.get(normalized);
        if (resolved != null) {
            return resolved;
        }
        // Fallback to direct enum lookup
        try {
            return DataType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format(MessageConstants.UNSUPPORTED_FIELD_TYPE, type,
                            String.join(", ", TYPE_ALIASES.keySet().stream().sorted().toList())));
        }
    }
}