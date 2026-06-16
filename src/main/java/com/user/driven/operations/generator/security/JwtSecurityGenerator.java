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

        // Security classes
        generate("JwtUtils.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/security/JwtUtils.java"));

        generate("JwtAuthenticationFilter.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/security/JwtAuthenticationFilter.java"));

        generate("JwtAuthenticationEntryPoint.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/security/JwtAuthenticationEntryPoint.java"));

        generate("UserPrincipal.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/security/UserPrincipal.java"));

        // Config
        generate("common/SecurityConfig.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/config/SecurityConfig.java"));

        // Controller
        generate("AuthController.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/controller/AuthController.java"));

        // Model classes
        generate("User.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/model/User.java"));

        generate("Role.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/model/Role.java"));

        generate("ERole.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/model/ERole.java"));

        // Repositories
        generate("UserRepository.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/repository/UserRepository.java"));

        generate("RoleRepository.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/repository/RoleRepository.java"));

        // Service
        generate("UserDetailsServiceImpl.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/service/UserDetailsServiceImpl.java"));

        // DTOs
        generate("LoginRequest.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/dto/LoginRequest.java"));

        generate("SignupRequest.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/dto/SignupRequest.java"));

        generate("MessageResponse.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/payload/response/MessageResponse.java"));

        generate("UserInfoResponse.java.ftl",
                Map.of("project", project),
                path.resolve("src/main/java/" + pkg + "/payload/response/UserInfoResponse.java"));
    }
}
