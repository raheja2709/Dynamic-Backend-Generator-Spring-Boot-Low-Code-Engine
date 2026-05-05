package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import org.springframework.stereotype.Component;

@Component
public class ProjectValidator {

    public void validate(ProjectDefinition project) {
        if (project.getName() == null) throw new RuntimeException("Project name required");
        if (project.getPackageName() == null) throw new RuntimeException("Package required");
    }
}
