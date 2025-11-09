package com.invoiceme.interfaces.rest;

import com.invoiceme.application.payments.GetPayment.GetPaymentHandler;
import com.invoiceme.application.payments.GetPayment.GetPaymentQuery;
import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.application.payments.GetPaymentsByInvoice.GetPaymentsByInvoiceHandler;
import com.invoiceme.application.payments.GetPaymentsByInvoice.GetPaymentsByInvoiceQuery;
import com.invoiceme.application.payments.ListPayments.ListPaymentsHandler;
import com.invoiceme.application.payments.ListPayments.ListPaymentsQuery;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentCommand;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Payment management operations (authenticated endpoints).
 * Provides CQRS endpoints for payment recording and querying.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RecordPaymentHandler recordPaymentHandler;
    private final GetPaymentHandler getPaymentHandler;
    private final ListPaymentsHandler listPaymentsHandler;
    private final GetPaymentsByInvoiceHandler getPaymentsByInvoiceHandler;

    public PaymentController(RecordPaymentHandler recordPaymentHandler,
                            GetPaymentHandler getPaymentHandler,
                            ListPaymentsHandler listPaymentsHandler,
                            GetPaymentsByInvoiceHandler getPaymentsByInvoiceHandler) {
        this.recordPaymentHandler = recordPaymentHandler;
        this.getPaymentHandler = getPaymentHandler;
        this.listPaymentsHandler = listPaymentsHandler;
        this.getPaymentsByInvoiceHandler = getPaymentsByInvoiceHandler;
    }

    /**
     * Record a new payment against an invoice.
     *
     * @param command the payment recording command
     * @return the created payment DTO with 201 Created status
     */
    @PostMapping
    public ResponseEntity<PaymentDto> recordPayment(@RequestBody RecordPaymentCommand command) {
        UUID paymentId = recordPaymentHandler.handle(command);

        // Fetch the created payment to return complete DTO
        GetPaymentQuery query = new GetPaymentQuery();
        query.setPaymentId(paymentId);
        PaymentDto payment = getPaymentHandler.handle(query);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * Get a payment by ID.
     *
     * @param id the payment ID
     * @return the payment DTO with 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID id) {
        GetPaymentQuery query = new GetPaymentQuery();
        query.setPaymentId(id);
        PaymentDto payment = getPaymentHandler.handle(query);
        return ResponseEntity.ok(payment);
    }

    /**
     * List all payments, optionally filtered by invoice.
     *
     * @param invoiceId optional filter to show only payments for specific invoice
     * @return list of payment DTOs with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<PaymentDto>> listPayments(
            @RequestParam(required = false) UUID invoiceId) {

        ListPaymentsQuery query = new ListPaymentsQuery();
        query.setInvoiceId(invoiceId);

        List<PaymentDto> payments = listPaymentsHandler.handle(query);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all payments for a specific invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of payment DTOs for the invoice with 200 OK status
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByInvoice(@PathVariable UUID invoiceId) {
        GetPaymentsByInvoiceQuery query = new GetPaymentsByInvoiceQuery();
        query.setInvoiceId(invoiceId);

        List<PaymentDto> payments = getPaymentsByInvoiceHandler.handle(query);
        return ResponseEntity.ok(payments);
    }
}
