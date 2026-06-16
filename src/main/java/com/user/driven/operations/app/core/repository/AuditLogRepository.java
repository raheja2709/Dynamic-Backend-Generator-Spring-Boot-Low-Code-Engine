package com.user.driven.operations.app.core.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.AuditLog;

/**
 * Repository interface for {@link AuditLog} entity.
 * Provides CRUD operations and a cleanup method for removing old audit records.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Deletes all audit log records created before the specified cutoff timestamp.
     * Used by the scheduled cleanup job to remove records older than the retention period.
     *
     * @param cutoff the cutoff timestamp; records older than this will be deleted
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
