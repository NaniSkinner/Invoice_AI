package com.invoiceme.application.reminders.GetReminderHistory;

import com.invoiceme.domain.reminder.ReminderEmail;
import com.invoiceme.infrastructure.persistence.ReminderEmailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the GetReminderHistoryQuery.
 * Retrieves all reminder emails for a specific invoice.
 */
@Service
public class GetReminderHistoryHandler {

    private final ReminderEmailRepository reminderEmailRepository;

    public GetReminderHistoryHandler(ReminderEmailRepository reminderEmailRepository) {
        this.reminderEmailRepository = reminderEmailRepository;
    }

    /**
     * Handles retrieving the reminder history for an invoice.
     *
     * @param query the get reminder history query
     * @return list of reminder email DTOs, ordered by sent date descending
     * @throws IllegalArgumentException if invoice ID is null
     */
    @Transactional(readOnly = true)
    public List<ReminderEmailDto> handle(GetReminderHistoryQuery query) {
        if (query.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        List<ReminderEmail> reminders = reminderEmailRepository.findByInvoiceIdOrderBySentAtDesc(
            query.getInvoiceId()
        );

        return reminders.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Maps a ReminderEmail entity to a ReminderEmailDto.
     *
     * @param reminder the reminder email entity
     * @return the reminder email DTO
     */
    private ReminderEmailDto mapToDto(ReminderEmail reminder) {
        return new ReminderEmailDto(
            reminder.getId(),
            reminder.getInvoice().getId(),
            reminder.getRecipientEmail(),
            reminder.getSubject(),
            reminder.getEmailBody(),
            reminder.getReminderType(),
            reminder.getStatus(),
            reminder.getScheduledFor(),
            reminder.getSentAt(),
            reminder.getErrorMessage(),
            reminder.getCreatedAt(),
            reminder.getUpdatedAt()
        );
    }
}
