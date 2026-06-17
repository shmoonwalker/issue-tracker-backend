package net.hackyourfuture.tickettrackingsystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "TRUNCATE TABLE user_ticket, tickets, users, projects RESTART IDENTITY CASCADE",
                "INSERT INTO users (name, email) VALUES ('Alice Doe', 'alice@example.com')",
                "INSERT INTO users (name, email) VALUES ('Bob Smith', 'bob@example.com')"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllUsers_returnsBothSeededUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"));
    }

    @Test
    void getUserById_whenUserMissing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createUser_withDuplicateEmail_returnsConflict() throws Exception {
        String body = """
                { "name": "Another Alice", "email": "alice@example.com" }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createUser_withInvalidEmail_returnsBadRequestWithFieldError() throws Exception {
        String body = """
                { "name": "Eve", "email": "not-an-email" }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void createUser_withValidPayload_returnsCreatedUser() throws Exception {
        String body = """
                { "name": "Carol", "email": "carol@example.com" }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Carol"))
                .andExpect(jsonPath("$.email").value("carol@example.com"));
    }

    @Test
    void deleteUser_whenUserMissing_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }
}
