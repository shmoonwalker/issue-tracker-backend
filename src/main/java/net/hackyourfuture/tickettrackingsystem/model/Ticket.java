package net.hackyourfuture.tickettrackingsystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Ticket(
        Long id,
        Long projectId,
        Long createdByUserId,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime archivedAt
) {
}