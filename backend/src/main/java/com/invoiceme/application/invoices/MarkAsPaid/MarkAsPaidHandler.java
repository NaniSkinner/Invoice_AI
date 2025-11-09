package com.invoiceme.application.invoices.MarkAsPaid;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles the MarkAsPaidCommand.
 * Validates SENT status and transitions invoice to PAID.
 */
@Service
public class MarkAsPaidHandler {

    private final InvoiceRepository invoiceRepository;

    public MarkAsPaidHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Handles marking an invoice as paid.
     *
     * @param command the mark as paid command
     * @return the ID of the paid invoice
     * @throws IllegalArgumentException if invoice not found
     * @throws IllegalStateException if invoice is not in SENT status
     */
    @Transactional
    public UUID handle(MarkAsPaidCommand command) {
        if (command.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        // Get invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + command.getInvoiceId()));

        // Call domain method to mark as paid
        // This validates status and updates amounts
        invoice.markAsPaid();

        // Save changes
        invoiceRepository.save(invoice);

        // TODO: Publish InvoicePaidEvent
        // eventPublisher.publish(new InvoicePaidEvent(invoice.getId()));

        return invoice.getId();
    }
}
