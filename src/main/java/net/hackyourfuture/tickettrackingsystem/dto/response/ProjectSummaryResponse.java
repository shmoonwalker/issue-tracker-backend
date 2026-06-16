package net.hackyourfuture.tickettrackingsystem.dto.response;

public record ProjectSummaryResponse(
        Long id,
        String name,
        int openTickets,
        int inProgressTickets,
        int closedTickets
) {
}