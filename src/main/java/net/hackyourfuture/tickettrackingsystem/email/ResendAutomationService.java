package net.hackyourfuture.tickettrackingsystem.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class ResendAutomationService {

    private static final Logger logger =
            LoggerFactory.getLogger(ResendAutomationService.class);

    private static final String RESEND_EMAIL_URL = "https://api.resend.com/emails";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String fromEmail;

    public ResendAutomationService(
            HttpClient resendHttpClient,
            ObjectMapper objectMapper,
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail
    ) {
        this.httpClient = resendHttpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    @Async
    public void sendTicketUpdatedEmail(
            String assigneeEmail,
            Long ticketId,
            String ticketTitle,
            String ticketStatus,
            String updatedBy,
            String changes
    ) {
        try {
            String subject = "Ticket #" + ticketId + " updated";

            String html = """
                    <h2>Ticket updated</h2>

                    <p><strong>Ticket ID:</strong> %s</p>
                    <p><strong>Title:</strong> %s</p>
                    <p><strong>Status:</strong> %s</p>
                    <p><strong>Updated by:</strong> %s</p>
                    <p><strong>Changes:</strong></p>
                    <pre>%s</pre>
                    """.formatted(
                    ticketId,
                    escapeHtml(ticketTitle),
                    escapeHtml(ticketStatus),
                    escapeHtml(updatedBy),
                    escapeHtml(changes)
            );

            Map<String, Object> requestBody = Map.of(
                    "from", fromEmail,
                    "to", assigneeEmail,
                    "subject", subject,
                    "html", html
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_EMAIL_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warn(
                        "Email sending failed for {}. Resend returned status {}. Body: {}",
                        assigneeEmail,
                        response.statusCode(),
                        response.body()
                );
                return;
            }

            logger.info("Email sent successfully to {}", assigneeEmail);

        } catch (Exception exception) {
            logger.error("Email sending crashed for {}", assigneeEmail, exception);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
