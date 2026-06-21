package com.user.driven.operations.app.api.dto;

import com.user.driven.operations.app.core.model.GenerationJob;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for generation job status polling.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusResponse {

    private String jobId;
    private String status;
    private String currentStage;
    private int progress;
    private String projectName;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;

    public static JobStatusResponse from(GenerationJob job) {
        return JobStatusResponse.builder()
                .jobId(job.getId())
                .status(job.getStatus().name())
                .currentStage(job.getCurrentStage() != null ? job.getCurrentStage().name() : null)
                .progress(job.getProgress())
                .projectName(job.getProjectName())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .expiresAt(job.getExpiresAt())
                .build();
    }
}
