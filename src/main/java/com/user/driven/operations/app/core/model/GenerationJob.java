package com.user.driven.operations.app.core.model;

import com.user.driven.operations.enums.GenerationStage;
import com.user.driven.operations.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an asynchronous project generation job.
 * Tracks status, progress, and output path through the generation pipeline.
 *
 * @author Jatin Raheja
 */
@Entity
@Table(name = "generation_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationJob {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage")
    private GenerationStage currentStage;

    @Column(nullable = false)
    private int progress;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "output_path", length = 500)
    private String outputPath;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = JobStatus.QUEUED;
        }
    }
}
