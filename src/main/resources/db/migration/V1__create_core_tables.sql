-- V1: Create core tables for project definitions, entity definitions, field definitions, and operation configs

CREATE TABLE project_definitions (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL UNIQUE,
    description           TEXT,
    package_name          VARCHAR(255) NOT NULL,
    database_type         VARCHAR(50) DEFAULT 'H2',
    security_enabled      BOOLEAN DEFAULT FALSE,
    security_type         VARCHAR(50),
    caching_enabled       BOOLEAN DEFAULT FALSE,
    swagger_enabled       BOOLEAN DEFAULT TRUE,
    custom_configuration  TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE entity_definitions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    project_id  BIGINT NOT NULL REFERENCES project_definitions(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(name, project_id)
);

CREATE TABLE field_definitions (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    data_type           VARCHAR(50) NOT NULL,
    field_type          VARCHAR(50) NOT NULL,
    validation_rules    VARCHAR(500),
    relationship_type   VARCHAR(50),
    relationship_target VARCHAR(255),
    nullable            BOOLEAN DEFAULT TRUE,
    default_value       VARCHAR(255),
    reference_entity    VARCHAR(255),
    reference_field     VARCHAR(255),
    entity_id           BIGINT NOT NULL REFERENCES entity_definitions(id) ON DELETE CASCADE
);

CREATE TABLE operation_configs (
    id             BIGSERIAL PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL,
    enabled        BOOLEAN DEFAULT TRUE,
    custom_logic   TEXT,
    parameters     TEXT,
    entity_id      BIGINT NOT NULL REFERENCES entity_definitions(id) ON DELETE CASCADE
);

-- Indexes on foreign key columns
CREATE INDEX idx_entity_definitions_project_id ON entity_definitions(project_id);
CREATE INDEX idx_field_definitions_entity_id ON field_definitions(entity_id);
CREATE INDEX idx_operation_configs_entity_id ON operation_configs(entity_id);
