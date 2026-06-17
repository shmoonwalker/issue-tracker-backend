package net.hackyourfuture.tickettrackingsystem.service;

import net.hackyourfuture.tickettrackingsystem.dto.request.CreateTicketRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.UpdateTicketRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.TicketResponse;
import net.hackyourfuture.tickettrackingsystem.dto.response.TicketUpdateResponse;
import net.hackyourfuture.tickettrackingsystem.email.ResendAutomationService;
import net.hackyourfuture.tickettrackingsystem.exception.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.model.Ticket;
import net.hackyourfuture.tickettrackingsystem.model.TicketStatus;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import net.hackyourfuture.tickettrackingsystem.repository.TicketRepository;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResendAutomationService resendAutomationService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicket_whenProjectDoesNotExist_throwsResourceNotFoundException() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Bug login",
                "Login button is broken",
                99L,
                TicketStatus.OPEN
        );

        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.createTicket(request)
        );

        verify(ticketRepository, never()).create(any());
    }

    @Test
    void updateTicket_dispatchesEmailToEveryAssigneeAndFlagsResponse() {
        Ticket existingTicket = new Ticket(
                1L,
                "Bug login",
                "Login button is broken",
                1L,
                TicketStatus.OPEN,
                LocalDateTime.now().minusDays(1),
                null
        );

        Ticket savedTicket = new Ticket(
                1L,
                "Bug login fixed",
                "Login button is fixed",
                1L,
                TicketStatus.IN_PROGRESS,
                existingTicket.creationDate(),
                LocalDateTime.now()
        );

        UpdateTicketRequest request = new UpdateTicketRequest(
                "Bug login fixed",
                "Login button is fixed",
                1L,
                TicketStatus.IN_PROGRESS
        );

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existingTicket));
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(ticketRepository.update(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.findAssigneeEmailsByTicketId(1L))
                .thenReturn(List.of("alice@example.com", "bob@example.com"));
        when(ticketRepository.findAssignedUserIds(1L))
                .thenReturn(List.of(2L, 3L));

        TicketUpdateResponse response = ticketService.updateTicket(1L, request);

        assertEquals("Bug login fixed", response.ticket().title());
        assertEquals(TicketStatus.IN_PROGRESS, response.ticket().status());
        assertTrue(response.emailNotificationsDispatched());

        verify(resendAutomationService, times(2)).sendTicketUpdatedEmail(
                anyString(),
                eq(1L),
                anyString(),
                anyString(),
                eq("System"),
                anyString()
        );
    }

    @Test
    void updateTicket_withNoAssignees_doesNotDispatchAnyEmail() {
        Ticket existingTicket = new Ticket(
                1L,
                "Bug login",
                "Login button is broken",
                1L,
                TicketStatus.OPEN,
                LocalDateTime.now().minusDays(1),
                null
        );

        Ticket savedTicket = new Ticket(
                1L,
                "Bug login fixed",
                "Login button is broken",
                1L,
                TicketStatus.IN_PROGRESS,
                existingTicket.creationDate(),
                LocalDateTime.now()
        );

        UpdateTicketRequest request = new UpdateTicketRequest(
                "Bug login fixed",
                "Login button is broken",
                1L,
                TicketStatus.IN_PROGRESS
        );

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existingTicket));
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(ticketRepository.update(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.findAssigneeEmailsByTicketId(1L)).thenReturn(List.of());
        when(ticketRepository.findAssignedUserIds(1L)).thenReturn(List.of());

        TicketUpdateResponse response = ticketService.updateTicket(1L, request);

        assertFalse(response.emailNotificationsDispatched());
        verify(resendAutomationService, never()).sendTicketUpdatedEmail(
                anyString(), anyLong(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void assignUserToTicket_whenAssignmentAlreadyExists_throwsAndDoesNotEmail() {
        Ticket ticket = new Ticket(
                1L,
                "Bug login",
                "Login button is broken",
                1L,
                TicketStatus.OPEN,
                LocalDateTime.now().minusDays(1),
                null
        );

        User user = new User(2L, "Alice Doe", "alice@example.com");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(ticketRepository.assignUser(1L, 2L)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.assignUserToTicket(1L, 2L)
        );

        verify(resendAutomationService, never()).sendTicketUpdatedEmail(
                anyString(), anyLong(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void assignUserToTicket_whenInsertSucceeds_dispatchesEmailAndReturnsTicket() {
        Ticket ticket = new Ticket(
                1L,
                "Bug login",
                "Login button is broken",
                1L,
                TicketStatus.OPEN,
                LocalDateTime.now().minusDays(1),
                null
        );

        User user = new User(2L, "Alice Doe", "alice@example.com");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(ticketRepository.assignUser(1L, 2L)).thenReturn(true);
        when(ticketRepository.findAssignedUserIds(1L)).thenReturn(List.of(2L));

        TicketResponse response = ticketService.assignUserToTicket(1L, 2L);

        assertEquals(1L, response.id());
        assertTrue(response.assignedUserIds().contains(2L));

        verify(resendAutomationService).sendTicketUpdatedEmail(
                eq("alice@example.com"),
                eq(1L),
                anyString(),
                anyString(),
                eq("System"),
                anyString()
        );
    }
}
