package com.user.driven.operations.app.core.repository;

import com.user.driven.operations.app.core.model.GenerationJob;
import com.user.driven.operations.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GenerationJobRepository extends JpaRepository<GenerationJob, String> {

    long countByUserIdAndStatusIn(Long userId, List<JobStatus> statuses);

    List<GenerationJob> findByStatusAndExpiresAtBefore(JobStatus status, LocalDateTime dateTime);

    @Modifying
    @Query("UPDATE GenerationJob j SET j.status = :status WHERE j.id = :id")
    void updateStatus(@Param("id") String id, @Param("status") JobStatus status);

    List<GenerationJob> findByStatusAndCompletedAtBefore(JobStatus status, LocalDateTime dateTime);
}
