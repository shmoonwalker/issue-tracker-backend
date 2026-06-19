package net.hackyourfuture.tickettrackingsystem.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum TicketPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TicketPriority fromValue(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.trim().toLowerCase();
        String enumStyle = normalized
                .replace("-", "_")
                .replace(" ", "_");

        return Arrays.stream(values())
                .filter(priority ->
                        priority.value.equals(normalized)
                                || priority.name().equalsIgnoreCase(enumStyle)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown priority: \"" + input + "\". Allowed: low, medium, high, urgent"
                ));
    }
}