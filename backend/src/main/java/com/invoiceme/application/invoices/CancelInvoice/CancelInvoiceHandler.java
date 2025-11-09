package com.invoiceme.application.invoices.CancelInvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles the CancelInvoiceCommand.
 * Cancels an invoice with a reason.
 */
@Service
public class CancelInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    public CancelInvoiceHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Handles cancelling an invoice.
     *
     * @param command the cancel invoice command
     * @return the ID of the cancelled invoice
     * @throws IllegalArgumentException if invoice not found or reason is missing
     * @throws IllegalStateException if invoice is already cancelled
     */
    @Transactional
    public UUID handle(CancelInvoiceCommand command) {
        if (command.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        if (command.getCancellationReason() == null || command.getCancellationReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        // Get invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + command.getInvoiceId()));

        // Call domain method to cancel invoice
        // This validates that invoice is not already cancelled
        invoice.cancel(command.getCancellationReason());

        // Save changes
        invoiceRepository.save(invoice);

        // TODO: Publish InvoiceCancelledEvent
        // eventPublisher.publish(new InvoiceCancelledEvent(invoice.getId(), command.getCancellationReason()));

        return invoice.getId();
    }
}
