-- V4: Create relationship_definitions table and migrate legacy relationship data from field_definitions

CREATE TABLE relationship_definitions (
    id                   BIGSERIAL PRIMARY KEY,
    entity_id            BIGINT NOT NULL REFERENCES entity_definitions(id) ON DELETE CASCADE,
    relationship_type    VARCHAR(50) NOT NULL,
    target_entity        VARCHAR(255) NOT NULL,
    field_name           VARCHAR(255) NOT NULL,
    mapped_by            VARCHAR(255),
    fetch_type           VARCHAR(10) DEFAULT 'LAZY',
    cascade_types        VARCHAR(255),
    orphan_removal       BOOLEAN DEFAULT FALSE,
    join_column          VARCHAR(255),
    join_table_name      VARCHAR(255),
    inverse_join_column  VARCHAR(255),
    nullable             BOOLEAN DEFAULT TRUE,
    dto_strategy         VARCHAR(20) DEFAULT 'ID_ONLY',
    json_handling        VARCHAR(30) DEFAULT 'BACK_REFERENCE',
    created_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_relationship_defs_entity_id ON relationship_definitions(entity_id);

-- Migrate existing relationship data from field_definitions (legacy fields)
INSERT INTO relationship_definitions (entity_id, relationship_type, target_entity, field_name, nullable, created_at)
SELECT entity_id, relationship_type, relationship_target, name, nullable, NOW()
FROM field_definitions
WHERE relationship_type IS NOT NULL AND relationship_target IS NOT NULL;
