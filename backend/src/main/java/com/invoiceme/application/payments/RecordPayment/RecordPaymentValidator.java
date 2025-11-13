package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validates RecordPaymentCommand business rules.
 */
@Component
public class RecordPaymentValidator {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public RecordPaymentValidator(PaymentRepository paymentRepository,
                                  InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Validates the record payment command.
     *
     * @param command the command to validate
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validate(RecordPaymentCommand command) {
        List<String> errors = new ArrayList<>();

        // Validate payment ID (required for idempotency)
        if (command.getId() == null) {
            errors.add("Payment ID is required for idempotency");
        } else if (paymentRepository.existsById(command.getId())) {
            // Payment already exists - this is handled as idempotent operation, not an error
            // The handler will return the existing payment ID
        }

        // Validate invoice ID
        if (command.getInvoiceId() == null) {
            errors.add("Invoice ID is required");
        } else {
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(command.getInvoiceId());
            if (invoiceOpt.isEmpty()) {
                errors.add("Invoice with ID " + command.getInvoiceId() + " does not exist");
            } else {
                Invoice invoice = invoiceOpt.orElseThrow(() ->
                    new IllegalStateException("Invoice should exist but was not found"));

                // Check invoice is not cancelled
                if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
                    errors.add("Cannot record payment for a cancelled invoice");
                }

                // Validate payment amount
                if (command.getPaymentAmount() == null) {
                    errors.add("Payment amount is required");
                } else if (command.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Payment amount must be greater than zero");
                } else {
                    BigDecimal balanceRemaining = invoice.getBalanceRemaining();

                    // Check for overpayment
                    if (command.getPaymentAmount().compareTo(balanceRemaining) > 0) {
                        errors.add(String.format("Payment amount (%.2f) exceeds invoice balance remaining (%.2f)",
                            command.getPaymentAmount(), balanceRemaining));
                    }

                    // Check partial payment rules
                    if (!invoice.isAllowsPartialPayment() &&
                        command.getPaymentAmount().compareTo(balanceRemaining) != 0) {
                        errors.add("This invoice does not allow partial payments. Payment must equal the balance remaining (%.2f)"
                            .formatted(balanceRemaining));
                    }
                }
            }
        }

        // Validate payment date
        if (command.getPaymentDate() == null) {
            errors.add("Payment date is required");
        }

        // Validate payment method
        if (command.getPaymentMethod() == null) {
            errors.add("Payment method is required");
        }

        return errors;
    }
}
