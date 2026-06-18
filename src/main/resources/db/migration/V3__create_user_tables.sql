-- V3: Create user authentication tables and add user ownership to projects

CREATE TABLE app_users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(255),
    role           VARCHAR(50) NOT NULL DEFAULT 'USER',
    enabled        BOOLEAN DEFAULT TRUE,
    locked         BOOLEAN DEFAULT FALSE,
    api_key        VARCHAR(255) UNIQUE,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at  TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Add user ownership to projects
ALTER TABLE project_definitions ADD COLUMN user_id BIGINT REFERENCES app_users(id);

-- Indexes
CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_app_users_api_key ON app_users(api_key);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_project_definitions_user_id ON project_definitions(user_id);
