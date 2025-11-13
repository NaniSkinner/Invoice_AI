package com.invoiceme.application.reminders.SendReminderEmail;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validator for SendReminderEmailCommand.
 * Ensures the invoice exists, is in a valid state, and can receive reminders.
 */
@Component
public class SendReminderEmailValidator {

    private final InvoiceRepository invoiceRepository;

    public SendReminderEmailValidator(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Validates the SendReminderEmailCommand.
     *
     * @param command the command to validate
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validate(SendReminderEmailCommand command) {
        List<String> errors = new ArrayList<>();

        // Validate invoiceId is present
        if (command.getInvoiceId() == null) {
            errors.add("Invoice ID is required");
            return errors; // Early return if no invoice ID
        }

        // Validate reminderType is present
        if (command.getReminderType() == null) {
            errors.add("Reminder type is required");
        }

        // Validate invoice exists
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(command.getInvoiceId());
        if (invoiceOpt.isEmpty()) {
            errors.add("Invoice not found with ID: " + command.getInvoiceId());
            return errors; // Early return if invoice doesn't exist
        }

        Invoice invoice = invoiceOpt.orElseThrow(() ->
            new IllegalStateException("Invoice should exist but was not found"));

        // Validate invoice status
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            errors.add("Cannot send reminder for invoice in DRAFT status. Invoice must be sent first.");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            errors.add("Cannot send reminder for cancelled invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            errors.add("Cannot send reminder for fully paid invoice");
        }

        // Validate invoice has remaining balance
        if (invoice.getBalanceRemaining() == null ||
            invoice.getBalanceRemaining().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Cannot send reminder for invoice with no remaining balance");
        }

        // Validate reminders are not suppressed
        if (invoice.isRemindersSuppressed()) {
            errors.add("Reminders are suppressed for this invoice");
        }

        // Validate recipient email if provided
        if (command.getRecipientEmail() != null && !command.getRecipientEmail().isBlank()) {
            if (!isValidEmail(command.getRecipientEmail())) {
                errors.add("Invalid recipient email format: " + command.getRecipientEmail());
            }
        } else {
            // If no override email, validate invoice customer has email
            if (invoice.getCustomer() == null ||
                invoice.getCustomer().getEmail() == null ||
                invoice.getCustomer().getEmail().isBlank()) {
                errors.add("Invoice customer does not have a valid email address");
            }
        }

        return errors;
    }

    /**
     * Simple email validation.
     *
     * @param email the email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        // Simple regex for basic email validation
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
