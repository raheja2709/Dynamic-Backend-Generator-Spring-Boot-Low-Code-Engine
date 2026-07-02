-- V6: Add additional indexes for frequently queried columns

-- Project name for search
CREATE INDEX IF NOT EXISTS idx_project_definitions_name ON project_definitions(name);

-- Entity name + project for uniqueness check
CREATE INDEX IF NOT EXISTS idx_entity_definitions_name_project ON entity_definitions(name, project_id);

-- Audit log stage for stage-based filtering
CREATE INDEX IF NOT EXISTS idx_audit_logs_stage ON audit_logs(stage);

-- Application logs level index (already exists from V2, skip if duplicate)
CREATE INDEX IF NOT EXISTS idx_application_logs_user_id ON application_logs(user_id);
