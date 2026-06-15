package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.SecurityType;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import com.user.driven.operations.generator.utils.NamingUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationGenerator extends BaseGenerator {

    public ApplicationGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    public void generate(ProjectDefinition project, Path path) {

        String pkg = project.getPackageName().replace(".", "/");

        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("hasEntities", project.getEntities() != null && !project.getEntities().isEmpty());
        model.put("isJwt", SecurityType.JWT.equals(project.getSecurityType())); // safe
        model.put("isSecurityEnabled", project.isSecurityEnabled());
        String applicationClassName = NamingUtils.toApplicationClassName(project.getName());
        model.put("applicationClassName", applicationClassName);

        generate(
                "project/Application.java.ftl",
                model,
                path.resolve("src/main/java/" + pkg + "/" +applicationClassName + ".java")
        );
    }
}
