-- V6: Add additional indexes for frequently queried columns

-- Project name for uniqueness and search
CREATE INDEX IF NOT EXISTS idx_project_definitions_name ON project_definitions(name);

-- Entity name + project for uniqueness check
CREATE INDEX IF NOT EXISTS idx_entity_definitions_name_project ON entity_definitions(name, project_id);

-- Audit log user_id for user-specific queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);

-- Generation stage for stage-based filtering
CREATE INDEX IF NOT EXISTS idx_audit_logs_stage ON audit_logs(stage);
