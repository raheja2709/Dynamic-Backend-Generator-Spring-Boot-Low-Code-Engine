-- V8: Create generation_stage_logs for tracking each stage of the generation pipeline

CREATE TABLE generation_stage_logs (
    id              BIGSERIAL PRIMARY KEY,
    job_id          VARCHAR(36) NOT NULL,
    stage           VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    message         TEXT,
    error_message   TEXT,
    stack_trace     TEXT,
    duration_ms     BIGINT,
    started_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

-- Indexes for querying by job and stage
CREATE INDEX idx_generation_stage_logs_job_id ON generation_stage_logs(job_id);
CREATE INDEX idx_generation_stage_logs_stage ON generation_stage_logs(stage);
CREATE INDEX idx_generation_stage_logs_status ON generation_stage_logs(status);
