package com.user.driven.operations.app.core.repository;

import com.user.driven.operations.app.core.model.GenerationStageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenerationStageLogRepository extends JpaRepository<GenerationStageLog, Long> {

    List<GenerationStageLog> findByJobIdOrderByStartedAtAsc(String jobId);

    List<GenerationStageLog> findByStatus(String status);
}
