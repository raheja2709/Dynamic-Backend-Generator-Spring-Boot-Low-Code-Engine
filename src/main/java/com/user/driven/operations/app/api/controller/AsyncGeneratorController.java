package com.user.driven.operations.app.api.controller;

import com.user.driven.operations.app.api.dto.GenerateProjectRequest;
import com.user.driven.operations.app.api.dto.JobStatusResponse;
import com.user.driven.operations.app.api.mapper.ProjectMapper;
import com.user.driven.operations.app.common.util.AppConstants;
import com.user.driven.operations.app.core.model.GenerationJob;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.repository.GenerationJobRepository;
import com.user.driven.operations.app.core.service.AsyncGenerationExecutor;
import com.user.driven.operations.enums.JobStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Async generation controller. Submits generation jobs and allows polling/download.
 *
 * @author Jatin Raheja
 */
@Slf4j
@RestController
@RequestMapping(AppConstants.GENERATOR_V1)
@Tag(name = "Async Project Generation", description = "Asynchronous project generation with job tracking")
public class AsyncGeneratorController {

    private static final int MAX_CONCURRENT_JOBS = 3;

    private final GenerationJobRepository jobRepository;
    private final AsyncGenerationExecutor asyncExecutor;

    public AsyncGeneratorController(GenerationJobRepository jobRepository,
                                    AsyncGenerationExecutor asyncExecutor) {
        this.jobRepository = jobRepository;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * Submits a project generation job. Returns immediately with job ID (HTTP 202).
     * Rejects if the user already has 3+ concurrent (QUEUED/PROCESSING) jobs.
     */
    @PostMapping("/async/generate")
    @Operation(summary = "Submit async generation job", description = "Submits a project for async generation and returns a job ID for polling")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Job accepted"),
            @ApiResponse(responseCode = "429", description = "Too many concurrent jobs")
    })
    public ResponseEntity<?> submitGeneration(@Valid @RequestBody GenerateProjectRequest request,
                                              @RequestAttribute(value = "userId", required = false) Long userId) {
        // Check concurrent job limit
        Long effectiveUserId = userId != null ? userId : 0L;
        long activeJobs = jobRepository.countByUserIdAndStatusIn(effectiveUserId,
                List.of(JobStatus.QUEUED, JobStatus.PROCESSING));

        if (activeJobs >= MAX_CONCURRENT_JOBS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Too many concurrent generation jobs",
                            "message", "You have " + activeJobs + " active jobs. Maximum is " + MAX_CONCURRENT_JOBS,
                            "activeJobs", activeJobs
                    ));
        }

        // Create job
        GenerationJob job = GenerationJob.builder()
                .id(UUID.randomUUID().toString())
                .userId(effectiveUserId)
                .projectName(request.getName())
                .status(JobStatus.QUEUED)
                .progress(0)
                .build();
        job.setCreatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);

        // Map request and submit async
        ProjectDefinition project = ProjectMapper.map(request);
        asyncExecutor.executeGeneration(job.getId(), project);

        log.info("Submitted generation job={} for project={}", job.getId(), request.getName());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "jobId", job.getId(),
                        "status", "QUEUED",
                        "message", "Generation job submitted"
                ));
    }

    /**
     * Returns the status of a generation job including stage and progress.
     */
    @GetMapping("/status/{jobId}")
    @Operation(summary = "Get job status", description = "Returns current status, stage, and progress of a generation job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job status returned"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(job -> ResponseEntity.ok(JobStatusResponse.from(job)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Downloads the generated ZIP for a completed job.
     * Returns 404 if job not found or still in progress, 410 if expired.
     */
    @GetMapping("/download/{jobId}")
    @Operation(summary = "Download generated project", description = "Downloads the ZIP file for a completed generation job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ZIP file download"),
            @ApiResponse(responseCode = "404", description = "Job not found or not completed"),
            @ApiResponse(responseCode = "410", description = "Job expired, file no longer available")
    })
    public ResponseEntity<?> downloadResult(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(job -> {
                    if (job.getStatus() == JobStatus.EXPIRED) {
                        return ResponseEntity.status(HttpStatus.GONE)
                                .body((Object) Map.of("error", "Job expired", "message", "Generated files have been cleaned up"));
                    }
                    if (job.getStatus() != JobStatus.COMPLETED || job.getOutputPath() == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body((Object) Map.of("error", "Not ready", "status", job.getStatus().name()));
                    }

                    Path zipPath = Path.of(job.getOutputPath());
                    if (!Files.exists(zipPath)) {
                        return ResponseEntity.status(HttpStatus.GONE)
                                .body((Object) Map.of("error", "File not found", "message", "ZIP file has been removed"));
                    }

                    FileSystemResource resource = new FileSystemResource(zipPath.toFile());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipPath.getFileName())
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body((Object) resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
