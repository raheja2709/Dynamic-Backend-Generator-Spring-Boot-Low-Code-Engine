package com.user.driven.operations.generator.security;

import com.user.driven.operations.app.core.model.ProjectDefinition;

import java.nio.file.Path;

public interface SecurityGenerator {
    void generate(ProjectDefinition project, Path path);
}
