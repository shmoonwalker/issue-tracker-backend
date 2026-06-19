# IssueFlow API

IssueFlow API is a Java Spring Boot backend for a lightweight issue tracking system designed for small development teams.

The project started as a mid-course ticket tracking system and is being extended into a cleaner portfolio backend API. The focus is on backend design, SQL, authentication, permissions, and a professional REST API structure.

## Core Idea

Small development teams often need more structure than simple notes or task lists, but less complexity than enterprise tools like Jira.

IssueFlow API helps teams organize project work by managing users, workspaces, projects, tickets, assignments, priorities, statuses, and comments.

## MVP Scope

- User registration and login
- Workspace creation
- Workspace members
- Role-based permissions: Owner, Admin, Member
- Projects inside a workspace
- Ticket creation and management
- Ticket assignment to team members
- Ticket statuses: Backlog, To Do, In Progress, In Review, Done
- Ticket priorities: Low, Medium, High, Urgent
- Ticket comments
- "My tickets" view
- API documentation

## Project Structure

```text
User
  -> Workspace
      -> Project
          -> Ticket
```

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring JDBC / JdbcTemplate
- PostgreSQL
- SQL migration scripts
- Bean Validation
- JUnit
- Swagger/OpenAPI

## Backend Architecture

The backend will follow a simple layered structure:

```text
Controller -> Service -> Repository -> PostgreSQL
```

- Controllers handle HTTP requests and responses.
- Services contain business rules and permission checks.
- Repositories use JDBC/JdbcTemplate and SQL queries.
- DTOs are used for request and response data.

## Portfolio Goal

The goal of this project is to demonstrate Java backend development skills through:

- clean REST API design
- relational database modeling
- direct SQL/JDBC data access
- authentication and authorization
- validation and error handling
- readable layered architecture
- API documentation
- focused backend testing

## Current Direction

This project is intentionally starting with a clean and realistic MVP. Extra features can be added later after the core backend is stable.