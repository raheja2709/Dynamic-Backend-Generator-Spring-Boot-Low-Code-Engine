-- V7: Create generation_jobs table for async generation pipeline

CREATE TABLE generation_jobs (
    id              VARCHAR(36) PRIMARY KEY,
    project_id      BIGINT REFERENCES project_definitions(id),
    user_id         BIGINT REFERENCES app_users(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    current_stage   VARCHAR(20),
    progress        INTEGER NOT NULL DEFAULT 0,
    error_message   VARCHAR(2000),
    output_path     VARCHAR(500),
    project_name    VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    expires_at      TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_generation_jobs_user_id ON generation_jobs(user_id);
CREATE INDEX idx_generation_jobs_status ON generation_jobs(status);
CREATE INDEX idx_generation_jobs_created_at ON generation_jobs(created_at);
CREATE INDEX idx_generation_jobs_completed_at ON generation_jobs(completed_at);
