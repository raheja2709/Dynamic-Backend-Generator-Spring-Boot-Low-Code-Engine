package com.user.driven.operations.generator.security;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.core.BaseGenerator;
import com.user.driven.operations.generator.core.FileWriterService;
import com.user.driven.operations.generator.core.TemplateEngine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

/**
 * Generates HTTP Basic Auth security configuration for output projects.
 * Produces a SecurityConfig with HTTP Basic authentication and BCryptPasswordEncoder.
 *
 * @author Jatin Raheja
 */
@Component
public class BasicAuthSecurityGenerator extends BaseGenerator implements SecurityGenerator {

    public BasicAuthSecurityGenerator(TemplateEngine engine, FileWriterService writer) {
        super(engine, writer);
    }

    @Override
    public void generate(ProjectDefinition project, Path path) {
        String pkg = project.getPackageName().replace(".", "/");
        Map<String, Object> model = Map.of("project", project);

        generate("security/basic/SecurityConfig.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/config/SecurityConfig.java"));

        generate("security/basic/User.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/model/User.java"));

        generate("security/basic/UserRepository.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/repository/UserRepository.java"));

        generate("security/basic/UserDetailsServiceImpl.java.ftl", model,
                path.resolve("src/main/java/" + pkg + "/service/UserDetailsServiceImpl.java"));
    }
}
