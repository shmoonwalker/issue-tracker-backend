package net.hackyourfuture.tickettrackingsystem.repository;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.model.Ticket;
import net.hackyourfuture.tickettrackingsystem.model.TicketStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TicketRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Ticket> ticketRowMapper = (rs, rowNum) ->
            new Ticket(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getLong("project_id"),
                    TicketStatus.valueOf(rs.getString("status")),
                    rs.getTimestamp("creation_date").toLocalDateTime(),
                    rs.getTimestamp("update_date") != null
                            ? rs.getTimestamp("update_date").toLocalDateTime()
                            : null
            );

    public Ticket create(Ticket ticket) {
        String sql = """
                INSERT INTO tickets (title, description, project_id, status)
                VALUES (?, ?, ?, ?::ticket_status)
                RETURNING id, title, description, project_id, status, creation_date, update_date
                """;

        return jdbcTemplate.queryForObject(
                sql,
                ticketRowMapper,
                ticket.title(),
                ticket.description(),
                ticket.projectId(),
                ticket.status().name()
        );
    }

    public List<Ticket> findAll(String text, TicketStatus status) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, title, description, project_id, status, creation_date, update_date
            FROM tickets
            WHERE 1 = 1
            """);

        List<Object> params = new ArrayList<>();

        String cleanedText = text == null ? null : text.trim();

        if (cleanedText != null && !cleanedText.isBlank()) {
            sql.append("""
                AND (
                    strpos(LOWER(title), LOWER(?)) > 0
                    OR strpos(LOWER(COALESCE(description, '')), LOWER(?)) > 0
                )
                """);

            params.add(cleanedText);
            params.add(cleanedText);
        }

        if (status != null) {
            sql.append("""
                AND status = ?::ticket_status
                """);

            params.add(status.name());
        }

        sql.append(" ORDER BY id");

        return jdbcTemplate.query(
                sql.toString(),
                ticketRowMapper,
                params.toArray()
        );
    }


    public Optional<Ticket> findById(Long id) {
        String sql = """
                SELECT id, title, description, project_id, status, creation_date, update_date
                FROM tickets
                WHERE id = ?
                """;

        try {
            Ticket ticket = jdbcTemplate.queryForObject(sql, ticketRowMapper, id);
            return Optional.of(ticket);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Ticket update(Ticket ticket) {
        String sql = """
                UPDATE tickets
                SET title = ?,
                    description = ?,
                    project_id = ?,
                    status = ?::ticket_status,
                    update_date = CURRENT_TIMESTAMP
                WHERE id = ?
                RETURNING id, title, description, project_id, status, creation_date, update_date
                """;

        return jdbcTemplate.queryForObject(
                sql,
                ticketRowMapper,
                ticket.title(),
                ticket.description(),
                ticket.projectId(),
                ticket.status().name(),
                ticket.id()
        );
    }



    public boolean assignUser(Long ticketId, Long userId) {
        String sql = """
                INSERT INTO user_ticket (ticket_id, user_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        int rowsAffected = jdbcTemplate.update(sql, ticketId, userId);

        return rowsAffected > 0;
    }

    public boolean unassignUser(Long ticketId, Long userId) {
        String sql = """
                DELETE FROM user_ticket
                WHERE ticket_id = ?
                  AND user_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(sql, ticketId, userId);

        return rowsAffected > 0;
    }

    public Map<Long, List<Long>> findAssignedUserIdsByTicketIds(Collection<Long> ticketIds) {
        if (ticketIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = ticketIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT ticket_id, user_id
                FROM user_ticket
                WHERE ticket_id IN (%s)
                ORDER BY ticket_id, user_id
                """.formatted(placeholders);

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, List<Long>> result = new HashMap<>();
            while (rs.next()) {
                long ticketId = rs.getLong("ticket_id");
                long userId = rs.getLong("user_id");
                result.computeIfAbsent(ticketId, k -> new ArrayList<>()).add(userId);
            }
            return result;
        }, ticketIds.toArray());
    }

    public List<Long> findAssignedUserIds(Long ticketId) {
        String sql = """
                SELECT user_id
                FROM user_ticket
                WHERE ticket_id = ?
                ORDER BY user_id
                """;

        return jdbcTemplate.queryForList(sql, Long.class, ticketId);
    }

    public List<String> findAssigneeEmailsByTicketId(Long ticketId) {
        String sql = """
            SELECT u.email
            FROM users u
            JOIN user_ticket ut ON ut.user_id = u.id
            WHERE ut.ticket_id = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("email"),
                ticketId
        );
    }
}