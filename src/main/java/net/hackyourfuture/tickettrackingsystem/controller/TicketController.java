package net.hackyourfuture.tickettrackingsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateTicketRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.UpdateTicketRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.TicketResponse;
import net.hackyourfuture.tickettrackingsystem.dto.response.TicketUpdateResponse;
import net.hackyourfuture.tickettrackingsystem.model.TicketStatus;
import net.hackyourfuture.tickettrackingsystem.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request
    ) {
        TicketResponse response = ticketService.createTicket(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TicketResponse> getAllTickets(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) TicketStatus status
    ) {
        return ticketService.getAllTickets(text, status);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PutMapping("/{id}")
    public TicketUpdateResponse updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.updateTicket(id, request);
    }



    @PostMapping("/{ticketId}/assignees/{userId}")
    public ResponseEntity<TicketResponse> assignUserToTicket(
            @PathVariable Long ticketId,
            @PathVariable Long userId
    ) {
        TicketResponse response =
                ticketService.assignUserToTicket(ticketId, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ticketId}/assignees/{userId}")
    public ResponseEntity<TicketResponse> unassignUserFromTicket(
            @PathVariable Long ticketId,
            @PathVariable Long userId
    ) {
        TicketResponse response =
                ticketService.unassignUserFromTicket(ticketId, userId);

        return ResponseEntity.ok(response);
    }
}