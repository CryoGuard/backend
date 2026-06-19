-- V4__add_last_config_sync.sql
-- Track when edge last synced its config from the backend
-- Separates config sync from telemetry updates (lastUpdate) and heartbeat (lastSeenAt)

ALTER TABLE containers ADD COLUMN last_config_sync_at TIMESTAMP;
