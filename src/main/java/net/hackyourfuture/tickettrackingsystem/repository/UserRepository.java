package net.hackyourfuture.tickettrackingsystem.repository;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) ->
            new User(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash")
            );

    public List<User> findAll() {
        String sql = """
                SELECT id, name, email, password_hash
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(Long id) {
        String sql = """
                SELECT id, name, email, password_hash
                FROM users
                WHERE id = ?
                """;

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT id, name, email, password_hash
                FROM users
                WHERE lower(trim(email)) = lower(trim(?))
                """;

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public User create(User user) {
        String sql = """
            INSERT INTO users (name, email, password_hash)
            VALUES (trim(?), lower(trim(?)), ?)
            RETURNING id, name, email, password_hash
            """;

        return jdbcTemplate.queryForObject(
                sql,
                userRowMapper,
                user.name(),
                user.email(),
                user.passwordHash()
        );
    }

    public User update(User user) {
        String sql = """
            UPDATE users
            SET name = trim(?),
                email = lower(trim(?)),
                updated_at = current_timestamp
            WHERE id = ?
            RETURNING id, name, email, password_hash
            """;

        return jdbcTemplate.queryForObject(
                sql,
                userRowMapper,
                user.name(),
                user.email(),
                user.id()
        );
    }

    public boolean existsByEmail(String email) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM users
                WHERE lower(trim(email)) = lower(trim(?))
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, email);
        return Boolean.TRUE.equals(exists);
    }

}