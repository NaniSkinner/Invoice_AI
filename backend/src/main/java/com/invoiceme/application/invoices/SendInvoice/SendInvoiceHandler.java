package com.invoiceme.application.invoices.SendInvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles the SendInvoiceCommand.
 * Validates DRAFT status and transitions invoice to SENT.
 */
@Service
public class SendInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    public SendInvoiceHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Handles sending an invoice.
     *
     * @param command the send invoice command
     * @return the ID of the sent invoice
     * @throws IllegalArgumentException if invoice not found
     * @throws IllegalStateException if invoice is not in DRAFT status
     */
    @Transactional
    public UUID handle(SendInvoiceCommand command) {
        if (command.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        // Get invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + command.getInvoiceId()));

        // Call domain method to send invoice
        // This validates status and generates payment link
        invoice.send();

        // Save changes
        invoiceRepository.save(invoice);

        // TODO: Publish InvoiceSentEvent
        // eventPublisher.publish(new InvoiceSentEvent(invoice.getId(), invoice.getPaymentLink()));

        return invoice.getId();
    }
}
