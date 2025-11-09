package com.invoiceme.interfaces.rest;

import com.invoiceme.application.reminders.GetReminderHistory.GetReminderHistoryHandler;
import com.invoiceme.application.reminders.GetReminderHistory.GetReminderHistoryQuery;
import com.invoiceme.application.reminders.GetReminderHistory.ReminderEmailDto;
import com.invoiceme.application.reminders.ListOverdueInvoices.ListOverdueInvoicesHandler;
import com.invoiceme.application.reminders.ListOverdueInvoices.ListOverdueInvoicesQuery;
import com.invoiceme.application.reminders.ListOverdueInvoices.OverdueInvoiceDto;
import com.invoiceme.application.reminders.PreviewReminder.PreviewReminderHandler;
import com.invoiceme.application.reminders.PreviewReminder.PreviewReminderQuery;
import com.invoiceme.application.reminders.PreviewReminder.PreviewReminderDto;
import com.invoiceme.application.reminders.SendReminderEmail.SendReminderEmailCommand;
import com.invoiceme.application.reminders.SendReminderEmail.SendReminderEmailHandler;
import com.invoiceme.domain.reminder.ReminderType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Reminder Email management operations (authenticated endpoints).
 * Provides CQRS endpoints for sending reminders, viewing history, and managing overdue invoices.
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final SendReminderEmailHandler sendReminderEmailHandler;
    private final GetReminderHistoryHandler getReminderHistoryHandler;
    private final ListOverdueInvoicesHandler listOverdueInvoicesHandler;
    private final PreviewReminderHandler previewReminderHandler;

    public ReminderController(SendReminderEmailHandler sendReminderEmailHandler,
                             GetReminderHistoryHandler getReminderHistoryHandler,
                             ListOverdueInvoicesHandler listOverdueInvoicesHandler,
                             PreviewReminderHandler previewReminderHandler) {
        this.sendReminderEmailHandler = sendReminderEmailHandler;
        this.getReminderHistoryHandler = getReminderHistoryHandler;
        this.listOverdueInvoicesHandler = listOverdueInvoicesHandler;
        this.previewReminderHandler = previewReminderHandler;
    }

    /**
     * Send a reminder email for an invoice (manual trigger).
     *
     * @param command the send reminder email command
     * @return the reminder ID with 201 Created status
     */
    @PostMapping("/send")
    public ResponseEntity<ReminderSentResponse> sendReminder(@RequestBody SendReminderEmailCommand command) {
        UUID reminderId = sendReminderEmailHandler.handle(command);

        ReminderSentResponse response = new ReminderSentResponse();
        response.setReminderId(reminderId);
        response.setMessage("Reminder email sent successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get reminder history for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of reminder DTOs with 200 OK status
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<ReminderEmailDto>> getReminderHistory(@PathVariable UUID invoiceId) {
        GetReminderHistoryQuery query = new GetReminderHistoryQuery();
        query.setInvoiceId(invoiceId);

        List<ReminderEmailDto> reminders = getReminderHistoryHandler.handle(query);
        return ResponseEntity.ok(reminders);
    }

    /**
     * List all overdue invoices that may need reminders.
     *
     * @return list of overdue invoice DTOs with 200 OK status
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<OverdueInvoiceDto>> listOverdueInvoices() {
        ListOverdueInvoicesQuery query = new ListOverdueInvoicesQuery();
        List<OverdueInvoiceDto> overdueInvoices = listOverdueInvoicesHandler.handle(query);
        return ResponseEntity.ok(overdueInvoices);
    }

    /**
     * Preview a reminder email without sending it.
     *
     * @param invoiceId the invoice ID
     * @param reminderType the reminder type
     * @return the preview DTO with 200 OK status
     */
    @GetMapping("/preview")
    public ResponseEntity<PreviewReminderDto> previewReminder(
            @RequestParam UUID invoiceId,
            @RequestParam ReminderType reminderType) {

        PreviewReminderQuery query = new PreviewReminderQuery();
        query.setInvoiceId(invoiceId);
        query.setReminderType(reminderType);

        PreviewReminderDto preview = previewReminderHandler.handle(query);
        return ResponseEntity.ok(preview);
    }

    /**
     * Response DTO for reminder sent confirmation.
     */
    public static class ReminderSentResponse {
        private UUID reminderId;
        private String message;

        public ReminderSentResponse() {}

        public UUID getReminderId() {
            return reminderId;
        }

        public void setReminderId(UUID reminderId) {
            this.reminderId = reminderId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
