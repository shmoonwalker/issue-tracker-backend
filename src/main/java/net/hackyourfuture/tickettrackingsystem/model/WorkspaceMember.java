package net.hackyourfuture.tickettrackingsystem.model;

public record WorkspaceMember(
        Long workspaceId,
        Long userId,
        MemberRole role,
        Long addedByUserId
) {
}