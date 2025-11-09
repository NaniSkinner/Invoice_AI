package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.reminder.ReminderEmail;
import com.invoiceme.domain.reminder.ReminderStatus;
import com.invoiceme.domain.reminder.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ReminderEmail entity.
 * Provides CRUD operations and custom query methods for reminder emails.
 */
@Repository
public interface ReminderEmailRepository extends JpaRepository<ReminderEmail, UUID> {

    /**
     * Finds all reminder emails for a specific invoice, ordered by sent date descending.
     *
     * @param invoiceId the ID of the invoice
     * @return list of reminder emails for the invoice
     */
    List<ReminderEmail> findByInvoiceIdOrderBySentAtDesc(UUID invoiceId);

    /**
     * Finds all reminder emails with a specific status that are scheduled before a given date/time.
     * Useful for finding pending reminders that are due to be sent.
     *
     * @param status the status to filter by
     * @param dateTime the scheduled date/time threshold
     * @return list of reminder emails matching the criteria
     */
    List<ReminderEmail> findByStatusAndScheduledForBefore(ReminderStatus status, LocalDateTime dateTime);

    /**
     * Checks if a reminder email already exists for a given invoice, reminder type, and status.
     * Useful for preventing duplicate reminders.
     *
     * @param invoiceId the ID of the invoice
     * @param type the reminder type
     * @param status the reminder status
     * @return true if such a reminder exists, false otherwise
     */
    boolean existsByInvoiceIdAndReminderTypeAndStatus(UUID invoiceId, ReminderType type, ReminderStatus status);
}
