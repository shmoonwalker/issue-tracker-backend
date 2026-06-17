package net.hackyourfuture.tickettrackingsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank
        @Size(min = 3, max = 255)
        String name,

        @NotBlank
        @Email
        @Size(max = 255)
        String email

) {
}
