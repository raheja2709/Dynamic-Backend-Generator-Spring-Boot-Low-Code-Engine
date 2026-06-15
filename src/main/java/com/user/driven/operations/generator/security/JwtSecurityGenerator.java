package com.user.driven.operations.generator.security;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
public class JwtSecurityGenerator extends BaseGenerator implements SecurityGenerator {

    public JwtSecurityGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    @Override
    public void generate(ProjectDefinition project, Path path) {

        String pkg = project.getPackageName().replace(".", "/");

        generate("security/jwt/JwtUtils.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/security/JwtUtils.java"));
    }
}
