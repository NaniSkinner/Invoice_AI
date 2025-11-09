package com.invoiceme.application.invoices.CreateInvoice;

import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates CreateInvoiceCommand business rules.
 */
@Component
public class CreateInvoiceValidator {

    private final CustomerRepository customerRepository;

    public CreateInvoiceValidator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Validates the create invoice command.
     *
     * @param command the command to validate
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validate(CreateInvoiceCommand command) {
        List<String> errors = new ArrayList<>();

        // Validate customer ID
        if (command.getCustomerId() == null) {
            errors.add("Customer ID is required");
        } else if (!customerRepository.existsById(command.getCustomerId())) {
            errors.add("Customer with ID " + command.getCustomerId() + " does not exist");
        }

        // Validate issue date
        if (command.getIssueDate() == null) {
            errors.add("Issue date is required");
        }

        // Validate due date
        if (command.getDueDate() == null) {
            errors.add("Due date is required");
        } else if (command.getIssueDate() != null && command.getDueDate().isBefore(command.getIssueDate())) {
            errors.add("Due date must be on or after issue date");
        }

        // Validate tax amount (optional but must be non-negative if provided)
        if (command.getTaxAmount() != null && command.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Tax amount must be non-negative");
        }

        // Validate line items
        if (command.getLineItems() == null || command.getLineItems().isEmpty()) {
            errors.add("At least one line item is required");
        } else {
            validateLineItems(command.getLineItems(), errors);
        }

        return errors;
    }

    /**
     * Validates individual line items.
     *
     * @param lineItems the line items to validate
     * @param errors the list to add errors to
     */
    private void validateLineItems(List<CreateInvoiceCommand.LineItemDto> lineItems, List<String> errors) {
        for (int i = 0; i < lineItems.size(); i++) {
            CreateInvoiceCommand.LineItemDto item = lineItems.get(i);
            String prefix = "Line item " + (i + 1) + ": ";

            if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
                errors.add(prefix + "description is required");
            } else if (item.getDescription().length() > 500) {
                errors.add(prefix + "description must not exceed 500 characters");
            }

            if (item.getQuantity() == null) {
                errors.add(prefix + "quantity is required");
            } else if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(prefix + "quantity must be greater than zero");
            }

            if (item.getUnitPrice() == null) {
                errors.add(prefix + "unit price is required");
            } else if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.add(prefix + "unit price must be non-negative");
            }
        }
    }
}
