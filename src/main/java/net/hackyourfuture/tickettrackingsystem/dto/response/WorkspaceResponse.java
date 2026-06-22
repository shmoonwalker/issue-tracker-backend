package net.hackyourfuture.tickettrackingsystem.dto.response;

public record WorkspaceResponse(
        Long id,
        String name,
        Long createdByUserId
) {
}
