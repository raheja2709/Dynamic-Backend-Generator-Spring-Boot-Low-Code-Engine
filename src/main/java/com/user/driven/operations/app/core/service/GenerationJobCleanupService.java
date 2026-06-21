package com.user.driven.operations.app.core.service;

import com.user.driven.operations.app.core.model.GenerationJob;
import com.user.driven.operations.app.core.repository.GenerationJobRepository;
import com.user.driven.operations.enums.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Scheduled service that cleans up expired generation jobs and their files.
 * Runs every hour, marks completed jobs older than 24 hours as EXPIRED,
 * and deletes their associated ZIP files and output folders.
 *
 * @author Jatin Raheja
 */
@Slf4j
@Service
public class GenerationJobCleanupService {

    private final GenerationJobRepository jobRepository;

    public GenerationJobCleanupService(GenerationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Runs every hour to clean up expired generation jobs.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredJobs() {
        log.info("Starting generation job cleanup");

        LocalDateTime now = LocalDateTime.now();

        // Find completed jobs past their expiration time
        List<GenerationJob> expiredJobs = jobRepository.findByStatusAndCompletedAtBefore(
                JobStatus.COMPLETED, now.minusHours(24));

        int cleaned = 0;
        for (GenerationJob job : expiredJobs) {
            try {
                // Delete ZIP file
                if (job.getOutputPath() != null) {
                    Path zipPath = Path.of(job.getOutputPath());
                    if (Files.exists(zipPath)) {
                        Files.deleteIfExists(zipPath);
                        log.debug("Deleted ZIP for job={}: {}", job.getId(), zipPath);
                    }

                    // Also try to delete the output folder (ZIP name without extension)
                    String folderName = zipPath.getFileName().toString().replace(".zip", "");
                    Path folder = zipPath.getParent().resolve(folderName);
                    if (Files.exists(folder) && Files.isDirectory(folder)) {
                        deleteDirectory(folder);
                        log.debug("Deleted output folder for job={}: {}", job.getId(), folder);
                    }
                }

                // Mark as expired
                job.setStatus(JobStatus.EXPIRED);
                jobRepository.save(job);
                cleaned++;

            } catch (IOException e) {
                log.warn("Failed to cleanup files for job={}: {}", job.getId(), e.getMessage());
            }
        }

        if (cleaned > 0) {
            log.info("Cleaned up {} expired generation jobs", cleaned);
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
    }
}
