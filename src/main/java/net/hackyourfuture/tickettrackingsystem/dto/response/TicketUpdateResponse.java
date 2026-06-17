package net.hackyourfuture.tickettrackingsystem.dto.response;

import java.util.List;

public record TicketUpdateResponse(
        TicketResponse ticket,
        List<EmailNotificationResponse> emailNotifications
) {
}