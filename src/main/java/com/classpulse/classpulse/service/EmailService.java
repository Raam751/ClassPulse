package com.classpulse.classpulse.service;

import com.classpulse.classpulse.dto.response.SessionAnalyticsResponse;
import com.classpulse.classpulse.entity.Session;
import com.classpulse.classpulse.entity.User;
import com.classpulse.classpulse.exception.ResourceNotFoundException;
import com.classpulse.classpulse.repository.SessionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SessionRepository sessionRepository;
    private final AnalyticsService analyticsService;

    public EmailService(JavaMailSender mailSender,
            SessionRepository sessionRepository,
            AnalyticsService analyticsService) {
        this.mailSender = mailSender;
        this.sessionRepository = sessionRepository;
        this.analyticsService = analyticsService;
    }

    @Async
    public void sendSessionSummaryEmail(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        User teacher = session.getCreatedBy();
        SessionAnalyticsResponse analytics = analyticsService.getSessionAnalytics(sessionId);

        String subject = "Session Summary: " + session.getTitle();
        String htmlContent = buildSessionSummaryHtml(session, analytics);

        sendHtmlEmail(teacher.getEmail(), subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to ClassPulse!";
        String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Welcome to ClassPulse, %s!</h2>
                    <p>Your account has been created successfully.</p>
                    <p><strong>Role:</strong> %s</p>
                    <p>Start engaging with your classroom today!</p>
                    <br>
                    <p>Best regards,<br>The ClassPulse Team</p>
                </body>
                </html>
                """.formatted(user.getName(), user.getRole().name());

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Log error but don't throw - email shouldn't break the flow
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    private String buildSessionSummaryHtml(Session session, SessionAnalyticsResponse analytics) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Session Summary: %s</h2>
                    <hr>
                    <h3>Overview</h3>
                    <ul>
                        <li><strong>Status:</strong> %s</li>
                        <li><strong>Total Questions:</strong> %d</li>
                        <li><strong>Total Responses:</strong> %d</li>
                        <li><strong>Unique Participants:</strong> %d</li>
                        <li><strong>Avg Responses/Question:</strong> %.2f</li>
                    </ul>
                """.formatted(
                session.getTitle(),
                analytics.getSessionStatus(),
                analytics.getTotalQuestions(),
                analytics.getTotalResponses(),
                analytics.getUniqueParticipants(),
                analytics.getAverageResponsesPerQuestion()));

        if (analytics.getQuestionAnalytics() != null && !analytics.getQuestionAnalytics().isEmpty()) {
            html.append("<h3>Question Breakdown</h3><ul>");
            for (var qa : analytics.getQuestionAnalytics()) {
                html.append("<li><strong>").append(qa.getQuestionText()).append("</strong>");
                html.append(" (").append(qa.getResponseCount()).append(" responses)</li>");
            }
            html.append("</ul>");
        }

        html.append("""
                    <br>
                    <p>Best regards,<br>The ClassPulse Team</p>
                </body>
                </html>
                """);

        return html.toString();
    }
}
