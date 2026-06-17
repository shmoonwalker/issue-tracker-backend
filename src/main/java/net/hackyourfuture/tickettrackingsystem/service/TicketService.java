package net.hackyourfuture.tickettrackingsystem.service;

import lombok.RequiredArgsConstructor;
import net.hackyourfuture.tickettrackingsystem.dto.request.CreateTicketRequest;
import net.hackyourfuture.tickettrackingsystem.dto.request.UpdateTicketRequest;
import net.hackyourfuture.tickettrackingsystem.dto.response.EmailNotificationResponse;
import net.hackyourfuture.tickettrackingsystem.dto.response.TicketResponse;
import net.hackyourfuture.tickettrackingsystem.dto.response.TicketUpdateResponse;
import net.hackyourfuture.tickettrackingsystem.email.EmailSendResult;
import net.hackyourfuture.tickettrackingsystem.email.ResendAutomationService;
import net.hackyourfuture.tickettrackingsystem.exception.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.model.Ticket;
import net.hackyourfuture.tickettrackingsystem.model.User;
import net.hackyourfuture.tickettrackingsystem.repository.ProjectRepository;
import net.hackyourfuture.tickettrackingsystem.repository.TicketRepository;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ResendAutomationService resendAutomationService;

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

    public List<TicketResponse> getAllTickets(String text, String status) {
        return ticketRepository.findAll(text, status)
                .stream()
                .map(this::toTicketResponse)
                .toList();
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket with id " + id + " not found"
                        )
                );

        return toTicketResponse(ticket);
    }

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

        List<EmailNotificationResponse> emailNotifications =
                sendTicketUpdateEmails(existingTicket, savedTicket);

        return new TicketUpdateResponse(
                toTicketResponse(savedTicket),
                emailNotifications
        );
    }

    public EmailNotificationResponse assignUserToTicket(Long ticketId, Long userId) {
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

        if (ticketRepository.assignmentExists(ticketId, userId)) {
            throw new IllegalArgumentException(
                    "User " + userId + " is already assigned to ticket " + ticketId
            );
        }

        ticketRepository.assignUser(ticketId, userId);

        return sendSingleTicketEmail(
                user.email(),
                ticket,
                "System",
                "You have been assigned to this ticket."
        );
    }

    public EmailNotificationResponse unassignUserFromTicket(Long ticketId, Long userId) {
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

        return sendSingleTicketEmail(
                user.email(),
                ticket,
                "System",
                "You have been removed from this ticket."
        );
    }


    private List<EmailNotificationResponse> sendTicketUpdateEmails(
            Ticket existingTicket,
            Ticket savedTicket
    ) {
        List<String> assigneeEmails =
                ticketRepository.findAssigneeEmailsByTicketId(savedTicket.id());

        if (assigneeEmails.isEmpty()) {
            return List.of();
        }

        String changes = buildChanges(existingTicket, savedTicket);

        return assigneeEmails.stream()
                .map(assigneeEmail ->
                        sendSingleTicketEmail(
                                assigneeEmail,
                                savedTicket,
                                "System",
                                changes
                        )
                )
                .toList();
    }

    private EmailNotificationResponse sendSingleTicketEmail(
            String assigneeEmail,
            Ticket ticket,
            String updatedBy,
            String changes
    ) {
        EmailSendResult result = resendAutomationService.sendTicketUpdatedEmail(
                assigneeEmail,
                ticket.id(),
                ticket.title(),
                String.valueOf(ticket.status()),
                updatedBy,
                changes
        );

        return new EmailNotificationResponse(
                result.recipientEmail(),
                result.sent(),
                result.message()
        );
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
        return new TicketResponse(
                ticket.id(),
                ticket.title(),
                ticket.description(),
                ticket.projectId(),
                ticket.status(),
                ticketRepository.findAssignedUserIds(ticket.id()),
                ticket.creationDate(),
                ticket.updateDate()
        );
    }
}