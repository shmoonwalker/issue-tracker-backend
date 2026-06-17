package net.hackyourfuture.tickettrackingsystem.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum TicketStatus {
    OPEN("open"),
    IN_PROGRESS("in progress"),
    CLOSED("closed");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TicketStatus fromValue(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.trim().toLowerCase();
        String enumStyle = normalized
                .replace("-", "_")
                .replace(" ", "_");

        return Arrays.stream(values())
                .filter(status ->
                        status.value.equals(normalized)
                                || status.name().equalsIgnoreCase(enumStyle)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown status: \"" + input + "\". Allowed: open, in progress, closed"
                ));
    }
}