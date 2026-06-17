CREATE TYPE ticket_status AS ENUM (
    'OPEN', 'IN_PROGRESS', 'CLOSED');

CREATE TABLE users (
    id bigserial primary key ,
    name varchar(255) not null CHECK (char_length(name) >= 3),
    email varchar(255) not null unique

);
CREATE TABLE projects (
    id bigserial primary key ,
    name varchar(255) not null CHECK (char_length(name) >= 3)
);
CREATE TABLE tickets (
    id bigserial primary key ,
    title varchar(255) not null,
    description varchar(255) ,
    project_id BIGINT NOT NULL,
    status ticket_status not null,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP,
    CONSTRAINT fk_ticket_project
        FOREIGN KEY (project_id)
            REFERENCES projects(id)

);

CREATE TABLE user_ticket (
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

CREATE INDEX idx_tickets_status
    ON tickets(status);

CREATE INDEX idx_tickets_project_id
    ON tickets(project_id);

CREATE INDEX idx_user_ticket_user_id
    ON user_ticket(user_id);

CREATE INDEX idx_user_ticket_ticket_id
    ON user_ticket(ticket_id);