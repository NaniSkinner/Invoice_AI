package com.invoiceme.application.reminders.SendReminderEmail;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.reminder.ReminderEmail;
import com.invoiceme.domain.reminder.ReminderStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.ReminderEmailRepository;
import com.invoiceme.infrastructure.services.AiContentGenerationService;
import com.invoiceme.infrastructure.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Handles the SendReminderEmailCommand.
 * Generates AI-powered email content, sends the email, and tracks the reminder.
 */
@Service
public class SendReminderEmailHandler {

    private static final Logger logger = LoggerFactory.getLogger(SendReminderEmailHandler.class);

    private final InvoiceRepository invoiceRepository;
    private final ReminderEmailRepository reminderEmailRepository;
    private final SendReminderEmailValidator validator;
    private final AiContentGenerationService aiContentGenerationService;
    private final EmailService emailService;

    public SendReminderEmailHandler(InvoiceRepository invoiceRepository,
                                   ReminderEmailRepository reminderEmailRepository,
                                   SendReminderEmailValidator validator,
                                   AiContentGenerationService aiContentGenerationService,
                                   EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.reminderEmailRepository = reminderEmailRepository;
        this.validator = validator;
        this.aiContentGenerationService = aiContentGenerationService;
        this.emailService = emailService;
    }

    /**
     * Handles sending a reminder email for an invoice.
     *
     * @param command the send reminder email command
     * @return the ID of the created reminder email record
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public UUID handle(SendReminderEmailCommand command) {
        // Validate command
        List<String> errors = validator.validate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }

        // Check if reminder already sent for this invoice and type
        boolean alreadySent = reminderEmailRepository.existsByInvoiceIdAndReminderTypeAndStatus(
            command.getInvoiceId(),
            command.getReminderType(),
            ReminderStatus.SENT
        );

        if (alreadySent) {
            logger.warn("Reminder of type {} already sent for invoice {}. Skipping duplicate.",
                       command.getReminderType(), command.getInvoiceId());
            throw new IllegalStateException(
                "A reminder of type " + command.getReminderType() +
                " has already been sent for this invoice"
            );
        }

        // Fetch invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Calculate days overdue
        int daysOverdue = 0;
        if (invoice.getDueDate().isBefore(LocalDate.now())) {
            daysOverdue = (int) ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
        }

        // Generate email content using AI service
        String emailBody = aiContentGenerationService.generateReminderEmail(
            invoice,
            command.getReminderType(),
            daysOverdue
        );

        // Determine recipient email
        String recipientEmail = command.getRecipientEmail() != null && !command.getRecipientEmail().isBlank()
            ? command.getRecipientEmail()
            : invoice.getCustomer().getEmail();

        // Generate subject line
        String subject = generateSubject(invoice.getInvoiceNumber(), command.getReminderType(), daysOverdue);

        // Create reminder email entity
        ReminderEmail reminder = new ReminderEmail();
        reminder.setInvoice(invoice);
        reminder.setRecipientEmail(recipientEmail);
        reminder.setSubject(subject);
        reminder.setEmailBody(emailBody);
        reminder.setReminderType(command.getReminderType());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setScheduledFor(LocalDateTime.now());

        // Save reminder (status = PENDING)
        reminder = reminderEmailRepository.save(reminder);

        // Attempt to send email
        try {
            emailService.sendEmail(recipientEmail, subject, emailBody);

            // Mark as sent
            reminder.markAsSent(LocalDateTime.now());
            reminderEmailRepository.save(reminder);

            // Update invoice lastReminderSentAt
            invoice.setLastReminderSentAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            logger.info("Reminder email sent successfully for invoice {} (reminder ID: {})",
                       invoice.getInvoiceNumber(), reminder.getId());

        } catch (Exception e) {
            // Mark as failed
            reminder.markAsFailed(e.getMessage());
            reminderEmailRepository.save(reminder);

            logger.error("Failed to send reminder email for invoice {}: {}",
                        invoice.getInvoiceNumber(), e.getMessage(), e);

            throw new RuntimeException("Failed to send reminder email: " + e.getMessage(), e);
        }

        return reminder.getId();
    }

    /**
     * Generates an appropriate subject line based on reminder type.
     *
     * @param invoiceNumber the invoice number
     * @param reminderType the type of reminder
     * @param daysOverdue the number of days overdue
     * @return the email subject line
     */
    private String generateSubject(String invoiceNumber, com.invoiceme.domain.reminder.ReminderType reminderType, int daysOverdue) {
        switch (reminderType) {
            case BEFORE_DUE:
                return String.format("Reminder: Invoice #%s Due Soon", invoiceNumber);
            case ON_DUE_DATE:
                return String.format("Payment Due Today: Invoice #%s", invoiceNumber);
            case OVERDUE_7_DAYS:
            case OVERDUE_14_DAYS:
                return String.format("Payment Overdue: Invoice #%s (%d days)", invoiceNumber, daysOverdue);
            case OVERDUE_30_DAYS:
                return String.format("URGENT: Invoice #%s Overdue (%d days)", invoiceNumber, daysOverdue);
            default:
                return String.format("Invoice Reminder: #%s", invoiceNumber);
        }
    }
}
