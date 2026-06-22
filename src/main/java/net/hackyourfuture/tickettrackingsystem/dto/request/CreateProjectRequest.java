package net.hackyourfuture.tickettrackingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank
        @Size(min = 3, max = 255) String name,
        String description
) {
}
