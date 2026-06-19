-- V3__create_route_container_assignments.sql
-- Route-to-Container many-to-many assignment table
-- Replaces single-container Route.containerId field
-- H2 dev uses ddl-auto=create-drop which picks up entity changes automatically
-- Apply this migration manually to PostgreSQL production

CREATE TABLE route_container_assignments (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    container_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_route_container_assignments_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_route_container_assignments_container FOREIGN KEY (container_id) REFERENCES containers(id)
);

CREATE INDEX idx_route_container_assignments_route_id ON route_container_assignments(route_id);
CREATE INDEX idx_route_container_assignments_container_id ON route_container_assignments(container_id);