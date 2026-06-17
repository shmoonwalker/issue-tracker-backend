package net.hackyourfuture.tickettrackingsystem.dto.response;

public record TicketUpdateResponse(
        TicketResponse ticket,
        boolean emailNotificationsDispatched
) {
}