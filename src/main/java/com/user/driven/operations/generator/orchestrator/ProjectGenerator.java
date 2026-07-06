package com.user.driven.operations.generator.orchestrator;

import com.user.driven.operations.app.common.exception.UnsupportedSecurityTypeException;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.generator.frontend.FrontendGenerator;
import com.user.driven.operations.generator.module.EntityModuleGenerator;
import com.user.driven.operations.generator.project.ApplicationGenerator;
import com.user.driven.operations.generator.project.BoilerplateGenerator;
import com.user.driven.operations.generator.project.PomGenerator;
import com.user.driven.operations.generator.project.ProjectStructureGenerator;
import com.user.driven.operations.generator.security.SecurityGeneratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Slf4j
@Component
public class ProjectGenerator {

    private final ProjectStructureGenerator structure;
    private final PomGenerator pom;
    private final ApplicationGenerator app;
    private final EntityModuleGenerator entity;
    private final SecurityGeneratorFactory securityFactory;
    private final FrontendGenerator frontendGenerator;
    private final BoilerplateGenerator boilerplateGenerator;

    public ProjectGenerator(ProjectStructureGenerator structure, PomGenerator pom, ApplicationGenerator app,
                            EntityModuleGenerator entity, SecurityGeneratorFactory securityFactory,
                            FrontendGenerator frontendGenerator, BoilerplateGenerator boilerplateGenerator) {
        this.structure = structure;
        this.pom = pom;
        this.app = app;
        this.entity = entity;
        this.securityFactory = securityFactory;
        this.frontendGenerator = frontendGenerator;
        this.boilerplateGenerator = boilerplateGenerator;
    }

    public void generate(ProjectDefinition project, Path path) {

        structure.generate(project, path);
        pom.generate(project, path);
        app.generate(project, path);
        boilerplateGenerator.generate(project, path);

        project.getEntities().forEach(e -> {
            // Log warning for unsupported operation types and skip them
            if (e.getOperations() != null) {
                e.getOperations().forEach(op -> {
                    String opType = op.getOperationType() != null ? op.getOperationType().name() : "null";
                    // Known supported types are handled in templates; log any future unknowns
                });
            }
            entity.generate(project, e, path);
        });

        if (project.isSecurityEnabled()) {
            var generator = securityFactory.get(project.getSecurityType());
            if (generator != null) {
                generator.generate(project, path);
            } else {
                throw new UnsupportedSecurityTypeException(
                        project.getSecurityType() != null ? project.getSecurityType().name() : "null");
            }
        }

        // Generate frontend if enabled and at least one entity exists
        if (project.isFrontendEnabled() && project.getEntities() != null && !project.getEntities().isEmpty()) {
            log.info("Generating frontend for project={}", project.getName());
            frontendGenerator.generate(project, path);
        }
    }
}
