package com.invoiceme.infrastructure.services;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.reminder.ReminderType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Service for generating AI-powered email content for invoice reminders.
 * Currently uses template-based generation with context-aware content.
 *
 * TODO: Integrate with Claude API (Anthropic) or OpenAI API for true AI-generated content
 * TODO: Add personalization based on customer history and payment patterns
 * TODO: Add A/B testing support for different email tones and formats
 * TODO: Add support for multiple languages based on customer preferences
 */
@Service
public class AiContentGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Generates a context-aware reminder email for an invoice.
     *
     * MOCK IMPLEMENTATION: Uses templates with placeholders.
     * In production, this should call Claude/OpenAI API to generate personalized content.
     *
     * @param invoice the invoice to generate a reminder for
     * @param reminderType the type of reminder (before due, overdue, etc.)
     * @param daysOverdue the number of days the invoice is overdue (0 if not overdue)
     * @return the generated email body content
     */
    public String generateReminderEmail(Invoice invoice, ReminderType reminderType, int daysOverdue) {
        String customerName = invoice.getCustomer().getContactName();
        String invoiceNumber = invoice.getInvoiceNumber();
        BigDecimal amountDue = invoice.getBalanceRemaining();
        LocalDate dueDate = invoice.getDueDate();
        String paymentLink = generatePaymentLink(invoice);

        String emailBody;

        switch (reminderType) {
            case BEFORE_DUE:
                emailBody = generateBeforeDueEmail(customerName, invoiceNumber, amountDue, dueDate, paymentLink);
                break;
            case ON_DUE_DATE:
                emailBody = generateOnDueDateEmail(customerName, invoiceNumber, amountDue, dueDate, paymentLink);
                break;
            case OVERDUE_7_DAYS:
                emailBody = generateOverdueEmail(customerName, invoiceNumber, amountDue, dueDate, daysOverdue, paymentLink, false);
                break;
            case OVERDUE_14_DAYS:
                emailBody = generateOverdueEmail(customerName, invoiceNumber, amountDue, dueDate, daysOverdue, paymentLink, false);
                break;
            case OVERDUE_30_DAYS:
                emailBody = generateOverdueEmail(customerName, invoiceNumber, amountDue, dueDate, daysOverdue, paymentLink, true);
                break;
            default:
                emailBody = generateGenericReminder(customerName, invoiceNumber, amountDue, dueDate, paymentLink);
        }

        // TODO: Replace with actual AI API call
        // Example Claude API integration:
        // String prompt = buildPromptForClaude(invoice, reminderType, daysOverdue);
        // String aiGeneratedContent = claudeApiClient.generateCompletion(prompt);
        // return aiGeneratedContent;

        return emailBody;
    }

    private String generateBeforeDueEmail(String customerName, String invoiceNumber,
                                         BigDecimal amountDue, LocalDate dueDate, String paymentLink) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

        return String.format(
            "Dear %s,\n\n" +
            "This is a friendly reminder that invoice #%s for $%.2f is due in %d day%s (%s).\n\n" +
            "To ensure uninterrupted service and avoid any late fees, please process your payment at your earliest convenience.\n\n" +
            "Payment Details:\n" +
            "Invoice Number: %s\n" +
            "Amount Due: $%.2f\n" +
            "Due Date: %s\n\n" +
            "You can make your payment quickly and securely using the link below:\n" +
            "%s\n\n" +
            "If you have already sent your payment, please disregard this reminder. If you have any questions or concerns about this invoice, " +
            "please don't hesitate to contact us.\n\n" +
            "Thank you for your prompt attention to this matter.\n\n" +
            "Best regards,\n" +
            "Accounts Receivable Team",
            customerName, invoiceNumber, amountDue, daysUntilDue, daysUntilDue == 1 ? "" : "s",
            dueDate.format(DATE_FORMATTER), invoiceNumber, amountDue,
            dueDate.format(DATE_FORMATTER), paymentLink
        );
    }

    private String generateOnDueDateEmail(String customerName, String invoiceNumber,
                                         BigDecimal amountDue, LocalDate dueDate, String paymentLink) {
        return String.format(
            "Dear %s,\n\n" +
            "This is a reminder that invoice #%s for $%.2f is due today (%s).\n\n" +
            "To avoid any late fees or service interruptions, please submit your payment today.\n\n" +
            "Payment Details:\n" +
            "Invoice Number: %s\n" +
            "Amount Due: $%.2f\n" +
            "Due Date: %s (TODAY)\n\n" +
            "You can make your payment quickly and securely using the link below:\n" +
            "%s\n\n" +
            "If you have already submitted your payment, thank you! Please disregard this reminder.\n\n" +
            "If you're experiencing any issues or have questions about this invoice, please contact us immediately " +
            "so we can assist you.\n\n" +
            "Thank you for your prompt payment.\n\n" +
            "Best regards,\n" +
            "Accounts Receivable Team",
            customerName, invoiceNumber, amountDue, dueDate.format(DATE_FORMATTER),
            invoiceNumber, amountDue, dueDate.format(DATE_FORMATTER), paymentLink
        );
    }

    private String generateOverdueEmail(String customerName, String invoiceNumber,
                                       BigDecimal amountDue, LocalDate dueDate, int daysOverdue,
                                       String paymentLink, boolean urgent) {
        String urgencyNote = urgent
            ? "\n\nIMPORTANT: This invoice is significantly overdue. Please treat this as an urgent matter. " +
              "Continued non-payment may result in service suspension and additional collection fees."
            : "";

        return String.format(
            "Dear %s,\n\n" +
            "This is an important notice regarding invoice #%s, which is now %d day%s overdue.\n\n" +
            "Payment Details:\n" +
            "Invoice Number: %s\n" +
            "Amount Due: $%.2f\n" +
            "Original Due Date: %s\n" +
            "Days Overdue: %d%s\n\n" +
            "We kindly request your immediate attention to settle this outstanding balance. " +
            "Late fees may apply to overdue invoices.\n\n" +
            "You can make your payment quickly and securely using the link below:\n" +
            "%s\n\n" +
            "If you have already sent your payment, please disregard this reminder and accept our thanks.\n\n" +
            "If you're experiencing financial difficulties or have questions about this invoice, " +
            "please contact us immediately. We're here to work with you and may be able to arrange a payment plan.\n\n" +
            "We value your business and look forward to resolving this matter promptly.\n\n" +
            "Best regards,\n" +
            "Accounts Receivable Team",
            customerName, invoiceNumber, daysOverdue, daysOverdue == 1 ? "" : "s",
            invoiceNumber, amountDue, dueDate.format(DATE_FORMATTER), daysOverdue,
            urgencyNote, paymentLink
        );
    }

    private String generateGenericReminder(String customerName, String invoiceNumber,
                                          BigDecimal amountDue, LocalDate dueDate, String paymentLink) {
        return String.format(
            "Dear %s,\n\n" +
            "This is a reminder regarding invoice #%s.\n\n" +
            "Payment Details:\n" +
            "Invoice Number: %s\n" +
            "Amount Due: $%.2f\n" +
            "Due Date: %s\n\n" +
            "You can make your payment quickly and securely using the link below:\n" +
            "%s\n\n" +
            "If you have any questions about this invoice, please don't hesitate to contact us.\n\n" +
            "Thank you for your business.\n\n" +
            "Best regards,\n" +
            "Accounts Receivable Team",
            customerName, invoiceNumber, invoiceNumber, amountDue,
            dueDate.format(DATE_FORMATTER), paymentLink
        );
    }

    private String generatePaymentLink(Invoice invoice) {
        // In production, this would generate a secure payment portal link
        String baseUrl = "https://pay.invoiceme.com";
        return baseUrl + "/pay/" + invoice.getPaymentLink();
    }
}
