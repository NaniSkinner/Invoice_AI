package com.invoiceme.application.invoices.ListInvoices;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the ListInvoicesQuery.
 * Fetches invoices and maps to summary DTOs with read-only transaction.
 */
@Service
public class ListInvoicesHandler {

    private final InvoiceRepository invoiceRepository;

    public ListInvoicesHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Handles retrieving a list of invoices.
     * Optionally filtered by status.
     *
     * @param query the list invoices query
     * @return list of invoice summary DTOs
     */
    @Transactional(readOnly = true)
    public List<InvoiceSummaryDto> handle(ListInvoicesQuery query) {
        List<Invoice> invoices;

        // Filter by status if provided
        if (query.getStatus() != null) {
            invoices = invoiceRepository.findByStatus(query.getStatus());
        } else {
            invoices = invoiceRepository.findAll();
        }

        // Map to summary DTOs
        return invoices.stream()
            .map(this::mapToSummaryDto)
            .collect(Collectors.toList());
    }

    /**
     * Maps Invoice entity to InvoiceSummaryDto.
     *
     * @param invoice the invoice entity
     * @return the invoice summary DTO
     */
    private InvoiceSummaryDto mapToSummaryDto(Invoice invoice) {
        return new InvoiceSummaryDto(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            invoice.getCustomer().getId(),
            invoice.getCustomer().getBusinessName(),
            invoice.getIssueDate(),
            invoice.getDueDate(),
            invoice.getStatus(),
            invoice.getTotalAmount(),
            invoice.getBalanceRemaining(),
            invoice.getCreatedAt(),
            invoice.getSentAt(),
            invoice.isOverdue()
        );
    }
}
