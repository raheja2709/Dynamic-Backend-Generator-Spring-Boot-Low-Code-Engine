-- V2: Create audit and application logging tables

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    request_id      VARCHAR(36) NOT NULL,
    method          VARCHAR(10) NOT NULL,
    endpoint        VARCHAR(500) NOT NULL,
    request_body    TEXT,
    response_status INTEGER,
    error_message   TEXT,
    stack_trace     TEXT,
    stage           VARCHAR(50),
    duration_ms     BIGINT,
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE application_logs (
    id          BIGSERIAL PRIMARY KEY,
    level       VARCHAR(10) NOT NULL,
    logger_name VARCHAR(255) NOT NULL,
    message     TEXT NOT NULL,
    stack_trace TEXT,
    request_id  VARCHAR(36),
    user_id     BIGINT,
    context     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_endpoint ON audit_logs(endpoint);
CREATE INDEX idx_application_logs_level ON application_logs(level);
CREATE INDEX idx_application_logs_created_at ON application_logs(created_at);
CREATE INDEX idx_application_logs_request_id ON application_logs(request_id);
