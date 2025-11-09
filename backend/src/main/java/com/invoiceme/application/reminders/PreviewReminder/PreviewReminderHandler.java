package com.invoiceme.application.reminders.PreviewReminder;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.services.AiContentGenerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Handles the PreviewReminderQuery.
 * Generates a preview of what a reminder email would look like without actually sending it.
 */
@Service
public class PreviewReminderHandler {

    private final InvoiceRepository invoiceRepository;
    private final AiContentGenerationService aiContentGenerationService;

    public PreviewReminderHandler(InvoiceRepository invoiceRepository,
                                 AiContentGenerationService aiContentGenerationService) {
        this.invoiceRepository = invoiceRepository;
        this.aiContentGenerationService = aiContentGenerationService;
    }

    /**
     * Handles previewing a reminder email.
     *
     * @param query the preview reminder query
     * @return the preview reminder DTO with subject, body, and recipient
     * @throws IllegalArgumentException if validation fails or invoice not found
     */
    @Transactional(readOnly = true)
    public PreviewReminderDto handle(PreviewReminderQuery query) {
        // Validate query
        if (query.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        if (query.getReminderType() == null) {
            throw new IllegalArgumentException("Reminder type is required");
        }

        // Fetch invoice
        Invoice invoice = invoiceRepository.findById(query.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + query.getInvoiceId()));

        // Calculate days overdue
        int daysOverdue = 0;
        if (invoice.getDueDate().isBefore(LocalDate.now())) {
            daysOverdue = (int) ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
        }

        // Generate email content using AI service (without saving)
        String emailBody = aiContentGenerationService.generateReminderEmail(
            invoice,
            query.getReminderType(),
            daysOverdue
        );

        // Generate subject line
        String subject = generateSubject(invoice.getInvoiceNumber(), query.getReminderType(), daysOverdue);

        // Determine recipient email
        String recipientEmail = invoice.getCustomer() != null && invoice.getCustomer().getEmail() != null
            ? invoice.getCustomer().getEmail()
            : "No email address available";

        // Return preview DTO
        return new PreviewReminderDto(subject, emailBody, recipientEmail);
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
