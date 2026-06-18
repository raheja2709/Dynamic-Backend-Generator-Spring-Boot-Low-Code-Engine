-- V5: Add frontend_enabled flag to project_definitions
ALTER TABLE project_definitions ADD COLUMN IF NOT EXISTS frontend_enabled BOOLEAN DEFAULT FALSE;
