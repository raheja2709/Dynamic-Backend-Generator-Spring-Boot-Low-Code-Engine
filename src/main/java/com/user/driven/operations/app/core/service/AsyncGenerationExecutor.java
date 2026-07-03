package com.user.driven.operations.app.core.service;

import com.user.driven.operations.app.common.util.ZipUtil;
import com.user.driven.operations.app.config.AppProperties;
import com.user.driven.operations.app.core.model.GenerationJob;
import com.user.driven.operations.app.core.model.GenerationStageLog;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.repository.GenerationJobRepository;
import com.user.driven.operations.app.core.repository.GenerationStageLogRepository;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Executes project generation asynchronously in a configurable thread pool.
 * Updates job progress at each stage and logs every stage transition to the database.
 *
 * @author Jatin Raheja
 */
@Slf4j
@Service
public class AsyncGenerationExecutor {

    private final GenerationJobRepository jobRepository;
    private final GenerationStageLogRepository stageLogRepository;
    private final ProjectGenerator projectGenerator;
    private final ProjectValidator projectValidator;
    private final BuildVerifier buildVerifier;
    private final AppProperties appProperties;

    public AsyncGenerationExecutor(GenerationJobRepository jobRepository,
                                   GenerationStageLogRepository stageLogRepository,
                                   ProjectGenerator projectGenerator,
                                   ProjectValidator projectValidator,
                                   BuildVerifier buildVerifier,
                                   AppProperties appProperties) {
        this.jobRepository = jobRepository;
        this.stageLogRepository = stageLogRepository;
        this.projectGenerator = projectGenerator;
        this.projectValidator = projectValidator;
        this.buildVerifier = buildVerifier;
        this.appProperties = appProperties;
    }

    /**
     * Executes the generation pipeline asynchronously.
     * Stages: VALIDATION(10%) → STRUCTURE(30%) → ENTITIES(50%) → SECURITY(70%) → BUILD(90%) → ZIP(100%)
     * Each stage is logged to generation_stage_logs for full traceability.
     */
    @Async("generationTaskExecutor")
    public void executeGeneration(String jobId, ProjectDefinition project) {
        log.info("Starting async generation for job={}, project={}", jobId, project.getName());

        try {
            // Stage: VALIDATION
            executeStage(jobId, GenerationStage.VALIDATION, "Validating project definition", () -> {
                projectValidator.validate(project);
            });

            // Stage: STRUCTURE
            Path output = Paths.get(appProperties.getGeneratedProjectsDirectory(), project.getName());
            executeStage(jobId, GenerationStage.STRUCTURE, "Generating project structure for: " + project.getName(), () -> {
                // Structure generation is part of projectGenerator.generate()
            });

            // Stage: ENTITIES
            executeStage(jobId, GenerationStage.ENTITIES, "Generating entities (" + project.getEntities().size() + " entities)", () -> {
                projectGenerator.generate(project, output);
            });

            // Stage: SECURITY
            executeStage(jobId, GenerationStage.SECURITY, "Security: " + (project.isSecurityEnabled() ? project.getSecurityType() : "disabled"), () -> {
                // Security already generated within projectGenerator.generate()
            });

            // Stage: BUILD
            executeStage(jobId, GenerationStage.BUILD, "Running Maven compile verification", () -> {
                buildVerifier.verify(output.toString());
            });

            // Stage: ZIP
            Path zipPath = Paths.get(output.toString() + ".zip");
            executeStage(jobId, GenerationStage.ZIP, "Creating ZIP archive", () -> {
                try {
                    ZipUtil.zipFolder(output, project.getName());
                } catch (Exception e) {
                    throw new RuntimeException("ZIP creation failed: " + e.getMessage(), e);
                }
            });

            // Update final ZIP path
            Path finalZipPath = ZipUtil.zipFolder(output, project.getName());
            markCompleted(jobId, finalZipPath.toString());
            log.info("Generation completed for job={}", jobId);

        } catch (Exception e) {
            log.error("Generation failed for job={}: {}", jobId, e.getMessage(), e);
            markFailed(jobId, e.getMessage());

            // Log failure stage
            logStageFailure(jobId, "PIPELINE", e);

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

    /**
     * Executes a single stage, logs it to DB with timing and status.
     */
    private void executeStage(String jobId, GenerationStage stage, String message, Runnable action) {
        LocalDateTime startedAt = LocalDateTime.now();
        updateProgress(jobId, stage);

        log.info("Job={} Stage={} START: {}", jobId, stage.name(), message);

        try {
            action.run();

            long durationMs = java.time.Duration.between(startedAt, LocalDateTime.now()).toMillis();

            // Log successful stage
            stageLogRepository.save(GenerationStageLog.builder()
                    .jobId(jobId)
                    .stage(stage.name())
                    .status("COMPLETED")
                    .message(message)
                    .durationMs(durationMs)
                    .startedAt(startedAt)
                    .completedAt(LocalDateTime.now())
                    .build());

            log.info("Job={} Stage={} COMPLETED in {}ms", jobId, stage.name(), durationMs);

        } catch (Exception e) {
            long durationMs = java.time.Duration.between(startedAt, LocalDateTime.now()).toMillis();

            // Log failed stage
            stageLogRepository.save(GenerationStageLog.builder()
                    .jobId(jobId)
                    .stage(stage.name())
                    .status("FAILED")
                    .message(message)
                    .errorMessage(truncate(e.getMessage(), 2000))
                    .stackTrace(getStackTrace(e))
                    .durationMs(durationMs)
                    .startedAt(startedAt)
                    .completedAt(LocalDateTime.now())
                    .build());

            log.error("Job={} Stage={} FAILED after {}ms: {}", jobId, stage.name(), durationMs, e.getMessage());
            throw e; // Re-throw to stop pipeline
        }
    }

    private void logStageFailure(String jobId, String stageName, Exception e) {
        try {
            stageLogRepository.save(GenerationStageLog.builder()
                    .jobId(jobId)
                    .stage(stageName)
                    .status("FAILED")
                    .message("Pipeline failed")
                    .errorMessage(truncate(e.getMessage(), 2000))
                    .stackTrace(getStackTrace(e))
                    .startedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build());
        } catch (Exception logEx) {
            log.warn("Failed to persist stage failure log for job={}: {}", jobId, logEx.getMessage());
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

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        return trace.length() > 5000 ? trace.substring(0, 5000) : trace;
    }
}
