package net.hackyourfuture.tickettrackingsystem.repository;


import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.model.Workspace;
import net.hackyourfuture.tickettrackingsystem.model.WorkspaceMember;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WorkspaceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Workspace> workspaceRowMapper = (rs, rowNum) ->
            new Workspace(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("created_by_user_id")
            );

    public Workspace create(Workspace workspace) {
        String sql = """
                INSERT INTO workspaces (name, created_by_user_id)
                VALUES (trim(?), ?)
                RETURNING id, name, created_by_user_id""";
        return jdbcTemplate.queryForObject(sql, workspaceRowMapper, workspace.name(), workspace.createdByUserId());

    }

    public void addMember(WorkspaceMember workspaceMember) {
        String sql = """
            INSERT INTO workspace_members (workspace_id, user_id, role, added_by_user_id)
            VALUES (?, ?, ?::member_role, ?)
            """;

        jdbcTemplate.update(
                sql,
                workspaceMember.workspaceId(),
                workspaceMember.userId(),
                workspaceMember.role().name(),
                workspaceMember.addedByUserId()
        );
    }

    public List<Workspace> findByUserId(Long userId) {
        String sql = """
            SELECT w.id, w.name, w.created_by_user_id
            FROM workspaces w
            JOIN workspace_members wm ON wm.workspace_id = w.id
            WHERE wm.user_id = ?
              AND w.archived_at IS NULL
            ORDER BY w.id
            """;

        return jdbcTemplate.query(sql, workspaceRowMapper, userId);
    }

}
