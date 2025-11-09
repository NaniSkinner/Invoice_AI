package com.invoiceme.infrastructure.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails.
 * Currently a mock implementation that logs to console.
 *
 * TODO: Implement real SMTP integration using JavaMailSender or SendGrid/Amazon SES API
 * TODO: Add email template rendering support
 * TODO: Add retry logic for failed sends
 * TODO: Add email delivery tracking
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Sends an email to the specified recipient.
     *
     * MOCK IMPLEMENTATION: Currently only logs the email details to console.
     * In production, this should integrate with an SMTP server or email service provider.
     *
     * @param to the recipient email address
     * @param subject the email subject
     * @param body the email body (plain text or HTML)
     * @throws RuntimeException if email sending fails
     */
    public void sendEmail(String to, String subject, String body) {
        logger.info("=".repeat(80));
        logger.info("MOCK EMAIL SERVICE - Email would be sent:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Body:");
        logger.info("{}", body);
        logger.info("=".repeat(80));

        // TODO: Replace with actual SMTP implementation
        // Example using Spring's JavaMailSender:
        // MimeMessage message = javaMailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        // helper.setTo(to);
        // helper.setSubject(subject);
        // helper.setText(body, true); // true = HTML
        // javaMailSender.send(message);

        // Simulate potential email failure (uncomment to test error handling)
        // if (Math.random() < 0.1) {
        //     throw new RuntimeException("Simulated email delivery failure");
        // }
    }
}
