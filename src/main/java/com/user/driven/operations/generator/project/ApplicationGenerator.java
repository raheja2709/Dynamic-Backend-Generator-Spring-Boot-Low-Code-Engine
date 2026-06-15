package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
public class ApplicationGenerator extends BaseGenerator {

    public ApplicationGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    public void generate(ProjectDefinition project, Path path) {

        String pkg = project.getPackageName().replace(".", "/");
        String className = project.getName() + "Application";

        generate("project/Application.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/" + className + ".java"));
    }
}
