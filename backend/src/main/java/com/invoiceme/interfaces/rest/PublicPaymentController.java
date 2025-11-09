package com.invoiceme.interfaces.rest;

import com.invoiceme.application.payments.GetPayment.GetPaymentHandler;
import com.invoiceme.application.payments.GetPayment.GetPaymentQuery;
import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentCommand;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentHandler;
import com.invoiceme.application.invoices.GetInvoice.GetInvoiceHandler;
import com.invoiceme.application.invoices.GetInvoice.GetInvoiceQuery;
import com.invoiceme.application.invoices.GetInvoice.InvoiceDto;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public REST controller for payment link operations (no authentication required).
 * Allows customers to record payments using the payment link from their invoice.
 */
@RestController
@RequestMapping("/api/public/payments")
public class PublicPaymentController {

    private final RecordPaymentHandler recordPaymentHandler;
    private final GetPaymentHandler getPaymentHandler;
    private final GetInvoiceHandler getInvoiceHandler;
    private final InvoiceRepository invoiceRepository;

    public PublicPaymentController(RecordPaymentHandler recordPaymentHandler,
                                   GetPaymentHandler getPaymentHandler,
                                   GetInvoiceHandler getInvoiceHandler,
                                   InvoiceRepository invoiceRepository) {
        this.recordPaymentHandler = recordPaymentHandler;
        this.getPaymentHandler = getPaymentHandler;
        this.getInvoiceHandler = getInvoiceHandler;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Get invoice details by payment link (public endpoint).
     * Customers can view invoice details using the payment link without authentication.
     *
     * @param paymentLink the payment link UUID from the invoice
     * @return the invoice DTO with 200 OK status
     */
    @GetMapping("/link/{paymentLink}")
    public ResponseEntity<InvoiceDto> getInvoiceByPaymentLink(@PathVariable String paymentLink) {
        // Find invoice by payment link
        UUID invoiceId = invoiceRepository.findByPaymentLink(paymentLink)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment link"))
                .getId();

        GetInvoiceQuery query = new GetInvoiceQuery();
        query.setInvoiceId(invoiceId);
        InvoiceDto invoice = getInvoiceHandler.handle(query);

        return ResponseEntity.ok(invoice);
    }

    /**
     * Record a payment using payment link (public endpoint).
     * Customers can record payments without authentication using the payment link.
     *
     * @param paymentLink the payment link UUID from the invoice
     * @param command the payment recording command
     * @return the created payment DTO with 201 Created status
     */
    @PostMapping("/link/{paymentLink}")
    public ResponseEntity<PaymentDto> recordPaymentByLink(
            @PathVariable String paymentLink,
            @RequestBody RecordPaymentCommand command) {

        // Verify payment link is valid and get invoice ID
        UUID invoiceId = invoiceRepository.findByPaymentLink(paymentLink)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment link"))
                .getId();

        // Override invoice ID from payment link (prevent manipulation)
        command.setInvoiceId(invoiceId);

        UUID paymentId = recordPaymentHandler.handle(command);

        // Fetch the created payment to return complete DTO
        GetPaymentQuery query = new GetPaymentQuery();
        query.setPaymentId(paymentId);
        PaymentDto payment = getPaymentHandler.handle(query);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
