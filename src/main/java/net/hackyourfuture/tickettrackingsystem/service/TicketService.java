package net.hackyourfuture.tickettrackingsystem.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String SYSTEM_ACTOR = "System";

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ResendAutomationService resendAutomationService;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        if (!projectRepository.existsById(request.projectId())) {
            throw new ResourceNotFoundException(
                    "Project with id " + request.projectId() + " not found"
            );
        }

        Ticket ticket = new Ticket(
                null,
                request.title(),
                request.description(),
                request.projectId(),
                request.status(),
                null,
                null
        );

        Ticket createdTicket = ticketRepository.create(ticket);

        return toTicketResponse(createdTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(String text, TicketStatus status) {
        List<Ticket> tickets = ticketRepository.findAll(text, status);

        if (tickets.isEmpty()) {
            return List.of();
        }

        List<Long> ticketIds = tickets.stream().map(Ticket::id).toList();
        Map<Long, List<Long>> assigneesByTicket =
                ticketRepository.findAssignedUserIdsByTicketIds(ticketIds);

        return tickets.stream()
                .map(ticket -> toTicketResponse(
                        ticket,
                        assigneesByTicket.getOrDefault(ticket.id(), List.of())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket with id " + id + " not found"
                        )
                );

        return toTicketResponse(ticket);
    }

    @Transactional
    public TicketUpdateResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket with id " + id + " not found"
                        )
                );

        if (!projectRepository.existsById(request.projectId())) {
            throw new ResourceNotFoundException(
                    "Project with id " + request.projectId() + " not found"
            );
        }

        Ticket updatedTicket = new Ticket(
                existingTicket.id(),
                request.title(),
                request.description(),
                request.projectId(),
                request.status(),
                existingTicket.creationDate(),
                existingTicket.updateDate()
        );

        Ticket savedTicket = ticketRepository.update(updatedTicket);

        boolean emailNotificationsDispatched =
                dispatchUpdateEmails(existingTicket, savedTicket);

        return new TicketUpdateResponse(
                toTicketResponse(savedTicket),
                emailNotificationsDispatched

        );
    }

    @Transactional
    public TicketResponse assignUserToTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket with id " + ticketId + " not found"
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId + " not found"
                        )
                );

        boolean inserted = ticketRepository.assignUser(ticketId, userId);

        if (!inserted) {
            throw new IllegalArgumentException(
                    "User " + userId + " is already assigned to ticket " + ticketId
            );
        }

        resendAutomationService.sendTicketUpdatedEmail(
                user.email(),
                ticket.id(),
                ticket.title(),
                String.valueOf(ticket.status()),
                SYSTEM_ACTOR,
                "You have been assigned to this ticket."
        );

        return toTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse unassignUserFromTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket with id " + ticketId + " not found"
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " + userId + " not found"
                        )
                );

        boolean removed = ticketRepository.unassignUser(ticketId, userId);

        if (!removed) {
            throw new ResourceNotFoundException(
                    "User " + userId + " is not assigned to ticket " + ticketId
            );
        }

        resendAutomationService.sendTicketUpdatedEmail(
                user.email(),
                ticket.id(),
                ticket.title(),
                String.valueOf(ticket.status()),
                SYSTEM_ACTOR,
                "You have been removed from this ticket."
        );

        return toTicketResponse(ticket);
    }

    private boolean dispatchUpdateEmails(Ticket existingTicket, Ticket savedTicket) {
        List<String> assigneeEmails =
                ticketRepository.findAssigneeEmailsByTicketId(savedTicket.id());

        if (assigneeEmails.isEmpty()) {
            return false;
        }

        String changes = buildChanges(existingTicket, savedTicket);

        for (String email : assigneeEmails) {
            resendAutomationService.sendTicketUpdatedEmail(
                    email,
                    savedTicket.id(),
                    savedTicket.title(),
                    String.valueOf(savedTicket.status()),
                    SYSTEM_ACTOR,
                    changes
            );
        }

        return true;
    }

    private String buildChanges(Ticket oldTicket, Ticket newTicket) {
        StringBuilder changes = new StringBuilder();

        if (!Objects.equals(oldTicket.title(), newTicket.title())) {
            changes.append("Title changed from \"")
                    .append(oldTicket.title())
                    .append("\" to \"")
                    .append(newTicket.title())
                    .append("\"\n");
        }

        if (!Objects.equals(oldTicket.description(), newTicket.description())) {
            changes.append("Description was updated\n");
        }

        if (!Objects.equals(oldTicket.projectId(), newTicket.projectId())) {
            changes.append("Project changed from ")
                    .append(oldTicket.projectId())
                    .append(" to ")
                    .append(newTicket.projectId())
                    .append("\n");
        }

        if (!Objects.equals(oldTicket.status(), newTicket.status())) {
            changes.append("Status changed from ")
                    .append(oldTicket.status())
                    .append(" to ")
                    .append(newTicket.status())
                    .append("\n");
        }

        if (changes.isEmpty()) {
            return "Ticket was updated, but no tracked fields changed.";
        }

        return changes.toString();
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return toTicketResponse(
                ticket,
                ticketRepository.findAssignedUserIds(ticket.id())
        );
    }

    private TicketResponse toTicketResponse(Ticket ticket, List<Long> assignedUserIds) {
        return new TicketResponse(
                ticket.id(),
                ticket.title(),
                ticket.description(),
                ticket.projectId(),
                ticket.status(),
                assignedUserIds,
                ticket.creationDate(),
                ticket.updateDate()
        );
    }
}
