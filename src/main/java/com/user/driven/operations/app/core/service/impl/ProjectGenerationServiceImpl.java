package com.user.driven.operations.app.core.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.user.driven.operations.app.common.exception.GenerationFailedException;
import com.user.driven.operations.app.common.util.ZipUtil;
import com.user.driven.operations.app.config.AppProperties;
import com.user.driven.operations.generator.core.BuildVerifier;
import com.user.driven.operations.generator.orchestrator.ProjectGenerator;
import com.user.driven.operations.generator.project.ProjectValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.service.ProjectGenerationService;

/**
 * Implementation of {@link ProjectGenerationService} for generating and downloading
 * Spring Boot project source code based on the provided project definition.
 * 
 * This service handles generating the directory structure, writing code files, and
 * compressing the project for download.
 * 
 * @author Jatin Raheja
 */
@Service
public class ProjectGenerationServiceImpl implements ProjectGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ProjectGenerationServiceImpl.class);

    private final ProjectGenerator generator;
    private final ProjectValidator validator;
    private final BuildVerifier buildVerifier;
    private final AppProperties appProperties;

    public ProjectGenerationServiceImpl(ProjectGenerator generator, ProjectValidator validator,
                                        BuildVerifier buildVerifier, AppProperties appProperties) {
        this.generator = generator;
        this.validator = validator;
        this.buildVerifier = buildVerifier;
        this.appProperties = appProperties;
    }

    @Override
    public Path generate(ProjectDefinition project) {

        try {
            validator.validate(project);

            Path output = Paths.get(appProperties.getGeneratedProjectsDirectory(), project.getName());

            generator.generate(project, output);

            buildVerifier.verify(output.toString());

            return ZipUtil.zipFolder(output, project.getName());
        } catch (com.user.driven.operations.app.common.exception.BaseApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Generation failed for project '{}': {}", project.getName(), e.getMessage(), e);
            throw new GenerationFailedException(
                    "Project generation failed: " + e.getMessage(),
                    "GENERATION",
                    e
            );
        }
    }
}