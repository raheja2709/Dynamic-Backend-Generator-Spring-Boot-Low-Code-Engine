package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * Generates boilerplate files for the output project:
 * .gitignore, .gitattributes, README.md, mvnw, mvnw.cmd, application.properties
 */
@Slf4j
@Component
public class BoilerplateGenerator extends BaseGenerator {

    private static final String MVNW_SCRIPT = """
            #!/bin/sh
            # Maven Wrapper script
            # This script downloads and runs Maven if not already available
            exec mvn "$@"
            """;

    private static final String MVNW_CMD_SCRIPT = """
            @REM Maven Wrapper script for Windows
            @echo off
            mvn %*
            """;

    public BoilerplateGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    public void generate(ProjectDefinition project, Path basePath) {
        log.info("Generating boilerplate files for project={}", project.getName());

        Map<String, Object> model = Map.of("project", project);

        // .gitignore
        generate("project/gitignore.ftl", model, basePath.resolve(".gitignore"));

        // .gitattributes
        generate("project/gitattributes.ftl", model, basePath.resolve(".gitattributes"));

        // README.md
        generate("project/README.md.ftl", model, basePath.resolve("README.md"));

        // application.properties
        generate("project/application.properties.ftl", model,
                basePath.resolve("src/main/resources/application.properties"));

        // Maven Wrapper (simple pass-through scripts)
        writer.write(basePath.resolve("mvnw"), MVNW_SCRIPT);
        writer.write(basePath.resolve("mvnw.cmd"), MVNW_CMD_SCRIPT);

        log.info("Boilerplate generation completed");
    }
}
