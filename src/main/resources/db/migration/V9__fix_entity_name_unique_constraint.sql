-- V9: Remove global unique constraint on entity_definitions.name
-- Entity names should be unique per project, not globally.
-- The per-project uniqueness is enforced at the application level.

ALTER TABLE entity_definitions DROP CONSTRAINT IF EXISTS entity_definitions_name_key;
DROP INDEX IF EXISTS entity_definitions_name_key;

-- Also try the Hibernate-generated constraint name format
ALTER TABLE entity_definitions DROP CONSTRAINT IF EXISTS uk_entity_definitions_name;
