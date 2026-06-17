package net.hackyourfuture.tickettrackingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.hackyourfuture.tickettrackingsystem.model.TicketStatus;

public record CreateTicketRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 5000)
        String description,

        @NotNull
        Long projectId,

        @NotNull
        TicketStatus status
) {
}
