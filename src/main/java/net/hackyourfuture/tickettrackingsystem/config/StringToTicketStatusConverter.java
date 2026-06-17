package net.hackyourfuture.tickettrackingsystem.config;

import net.hackyourfuture.tickettrackingsystem.model.TicketStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToTicketStatusConverter implements Converter<String, TicketStatus> {

    @Override
    public TicketStatus convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }

        return TicketStatus.fromValue(source);
    }
}
