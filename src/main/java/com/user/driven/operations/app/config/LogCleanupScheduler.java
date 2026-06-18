package com.user.driven.operations.app.config;

import com.user.driven.operations.app.core.repository.AuditLogRepository;
import com.user.driven.operations.app.core.repository.ApplicationLogRepository;
import com.user.driven.operations.app.common.util.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled cleanup job that deletes Audit_Log and Application_Log records
 * older than the configured retention period.
 * 
 * Runs daily at 2 AM by default.
 * 
 * @see com.user.driven.operations.app.core.repository.AuditLogRepository
 * @see com.user.driven.operations.app.core.repository.ApplicationLogRepository
 */
@Component
@RequiredArgsConstructor
public class LogCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(LogCleanupScheduler.class);

    private final AuditLogRepository auditLogRepository;
    private final ApplicationLogRepository applicationLogRepository;

    @Value("${app.log-retention-days:30}")
    private int retentionDays;

    /**
     * Scheduled cleanup job that runs daily at 2 AM.
     * Deletes AuditLog and ApplicationLog records older than the configured retention period.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        log.info(MessageConstants.LOG_CLEANUP_START, cutoff, retentionDays);

        try {
            auditLogRepository.deleteByCreatedAtBefore(cutoff);
            applicationLogRepository.deleteByCreatedAtBefore(cutoff);
            log.info(MessageConstants.LOG_CLEANUP_SUCCESS);
        } catch (Exception e) {
            log.error(MessageConstants.LOG_CLEANUP_FAILED, e.getMessage(), e);
        }
    }
}
