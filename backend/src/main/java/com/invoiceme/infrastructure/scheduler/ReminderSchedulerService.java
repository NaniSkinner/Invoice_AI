package com.invoiceme.infrastructure.scheduler;

import com.invoiceme.application.reminders.SendReminderEmail.SendReminderEmailCommand;
import com.invoiceme.application.reminders.SendReminderEmail.SendReminderEmailHandler;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.reminder.ReminderType;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.ReminderEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled service that automatically sends reminder emails for overdue and upcoming invoices.
 * Runs daily to check for invoices that need reminders.
 */
@Service
public class ReminderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final InvoiceRepository invoiceRepository;
    private final ReminderEmailRepository reminderEmailRepository;
    private final SendReminderEmailHandler sendReminderEmailHandler;

    public ReminderSchedulerService(InvoiceRepository invoiceRepository,
                                   ReminderEmailRepository reminderEmailRepository,
                                   SendReminderEmailHandler sendReminderEmailHandler) {
        this.invoiceRepository = invoiceRepository;
        this.reminderEmailRepository = reminderEmailRepository;
        this.sendReminderEmailHandler = sendReminderEmailHandler;
    }

    /**
     * Scheduled job that runs daily at 9:00 AM to check for invoices needing reminders.
     * Cron expression: "0 0 9 * * ?" = At 09:00:00 AM every day
     *
     * For testing, you can change this to run every minute: "0 * * * * ?"
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendAutomaticReminders() {
        logger.info("Starting automatic reminder job...");

        try {
            // Find all sent invoices with outstanding balance
            List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.SENT)
                .filter(inv -> inv.getBalanceRemaining().compareTo(BigDecimal.ZERO) > 0)
                .toList();

            logger.info("Found {} invoices to check for reminders", invoices.size());

            int remindersSent = 0;

            for (Invoice invoice : invoices) {
                LocalDate today = LocalDate.now();
                LocalDate dueDate = invoice.getDueDate();
                long daysDifference = ChronoUnit.DAYS.between(today, dueDate);

                // Determine which reminder type to send
                ReminderType reminderType = determineReminderType(daysDifference);

                if (reminderType != null) {
                    // Check if reminder already sent for this invoice + type
                    boolean alreadySent = reminderEmailRepository.existsByInvoiceIdAndReminderTypeAndStatus(
                        invoice.getId(),
                        reminderType,
                        com.invoiceme.domain.reminder.ReminderStatus.SENT
                    );

                    if (!alreadySent) {
                        try {
                            // Send reminder
                            SendReminderEmailCommand command = new SendReminderEmailCommand();
                            command.setInvoiceId(invoice.getId());
                            command.setReminderType(reminderType);
                            // recipientEmail is optional - handler will use customer email

                            UUID reminderId = sendReminderEmailHandler.handle(command);
                            logger.info("Sent {} reminder for invoice {} (ID: {})",
                                reminderType, invoice.getInvoiceNumber(), reminderId);
                            remindersSent++;

                        } catch (Exception e) {
                            logger.error("Failed to send reminder for invoice {}: {}",
                                invoice.getInvoiceNumber(), e.getMessage());
                        }
                    }
                }
            }

            logger.info("Automatic reminder job completed. Sent {} reminders.", remindersSent);

        } catch (Exception e) {
            logger.error("Error in automatic reminder job: {}", e.getMessage(), e);
        }
    }

    /**
     * Determines the appropriate reminder type based on days until/past due date.
     *
     * @param daysDifference positive if before due date, negative if after due date
     * @return the reminder type, or null if no reminder should be sent
     */
    private ReminderType determineReminderType(long daysDifference) {
        if (daysDifference == 7) {
            return ReminderType.BEFORE_DUE;  // 7 days before due date
        } else if (daysDifference == 0) {
            return ReminderType.ON_DUE_DATE;  // On the due date
        } else if (daysDifference == -7) {
            return ReminderType.OVERDUE_7_DAYS;  // 7 days overdue
        } else if (daysDifference == -14) {
            return ReminderType.OVERDUE_14_DAYS;  // 14 days overdue
        } else if (daysDifference == -30) {
            return ReminderType.OVERDUE_30_DAYS;  // 30 days overdue
        }

        return null;  // No reminder for this day count
    }
}
