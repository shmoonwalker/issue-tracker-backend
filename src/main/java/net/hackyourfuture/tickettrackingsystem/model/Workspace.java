package net.hackyourfuture.tickettrackingsystem.model;

public record Workspace(
        Long id,
        String name,
        Long createdByUserId
) {
}
