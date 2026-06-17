DROP TABLE IF EXISTS user_ticket;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

DROP TYPE IF EXISTS ticket_status;

CREATE TYPE ticket_status AS ENUM (
    'OPEN',
    'IN_PROGRESS',
    'CLOSED'
    );

CREATE TABLE IF NOT EXISTS users (
    id bigserial primary key,
    name varchar(255) not null CHECK (char_length(name) >= 3),
    email varchar(255) not null unique
);

CREATE TABLE IF NOT EXISTS projects (
    id bigserial primary key,
    name varchar(255) not null CHECK (char_length(name) >= 3)
);

CREATE TABLE IF NOT EXISTS tickets (
    id bigserial primary key,
    title varchar(255) not null,
    description TEXT,
    project_id BIGINT NOT NULL,
    status ticket_status not null,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP,
    CONSTRAINT fk_ticket_project
        FOREIGN KEY (project_id)
            REFERENCES projects(id)
);

CREATE TABLE IF NOT EXISTS user_ticket (
    user_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,

    primary key (user_id, ticket_id),

    CONSTRAINT fk_user_ticket_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user_ticket_ticket
        FOREIGN KEY (ticket_id)
            REFERENCES tickets(id)
            ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tickets_status
    ON tickets(status);

CREATE INDEX IF NOT EXISTS idx_tickets_project_id
    ON tickets(project_id);

CREATE INDEX IF NOT EXISTS idx_user_ticket_ticket_id
    ON user_ticket(ticket_id);
