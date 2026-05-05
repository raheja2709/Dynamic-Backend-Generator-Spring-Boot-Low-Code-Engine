package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ProjectStructureGenerator {

    public void generate(ProjectDefinition project, Path basePath) {

        String pkg = project.getPackageName().replace(".", "/");

        try {
            Files.createDirectories(basePath.resolve("src/main/java/" + pkg));
            Files.createDirectories(basePath.resolve("src/main/resources"));
            Files.createDirectories(basePath.resolve("src/test/java/" + pkg));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create structure", e);
        }
    }
}
