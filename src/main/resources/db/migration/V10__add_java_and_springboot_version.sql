-- V10: Add java_version and spring_boot_version columns to project_definitions
ALTER TABLE project_definitions ADD COLUMN IF NOT EXISTS java_version VARCHAR(10) DEFAULT '17';
ALTER TABLE project_definitions ADD COLUMN IF NOT EXISTS spring_boot_version VARCHAR(20) DEFAULT '3.2.5';
