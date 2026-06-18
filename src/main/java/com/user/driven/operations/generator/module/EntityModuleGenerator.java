package com.user.driven.operations.generator.module;

import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.DtoStrategy;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import com.user.driven.operations.generator.module.dto.SummaryStrategyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

@Slf4j
@Component
public class EntityModuleGenerator extends BaseGenerator {

    private final RelationshipCodeGenerator relationshipCodeGenerator;

    public EntityModuleGenerator(TemplateEngine engine, FileWriterService writer,
                                 RelationshipCodeGenerator relationshipCodeGenerator) {
        super(engine, writer);
        this.relationshipCodeGenerator = relationshipCodeGenerator;
    }

    public void generate(ProjectDefinition project, EntityDefinition entity, Path basePath) {
        log.info("Starting entity generation for entity={}", entity.getName());

        String pkg = project.getPackageName().replace(".", "/");
        log.info("Resolved package path={}", pkg);

        Map<String, Object> model = buildTemplateModel(project, entity);

        Path base = basePath.resolve("src/main/java/" + pkg);
        log.info("Resolved base source path={}", base);

        // Core entity files
        generate("entity/Entity.java.ftl", model,
                base.resolve("model/" + entity.getName() + ".java"));

        generate("entity/Repository.java.ftl", model,
                base.resolve("repository/" + entity.getName() + "Repository.java"));

        generate("entity/Service.java.ftl", model,
                base.resolve("service/" + entity.getName() + "Service.java"));

        generate("entity/ServiceImpl.java.ftl", model,
                base.resolve("service/impl/" + entity.getName() + "ServiceImpl.java"));

        generate("entity/Controller.java.ftl", model,
                base.resolve("controller/" + entity.getName() + "Controller.java"));

        // Legacy DTO (backward compatibility)
        generate("entity/Dto.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "Dto.java"));

        // New DTO types
        generate("entity/ListDto.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "ListDto.java"));

        generate("entity/DetailDto.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "DetailDto.java"));

        generate("entity/CreateRequest.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "CreateRequest.java"));

        generate("entity/UpdateRequest.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "UpdateRequest.java"));

        // Summary DTO (always generated - may be referenced by other entities)
        generate("entity/SummaryDto.java.ftl", model,
                base.resolve("dto/" + entity.getName() + "SummaryDto.java"));

        log.info("Completed generation for entity={}", entity.getName());
    }

    /**
     * Builds the template model including relationship code generation data.
     */
    private Map<String, Object> buildTemplateModel(ProjectDefinition project, EntityDefinition entity) {
        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);

        // Generate relationship fields and imports from RelationshipDefinition model
        List<RelationshipDefinition> relationships = entity.getRelationships();
        if (relationships != null && !relationships.isEmpty()) {
            log.info("Generating {} relationship fields for entity={}",
                    relationships.size(), entity.getName());

            List<String> relationshipFields = new ArrayList<>();
            for (RelationshipDefinition rel : relationships) {
                String fieldCode = relationshipCodeGenerator.generateRelationshipField(rel, entity.getName());
                relationshipFields.add(fieldCode);
            }
            model.put("relationshipFields", relationshipFields);

            Set<String> imports = relationshipCodeGenerator.generateImports(relationships);
            model.put("relationshipImports", imports);
        } else {
            model.put("relationshipFields", Collections.emptyList());
            model.put("relationshipImports", Collections.emptySet());
        }

        // Resolve label field for SummaryDto
        String labelField = resolveLabelField(entity);
        model.put("labelField", labelField);

        return model;
    }

    /**
     * Resolves the label field for the SummaryDto - picks the first String field
     * that is not a relationship and not the primary key.
     */
    private String resolveLabelField(EntityDefinition entity) {
        if (entity.getFields() != null) {
            for (FieldDefinition field : entity.getFields()) {
                if (field.getDataType() == DataType.STRING
                        && field.getRelationshipType() == null
                        && !"PRIMARY_KEY".equals(field.getFieldType() != null ? field.getFieldType().name() : "")) {
                    return field.getName();
                }
            }
        }
        return "name";
    }
}
