package net.hackyourfuture.tickettrackingsystem.repository;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Project> projectRowMapper = (rs, rowNum) ->
            new Project(
                    rs.getLong("id"),
                    rs.getLong("workspace_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getLong("created_by_user_id")
            );

    public Project create(Project project) {
        String sql = """
                INSERT INTO projects (workspace_id, name, description, created_by_user_id)
                VALUES (?, trim(?), ?, ?)
                RETURNING id, workspace_id, name, description, created_by_user_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                projectRowMapper,
                project.workspaceId(),
                project.name(),
                project.description(),
                project.createdByUserId()
        );
    }

    public List<Project> findByWorkspaceId(Long workspaceId) {
        String sql = """
                SELECT id, workspace_id, name, description, created_by_user_id
                FROM projects
                WHERE workspace_id = ?
                  AND archived_at IS NULL
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, projectRowMapper, workspaceId);
    }
}