package net.hackyourfuture.tickettrackingsystem.dto.response;

public record ProjectResponse(
        Long id,
        Long workspaceId,
        String name,
        String description,
        Long createdByUserId
) {
}
