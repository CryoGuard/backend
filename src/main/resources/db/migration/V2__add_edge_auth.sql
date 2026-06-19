-- V2__add_edge_auth.sql
-- Multi-edge authentication columns for Container aggregate
-- Applied manually to PostgreSQL production before deploying PR1
-- H2 dev uses ddl-auto=create-drop which picks up entity changes automatically

ALTER TABLE containers ADD COLUMN api_key_hash VARCHAR(255);
ALTER TABLE containers ADD COLUMN last_seen_at TIMESTAMP;
