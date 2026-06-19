package net.hackyourfuture.tickettrackingsystem.model;

import java.time.LocalDateTime;

public record TicketAssignee(
        Long ticketId,
        Long userId,
        LocalDateTime assignedAt,
        Long assignedByUserId
) {
}