package net.hackyourfuture.tickettrackingsystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        statements = {
                "TRUNCATE TABLE user_ticket, tickets, users, projects RESTART IDENTITY CASCADE",
                "INSERT INTO projects (name) VALUES ('Project Alpha')",
                "INSERT INTO projects (name) VALUES ('Project Beta')",
                "INSERT INTO tickets (title, project_id, status) VALUES ('A1', 1, 'OPEN')",
                "INSERT INTO tickets (title, project_id, status) VALUES ('A2', 1, 'OPEN')",
                "INSERT INTO tickets (title, project_id, status) VALUES ('A3', 1, 'IN_PROGRESS')",
                "INSERT INTO tickets (title, project_id, status) VALUES ('A4', 1, 'CLOSED')",
                "INSERT INTO tickets (title, project_id, status) VALUES ('B1', 2, 'CLOSED')"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllProjects_returnsProjectsWithStatusCounts() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Project Alpha"))
                .andExpect(jsonPath("$[0].openTickets").value(2))
                .andExpect(jsonPath("$[0].inProgressTickets").value(1))
                .andExpect(jsonPath("$[0].closedTickets").value(1))
                .andExpect(jsonPath("$[1].name").value("Project Beta"))
                .andExpect(jsonPath("$[1].openTickets").value(0))
                .andExpect(jsonPath("$[1].inProgressTickets").value(0))
                .andExpect(jsonPath("$[1].closedTickets").value(1));
    }
}
