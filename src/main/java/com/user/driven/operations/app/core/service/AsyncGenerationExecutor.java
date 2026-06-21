package com.user.driven.operations.app.core.service;

import com.user.driven.operations.app.common.util.ZipUtil;
import com.user.driven.operations.app.config.AppProperties;
import com.user.driven.operations.app.core.model.GenerationJob;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.repository.GenerationJobRepository;
import com.user.driven.operations.enums.GenerationStage;
import com.user.driven.operations.enums.JobStatus;
import com.user.driven.operations.generator.core.BuildVerifier;
import com.user.driven.operations.generator.orchestrator.ProjectGenerator;
import com.user.driven.operations.generator.project.ProjectValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Executes project generation asynchronously in a configurable thread pool.
 * Updates job progress at each stage of the generation pipeline.
 *
 * @author Jatin Raheja
 */
@Slf4j
@Service
public class AsyncGenerationExecutor {

    private final GenerationJobRepository jobRepository;
    private final ProjectGenerator projectGenerator;
    private final ProjectValidator projectValidator;
    private final BuildVerifier buildVerifier;
    private final AppProperties appProperties;

    public AsyncGenerationExecutor(GenerationJobRepository jobRepository,
                                   ProjectGenerator projectGenerator,
                                   ProjectValidator projectValidator,
                                   BuildVerifier buildVerifier,
                                   AppProperties appProperties) {
        this.jobRepository = jobRepository;
        this.projectGenerator = projectGenerator;
        this.projectValidator = projectValidator;
        this.buildVerifier = buildVerifier;
        this.appProperties = appProperties;
    }

    /**
     * Executes the generation pipeline asynchronously.
     * Stages: VALIDATION(10%) → STRUCTURE(30%) → ENTITIES(50%) → SECURITY(70%) → BUILD(90%) → ZIP(100%)
     */
    @Async("generationTaskExecutor")
    public void executeGeneration(String jobId, ProjectDefinition project) {
        log.info("Starting async generation for job={}, project={}", jobId, project.getName());

        try {
            // Stage: VALIDATION
            updateProgress(jobId, GenerationStage.VALIDATION);
            projectValidator.validate(project);

            // Stage: STRUCTURE + ENTITIES + SECURITY (all done by ProjectGenerator)
            updateProgress(jobId, GenerationStage.STRUCTURE);
            Path output = Paths.get(appProperties.getGeneratedProjectsDirectory(), project.getName());
            
            updateProgress(jobId, GenerationStage.ENTITIES);
            projectGenerator.generate(project, output);

            updateProgress(jobId, GenerationStage.SECURITY);
            // Security is already generated within projectGenerator.generate()

            // Stage: BUILD
            updateProgress(jobId, GenerationStage.BUILD);
            buildVerifier.verify(output.toString());

            // Stage: ZIP
            updateProgress(jobId, GenerationStage.ZIP);
            Path zipPath = ZipUtil.zipFolder(output, project.getName());

            // Mark completed
            markCompleted(jobId, zipPath.toString());
            log.info("Generation completed for job={}", jobId);

        } catch (Exception e) {
            log.error("Generation failed for job={}: {}", jobId, e.getMessage(), e);
            markFailed(jobId, e.getMessage());

            // Cleanup partial output
            try {
                Path output = Paths.get(appProperties.getGeneratedProjectsDirectory(), project.getName());
                if (Files.exists(output)) {
                    deleteDirectory(output);
                }
            } catch (IOException cleanupEx) {
                log.warn("Failed to cleanup partial output for job={}: {}", jobId, cleanupEx.getMessage());
            }
        }
    }

    @Transactional
    protected void updateProgress(String jobId, GenerationStage stage) {
        GenerationJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.PROCESSING);
            job.setCurrentStage(stage);
            job.setProgress(stage.getProgress());
            jobRepository.save(job);
        }
    }

    @Transactional
    protected void markCompleted(String jobId, String outputPath) {
        GenerationJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.COMPLETED);
            job.setProgress(100);
            job.setOutputPath(outputPath);
            job.setCompletedAt(LocalDateTime.now());
            job.setExpiresAt(LocalDateTime.now().plusHours(24));
            jobRepository.save(job);
        }
    }

    @Transactional
    protected void markFailed(String jobId, String errorMessage) {
        GenerationJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(errorMessage != null && errorMessage.length() > 2000
                    ? errorMessage.substring(0, 2000) : errorMessage);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walk(path)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }
}
