package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.SecurityType;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class PomGenerator extends BaseGenerator {

    public PomGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    public void generate(ProjectDefinition project, Path path) {
        Map<String, Object> model = new HashMap<>();

        model.put("project", project);

        // REQUIRED FIX
        model.put("hasEntities", project.getEntities() != null && !project.getEntities().isEmpty());
        model.put("isSecurityEnabled", project.isSecurityEnabled());
        model.put("isJwt", SecurityType.JWT.equals(project.getSecurityType()));

        generate("project/pom.xml.ftl", model, path.resolve("pom.xml"));
    }
}
