package com.invoiceme.application.invoices.CreateInvoice;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Handles the CreateInvoiceCommand.
 * Creates a new invoice in DRAFT status with a generated invoice number.
 */
@Service
public class CreateInvoiceHandler {

    private static final DateTimeFormatter INVOICE_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CreateInvoiceValidator validator;

    public CreateInvoiceHandler(InvoiceRepository invoiceRepository,
                                CustomerRepository customerRepository,
                                CreateInvoiceValidator validator) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.validator = validator;
    }

    /**
     * Handles the creation of a new invoice.
     *
     * @param command the create invoice command
     * @return the ID of the newly created invoice
     * @throws IllegalArgumentException if validation fails or customer not found
     */
    @Transactional
    public UUID handle(CreateInvoiceCommand command) {
        // Validate command
        List<String> errors = validator.validate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }

        // Get customer
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + command.getCustomerId()));

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setIssueDate(command.getIssueDate());
        invoice.setDueDate(command.getDueDate());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setNotes(command.getNotes());

        // Set tax amount (default to zero if not provided)
        if (command.getTaxAmount() != null) {
            invoice.setTaxAmount(command.getTaxAmount());
        } else {
            invoice.setTaxAmount(BigDecimal.ZERO);
        }

        // Add line items
        for (CreateInvoiceCommand.LineItemDto itemDto : command.getLineItems()) {
            LineItem lineItem = new LineItem();
            lineItem.setDescription(itemDto.getDescription());
            lineItem.setQuantity(itemDto.getQuantity());
            lineItem.setUnitPrice(itemDto.getUnitPrice());
            lineItem.calculateLineTotal(); // Calculate line total before adding to invoice
            invoice.addLineItem(lineItem);
        }

        // Calculate totals
        invoice.calculateTotals();

        // Generate invoice number (format: INV-YYYYMM-NNNN)
        String invoiceNumber = generateInvoiceNumber(command.getIssueDate());
        invoice.setInvoiceNumber(invoiceNumber);

        // Save to repository
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // TODO: Publish InvoiceCreatedEvent
        // eventPublisher.publish(new InvoiceCreatedEvent(savedInvoice.getId()));

        return savedInvoice.getId();
    }

    /**
     * Generates a unique invoice number in the format: INV-YYYYMM-NNNN
     *
     * @param issueDate the issue date of the invoice
     * @return the generated invoice number
     */
    private String generateInvoiceNumber(LocalDate issueDate) {
        String yearMonth = issueDate.format(INVOICE_NUMBER_DATE_FORMAT);

        // Find the next sequential number for this month
        // In a production system, this would use a database sequence or atomic counter
        long count = invoiceRepository.count() + 1;
        String sequentialNumber = String.format("%04d", count);

        return "INV-" + yearMonth + "-" + sequentialNumber;
    }
}
