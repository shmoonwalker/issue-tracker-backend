package net.hackyourfuture.tickettrackingsystem.dto.response;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
