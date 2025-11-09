package com.invoiceme.interfaces.rest;

import com.invoiceme.application.invoices.CancelInvoice.CancelInvoiceCommand;
import com.invoiceme.application.invoices.CancelInvoice.CancelInvoiceHandler;
import com.invoiceme.application.invoices.CreateInvoice.CreateInvoiceCommand;
import com.invoiceme.application.invoices.CreateInvoice.CreateInvoiceHandler;
import com.invoiceme.application.invoices.GetInvoice.GetInvoiceHandler;
import com.invoiceme.application.invoices.GetInvoice.GetInvoiceQuery;
import com.invoiceme.application.invoices.GetInvoice.InvoiceDto;
import com.invoiceme.application.invoices.ListInvoices.InvoiceSummaryDto;
import com.invoiceme.application.invoices.ListInvoices.ListInvoicesHandler;
import com.invoiceme.application.invoices.ListInvoices.ListInvoicesQuery;
import com.invoiceme.application.invoices.MarkAsPaid.MarkAsPaidCommand;
import com.invoiceme.application.invoices.MarkAsPaid.MarkAsPaidHandler;
import com.invoiceme.application.invoices.SendInvoice.SendInvoiceCommand;
import com.invoiceme.application.invoices.SendInvoice.SendInvoiceHandler;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Invoice management operations.
 * Provides CQRS endpoints for invoice lifecycle management with state machine.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final CreateInvoiceHandler createInvoiceHandler;
    private final SendInvoiceHandler sendInvoiceHandler;
    private final CancelInvoiceHandler cancelInvoiceHandler;
    private final MarkAsPaidHandler markAsPaidHandler;
    private final GetInvoiceHandler getInvoiceHandler;
    private final ListInvoicesHandler listInvoicesHandler;

    public InvoiceController(CreateInvoiceHandler createInvoiceHandler,
                            SendInvoiceHandler sendInvoiceHandler,
                            CancelInvoiceHandler cancelInvoiceHandler,
                            MarkAsPaidHandler markAsPaidHandler,
                            GetInvoiceHandler getInvoiceHandler,
                            ListInvoicesHandler listInvoicesHandler) {
        this.createInvoiceHandler = createInvoiceHandler;
        this.sendInvoiceHandler = sendInvoiceHandler;
        this.cancelInvoiceHandler = cancelInvoiceHandler;
        this.markAsPaidHandler = markAsPaidHandler;
        this.getInvoiceHandler = getInvoiceHandler;
        this.listInvoicesHandler = listInvoicesHandler;
    }

    /**
     * Create a new invoice in DRAFT status.
     *
     * @param command the invoice creation command
     * @return the created invoice DTO with 201 Created status
     */
    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@RequestBody CreateInvoiceCommand command) {
        UUID invoiceId = createInvoiceHandler.handle(command);

        // Fetch the created invoice to return complete DTO
        GetInvoiceQuery query = new GetInvoiceQuery();
        query.setInvoiceId(invoiceId);
        InvoiceDto invoice = getInvoiceHandler.handle(query);

        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    /**
     * Get an invoice by ID.
     *
     * @param id the invoice ID
     * @return the invoice DTO with 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable UUID id) {
        GetInvoiceQuery query = new GetInvoiceQuery();
        query.setInvoiceId(id);
        InvoiceDto invoice = getInvoiceHandler.handle(query);
        return ResponseEntity.ok(invoice);
    }

    /**
     * List invoices with optional status filter.
     *
     * @param status optional filter to show only invoices with specific status
     * @return list of invoice summary DTOs with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<InvoiceSummaryDto>> listInvoices(
            @RequestParam(required = false) InvoiceStatus status) {

        ListInvoicesQuery query = new ListInvoicesQuery();
        query.setStatus(status);

        List<InvoiceSummaryDto> invoices = listInvoicesHandler.handle(query);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Send an invoice (transition from DRAFT to SENT).
     *
     * @param id the invoice ID
     * @return the updated invoice DTO with 200 OK status
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<InvoiceDto> sendInvoice(@PathVariable UUID id) {
        SendInvoiceCommand command = new SendInvoiceCommand();
        command.setInvoiceId(id);
        UUID invoiceId = sendInvoiceHandler.handle(command);

        // Fetch the updated invoice to return complete DTO
        GetInvoiceQuery query = new GetInvoiceQuery();
        query.setInvoiceId(invoiceId);
        InvoiceDto invoice = getInvoiceHandler.handle(query);

        return ResponseEntity.ok(invoice);
    }

    /**
     * Cancel an invoice with a cancellation reason.
     *
     * @param id the invoice ID
     * @param command the cancel command with cancellation reason
     * @return the updated invoice DTO with 200 OK status
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<InvoiceDto> cancelInvoice(
            @PathVariable UUID id,
            @RequestBody CancelInvoiceCommand command) {

        command.setInvoiceId(id);
        UUID invoiceId = cancelInvoiceHandler.handle(command);

        // Fetch the updated invoice to return complete DTO
        GetInvoiceQuery query = new GetInvoiceQuery();
        query.setInvoiceId(invoiceId);
        InvoiceDto invoice = getInvoiceHandler.handle(query);

        return ResponseEntity.ok(invoice);
    }

    /**
     * Mark an invoice as paid (transition from SENT to PAID).
     *
     * @param id the invoice ID
     * @return the updated invoice DTO with 200 OK status
     */
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<InvoiceDto> markAsPaid(@PathVariable UUID id) {
        MarkAsPaidCommand command = new MarkAsPaidCommand();
        command.setInvoiceId(id);
        UUID invoiceId = markAsPaidHandler.handle(command);

        // Fetch the updated invoice to return complete DTO
        GetInvoiceQuery query = new GetInvoiceQuery();
        query.setInvoiceId(invoiceId);
        InvoiceDto invoice = getInvoiceHandler.handle(query);

        return ResponseEntity.ok(invoice);
    }
}
