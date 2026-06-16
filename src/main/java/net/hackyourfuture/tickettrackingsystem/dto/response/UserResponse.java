package net.hackyourfuture.tickettrackingsystem.dto.response;

public record UserResponse(
        Long id,
        String name,
        String email
) {
}