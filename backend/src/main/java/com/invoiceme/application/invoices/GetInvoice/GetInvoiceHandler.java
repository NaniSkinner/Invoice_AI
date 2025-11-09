package com.invoiceme.application.invoices.GetInvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the GetInvoiceQuery.
 * Fetches invoice and maps to DTO with read-only transaction.
 */
@Service
public class GetInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    public GetInvoiceHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Handles retrieving an invoice by ID.
     *
     * @param query the get invoice query
     * @return the invoice DTO
     * @throws IllegalArgumentException if invoice not found
     */
    @Transactional(readOnly = true)
    public InvoiceDto handle(GetInvoiceQuery query) {
        if (query.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        // Get invoice
        Invoice invoice = invoiceRepository.findById(query.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + query.getInvoiceId()));

        // Map to DTO
        return mapToDto(invoice);
    }

    /**
     * Maps Invoice entity to InvoiceDto.
     *
     * @param invoice the invoice entity
     * @return the invoice DTO
     */
    private InvoiceDto mapToDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerId(invoice.getCustomer().getId());
        dto.setCustomerName(invoice.getCustomer().getBusinessName());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setAmountPaid(invoice.getAmountPaid());
        dto.setBalanceRemaining(invoice.getBalanceRemaining());
        dto.setAllowsPartialPayment(invoice.isAllowsPartialPayment());
        dto.setPaymentLink(invoice.getPaymentLink());
        dto.setNotes(invoice.getNotes());
        dto.setTerms(invoice.getTerms());
        dto.setCancellationReason(invoice.getCancellationReason());
        dto.setRemindersSuppressed(invoice.isRemindersSuppressed());
        dto.setLastReminderSentAt(invoice.getLastReminderSentAt());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setSentAt(invoice.getSentAt());
        dto.setPaidAt(invoice.getPaidAt());
        dto.setCancelledAt(invoice.getCancelledAt());
        dto.setOverdue(invoice.isOverdue());

        // Map line items
        List<InvoiceDto.LineItemDto> lineItemDtos = invoice.getLineItems().stream()
            .map(this::mapLineItemToDto)
            .collect(Collectors.toList());
        dto.setLineItems(lineItemDtos);

        return dto;
    }

    /**
     * Maps LineItem entity to LineItemDto.
     *
     * @param lineItem the line item entity
     * @return the line item DTO
     */
    private InvoiceDto.LineItemDto mapLineItemToDto(LineItem lineItem) {
        return new InvoiceDto.LineItemDto(
            lineItem.getId(),
            lineItem.getDescription(),
            lineItem.getQuantity(),
            lineItem.getUnitPrice(),
            lineItem.getLineTotal(),
            lineItem.getLineOrder()
        );
    }
}
