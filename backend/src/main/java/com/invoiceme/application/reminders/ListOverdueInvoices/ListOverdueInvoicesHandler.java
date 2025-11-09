package com.invoiceme.application.reminders.ListOverdueInvoices;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the ListOverdueInvoicesQuery.
 * Retrieves all invoices that are overdue with outstanding balances.
 */
@Service
public class ListOverdueInvoicesHandler {

    private final InvoiceRepository invoiceRepository;

    public ListOverdueInvoicesHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Handles retrieving all overdue invoices.
     *
     * @param query the list overdue invoices query
     * @return list of overdue invoice DTOs
     */
    @Transactional(readOnly = true)
    public List<OverdueInvoiceDto> handle(ListOverdueInvoicesQuery query) {
        // Find all invoices with status SENT or OVERDUE
        List<Invoice> allInvoices = invoiceRepository.findAll();

        LocalDate today = LocalDate.now();

        return allInvoices.stream()
            // Filter: only SENT invoices (OVERDUE status not used in current model)
            .filter(invoice -> invoice.getStatus() == InvoiceStatus.SENT)
            // Filter: only invoices with remaining balance
            .filter(invoice -> invoice.getBalanceRemaining() != null &&
                             invoice.getBalanceRemaining().compareTo(BigDecimal.ZERO) > 0)
            // Filter: only invoices where due date is in the past
            .filter(invoice -> invoice.getDueDate().isBefore(today))
            // Map to DTO
            .map(invoice -> mapToDto(invoice, today))
            // Sort by days overdue (most overdue first)
            .sorted((a, b) -> Integer.compare(b.getDaysOverdue(), a.getDaysOverdue()))
            .collect(Collectors.toList());
    }

    /**
     * Maps an Invoice entity to an OverdueInvoiceDto.
     *
     * @param invoice the invoice entity
     * @param today the current date
     * @return the overdue invoice DTO
     */
    private OverdueInvoiceDto mapToDto(Invoice invoice, LocalDate today) {
        int daysOverdue = (int) ChronoUnit.DAYS.between(invoice.getDueDate(), today);

        String customerName = invoice.getCustomer() != null
            ? invoice.getCustomer().getBusinessName()
            : "Unknown Customer";

        return new OverdueInvoiceDto(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            customerName,
            invoice.getDueDate(),
            daysOverdue,
            invoice.getBalanceRemaining(),
            invoice.getLastReminderSentAt()
        );
    }
}
