package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
public class PomGenerator extends BaseGenerator {

    public PomGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    public void generate(ProjectDefinition project, Path path) {
        generate("project/pom.xml.ftl", Map.of("project", project),
                path.resolve("pom.xml"));
    }
}
