package com.user.driven.operations.generator.orchestrator;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.module.EntityModuleGenerator;
import com.user.driven.operations.generator.project.ApplicationGenerator;
import com.user.driven.operations.generator.project.PomGenerator;
import com.user.driven.operations.generator.project.ProjectStructureGenerator;
import com.user.driven.operations.generator.security.SecurityGeneratorFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ProjectGenerator {

    private final ProjectStructureGenerator structure;
    private final PomGenerator pom;
    private final ApplicationGenerator app;
    private final EntityModuleGenerator entity;
    private final SecurityGeneratorFactory securityFactory;

    public ProjectGenerator(ProjectStructureGenerator structure, PomGenerator pom, ApplicationGenerator app, EntityModuleGenerator entity, SecurityGeneratorFactory securityFactory) {
        this.structure = structure;
        this.pom = pom;
        this.app = app;
        this.entity = entity;
        this.securityFactory = securityFactory;
    }

    public void generate(ProjectDefinition project, Path path) {

        structure.generate(project, path);
        pom.generate(project, path);
        app.generate(project, path);

        project.getEntities().forEach(e ->
                entity.generate(project, e, path)
        );

        if (project.isSecurityEnabled()) {
            var generator = securityFactory.get(project.getSecurityType());
            if (generator != null) {
                generator.generate(project, path);
            } else {
                throw new RuntimeException(
                        "No security generator available for type: " + project.getSecurityType() +
                        ". Supported types: JWT. Set securityEnabled=false or use a supported type.");
            }
        }
    }
}
