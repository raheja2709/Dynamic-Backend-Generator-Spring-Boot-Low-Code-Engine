package com.user.driven.operations.generator.frontend;

import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a React frontend application alongside the backend.
 * Produces CRUD pages, API client, admin dashboard, and package.json.
 *
 * @author Jatin Raheja
 */
@Slf4j
@Component
public class FrontendGenerator {

    private final TemplateEngine engine;
    private final FileWriterService writer;

    public FrontendGenerator(TemplateEngine engine, FileWriterService writer) {
        this.engine = engine;
        this.writer = writer;
    }

    /**
     * Generates the complete React frontend for a project.
     */
    public void generate(ProjectDefinition project, Path basePath) {
        log.info("Starting frontend generation for project={}", project.getName());

        Path frontendPath = basePath.resolve("frontend");

        // Package.json
        generateFile("frontend/package.json.ftl",
                Map.of("project", project),
                frontendPath.resolve("package.json"));

        // API client
        generateFile("frontend/api-client.js.ftl",
                buildApiClientModel(project),
                frontendPath.resolve("src/api/apiClient.js"));

        // App.jsx (router)
        generateFile("frontend/App.jsx.ftl",
                Map.of("project", project, "entities", project.getEntities()),
                frontendPath.resolve("src/App.jsx"));

        // index.jsx
        generateFile("frontend/index.jsx.ftl",
                Map.of("project", project),
                frontendPath.resolve("src/index.jsx"));

        // Dashboard
        generateFile("frontend/Dashboard.jsx.ftl",
                Map.of("project", project, "entities", project.getEntities()),
                frontendPath.resolve("src/pages/Dashboard.jsx"));

        // Sidebar layout
        generateFile("frontend/Layout.jsx.ftl",
                Map.of("project", project, "entities", project.getEntities()),
                frontendPath.resolve("src/components/Layout.jsx"));

        // Entity CRUD pages
        for (EntityDefinition entity : project.getEntities()) {
            Map<String, Object> entityModel = buildEntityModel(project, entity);

            generateFile("frontend/EntityList.jsx.ftl", entityModel,
                    frontendPath.resolve("src/pages/" + entity.getName() + "List.jsx"));

            generateFile("frontend/EntityForm.jsx.ftl", entityModel,
                    frontendPath.resolve("src/pages/" + entity.getName() + "Form.jsx"));

            generateFile("frontend/EntityDetail.jsx.ftl", entityModel,
                    frontendPath.resolve("src/pages/" + entity.getName() + "Detail.jsx"));
        }

        log.info("Frontend generation completed for project={}", project.getName());
    }

    private Map<String, Object> buildApiClientModel(ProjectDefinition project) {
        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entities", project.getEntities());
        return model;
    }

    private Map<String, Object> buildEntityModel(ProjectDefinition project, EntityDefinition entity) {
        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);
        model.put("fields", getFormFields(entity));
        model.put("relationships", entity.getRelationships() != null ? entity.getRelationships() : Collections.emptyList());
        return model;
    }

    /**
     * Returns fields suitable for forms (excludes PK and relationship fields).
     */
    private List<FieldDefinition> getFormFields(EntityDefinition entity) {
        if (entity.getFields() == null) return Collections.emptyList();
        return entity.getFields().stream()
                .filter(f -> !"PRIMARY_KEY".equals(f.getFieldType() != null ? f.getFieldType().name() : ""))
                .filter(f -> f.getRelationshipType() == null)
                .collect(Collectors.toList());
    }

    private void generateFile(String template, Map<String, Object> model, Path outputPath) {
        String content = engine.process(template, model);
        writer.write(outputPath, content);
    }
}
