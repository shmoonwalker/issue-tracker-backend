DROP TABLE IF EXISTS ticket_comments;
DROP TABLE IF EXISTS ticket_assignees;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS workspace_members;
DROP TABLE IF EXISTS workspaces;
DROP TABLE IF EXISTS users;


DROP TYPE IF EXISTS ticket_priority;
DROP TYPE IF EXISTS ticket_status;
DROP TYPE IF EXISTS member_role;

CREATE TYPE member_role AS ENUM (
    'OWNER',
    'ADMIN',
    'MEMBER'
    );

CREATE TYPE ticket_status AS ENUM (
    'BACKLOG',
    'TODO',
    'IN_PROGRESS',
    'IN_REVIEW',
    'DONE'
    );

CREATE TYPE ticket_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT'
    );

CREATE TABLE IF NOT EXISTS users
(
    id            bigserial primary key,
    name          varchar(255) not null CHECK (char_length(trim(name)) >= 3),
    email         varchar(255) not null CHECK (char_length(trim(email)) >= 5),
    password_hash varchar(255) not null CHECK (char_length(password_hash) >= 20),
    created_at    timestamp    not null default current_timestamp,
    updated_at    timestamp,

    CHECK (updated_at IS NULL OR updated_at >= created_at)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique_lower
    ON users (lower(trim(email)));

CREATE TABLE IF NOT EXISTS workspaces
(
    id                 bigserial primary key,
    name               varchar(255) not null CHECK (char_length(trim(name)) >= 3),
    created_by_user_id bigint       not null,
    created_at         timestamp    not null default current_timestamp,
    updated_at         timestamp,
    archived_at        timestamp,

    CONSTRAINT fk_workspaces_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id),

    CHECK (updated_at IS NULL OR updated_at >= created_at),
    CHECK (archived_at IS NULL OR archived_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_workspaces_created_by_user_id
    ON workspaces (created_by_user_id);

CREATE TABLE IF NOT EXISTS workspace_members
(
    workspace_id     bigint      not null,
    user_id          bigint      not null,
    role             member_role not null,
    joined_at        timestamp   not null default current_timestamp,
    added_by_user_id bigint,

    primary key (workspace_id, user_id),

    CONSTRAINT fk_workspace_members_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workspace_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workspace_members_added_by_user
        FOREIGN KEY (added_by_user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_workspace_members_user_id
    ON workspace_members (user_id);

CREATE INDEX IF NOT EXISTS idx_workspace_members_workspace_id_role
    ON workspace_members (workspace_id, role);

CREATE TABLE IF NOT EXISTS projects
(
    id                 bigserial primary key,
    workspace_id       bigint       not null,
    name               varchar(255) not null CHECK (char_length(trim(name)) >= 3),
    description        text,
    created_by_user_id bigint       not null,
    created_at         timestamp    not null default current_timestamp,
    updated_at         timestamp,
    archived_at        timestamp,

    CONSTRAINT fk_projects_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_projects_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id),

    CHECK (updated_at IS NULL OR updated_at >= created_at),
    CHECK (archived_at IS NULL OR archived_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_projects_workspace_id
    ON projects (workspace_id);

CREATE INDEX IF NOT EXISTS idx_projects_created_by_user_id
    ON projects (created_by_user_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_projects_unique_active_name_per_workspace
    ON projects (workspace_id, lower(name))
    WHERE archived_at IS NULL;

CREATE TABLE IF NOT EXISTS tickets
(
    id                 bigserial primary key,
    project_id         bigint          not null,
    created_by_user_id bigint          not null,
    title              varchar(255)    not null CHECK (char_length(trim(title)) >= 3),
    description        text,
    status             ticket_status   not null default 'BACKLOG',
    priority           ticket_priority not null default 'MEDIUM',
    due_date           date,
    created_at         timestamp       not null default current_timestamp,
    updated_at         timestamp,
    archived_at        timestamp,

    CONSTRAINT fk_tickets_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_tickets_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id),

    CHECK (updated_at IS NULL OR updated_at >= created_at),
    CHECK (archived_at IS NULL OR archived_at >= created_at),
    CHECK (due_date IS NULL OR due_date >= created_at::date)
);

CREATE INDEX IF NOT EXISTS idx_tickets_project_id
    ON tickets (project_id);

CREATE INDEX IF NOT EXISTS idx_tickets_project_id_status
    ON tickets (project_id, status);

CREATE INDEX IF NOT EXISTS idx_tickets_created_by_user_id
    ON tickets (created_by_user_id);

CREATE INDEX IF NOT EXISTS idx_tickets_priority
    ON tickets (priority);

CREATE TABLE IF NOT EXISTS ticket_assignees
(
    ticket_id           bigint    not null,
    user_id             bigint    not null,
    assigned_at         timestamp not null default current_timestamp,
    assigned_by_user_id bigint,

    primary key (ticket_id, user_id),

    CONSTRAINT fk_ticket_assignees_ticket
        FOREIGN KEY (ticket_id)
            REFERENCES tickets (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ticket_assignees_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ticket_assignees_assigned_by_user
        FOREIGN KEY (assigned_by_user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_ticket_assignees_user_id
    ON ticket_assignees (user_id);

CREATE INDEX IF NOT EXISTS idx_ticket_assignees_ticket_id
    ON ticket_assignees (ticket_id);

CREATE TABLE IF NOT EXISTS ticket_comments
(
    id         bigserial primary key,
    ticket_id  bigint    not null,
    user_id    bigint    not null,
    body       text      not null CHECK (char_length(trim(body)) >= 1),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    CONSTRAINT fk_ticket_comments_ticket
        FOREIGN KEY (ticket_id)
            REFERENCES tickets (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ticket_comments_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CHECK (updated_at IS NULL OR updated_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket_id
    ON ticket_comments (ticket_id);

CREATE INDEX IF NOT EXISTS idx_ticket_comments_user_id
    ON ticket_comments (user_id);