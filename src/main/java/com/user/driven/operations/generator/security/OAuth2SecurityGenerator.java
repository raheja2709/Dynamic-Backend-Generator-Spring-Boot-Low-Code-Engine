package com.user.driven.operations.generator.security;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * Generates OAuth2 security configuration for output projects.
 * Produces SecurityConfig with OAuth2 login/resource server and
 * application.properties fragment with provider placeholders.
 *
 * @author Jatin Raheja
 */
@Component
public class OAuth2SecurityGenerator extends BaseGenerator implements SecurityGenerator {

    public OAuth2SecurityGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    @Override
    public void generate(ProjectDefinition project, Path path) {
        String pkg = project.getPackageName().replace(".", "/");
        Map<String, Object> model = Map.of("project", project);

        generate("security/oauth2/SecurityConfig.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/config/SecurityConfig.java"));

        generate("security/oauth2/OAuth2Properties.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/config/OAuth2Properties.java"));
    }
}
