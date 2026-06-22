package net.hackyourfuture.tickettrackingsystem.model;

public record Project(
        Long id,
        Long workspaceId,
        String name,
        String description,
        Long createdByUserId
) {
}