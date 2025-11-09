package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles the RecordPaymentCommand.
 * Records a payment against an invoice and updates invoice balances.
 */
@Service
public class RecordPaymentHandler {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RecordPaymentValidator validator;

    public RecordPaymentHandler(PaymentRepository paymentRepository,
                                InvoiceRepository invoiceRepository,
                                RecordPaymentValidator validator) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.validator = validator;
    }

    /**
     * Handles recording a payment against an invoice.
     *
     * @param command the record payment command
     * @return the ID of the payment (existing or newly created)
     * @throws IllegalArgumentException if validation fails or invoice not found
     */
    @Transactional
    public UUID handle(RecordPaymentCommand command) {
        // Validate command
        List<String> errors = validator.validate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }

        // Check idempotency: if payment already exists with this ID, return existing payment ID
        Optional<Payment> existingPayment = paymentRepository.findById(command.getId());
        if (existingPayment.isPresent()) {
            return existingPayment.get().getId();
        }

        // Fetch invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + command.getInvoiceId()));

        // Create payment entity
        Payment payment = new Payment();
        payment.setId(command.getId());
        payment.setInvoice(invoice);
        payment.setPaymentAmount(command.getPaymentAmount());
        payment.setPaymentDate(command.getPaymentDate());
        payment.setPaymentMethod(command.getPaymentMethod());
        payment.setTransactionReference(command.getTransactionReference());
        payment.setNotes(command.getNotes());

        // Validate payment (domain validation)
        payment.validate();

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice amounts
        BigDecimal newAmountPaid = invoice.getAmountPaid().add(command.getPaymentAmount());
        invoice.setAmountPaid(newAmountPaid);

        BigDecimal newBalanceRemaining = invoice.getTotalAmount().subtract(newAmountPaid);
        invoice.setBalanceRemaining(newBalanceRemaining);

        // If fully paid, mark invoice as paid
        if (newBalanceRemaining.compareTo(BigDecimal.ZERO) == 0) {
            invoice.markAsPaid();
        }

        // Save updated invoice
        invoiceRepository.save(invoice);

        // TODO: Publish PaymentRecordedEvent
        // eventPublisher.publish(new PaymentRecordedEvent(savedPayment.getId()));

        return savedPayment.getId();
    }
}
