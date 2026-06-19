package net.hackyourfuture.tickettrackingsystem.model;

import java.time.LocalDateTime;

public record TicketComment(
        Long id,
        Long ticketId,
        Long userId,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}