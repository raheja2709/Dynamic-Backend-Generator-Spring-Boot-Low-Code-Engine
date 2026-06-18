package com.user.driven.operations.generator.security;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * Generates session-based security configuration for output projects.
 * Produces SecurityConfig with form login, max 1 concurrent session,
 * and a login page HTML template.
 *
 * @author Jatin Raheja
 */
@Component
public class SessionSecurityGenerator extends BaseGenerator implements SecurityGenerator {

    public SessionSecurityGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    @Override
    public void generate(ProjectDefinition project, Path path) {
        String pkg = project.getPackageName().replace(".", "/");
        Map<String, Object> model = Map.of("project", project);

        generate("security/session/SecurityConfig.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/config/SecurityConfig.java"));

        generate("security/session/User.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/model/User.java"));

        generate("security/session/UserRepository.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/repository/UserRepository.java"));

        generate("security/session/UserDetailsServiceImpl.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/service/UserDetailsServiceImpl.java"));

        generate("security/session/login.html.ftl", model,
                path.resolve("src/main/resources/templates/login.html"));
    }
}
