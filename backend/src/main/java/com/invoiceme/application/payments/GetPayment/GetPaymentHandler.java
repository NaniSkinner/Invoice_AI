package com.invoiceme.application.payments.GetPayment;

import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the GetPaymentQuery.
 * Fetches payment and maps to DTO with read-only transaction.
 */
@Service
public class GetPaymentHandler {

    private final PaymentRepository paymentRepository;

    public GetPaymentHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Handles retrieving a payment by ID.
     *
     * @param query the get payment query
     * @return the payment DTO
     * @throws IllegalArgumentException if payment not found
     */
    @Transactional(readOnly = true)
    public PaymentDto handle(GetPaymentQuery query) {
        if (query.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment ID is required");
        }

        // Get payment
        Payment payment = paymentRepository.findById(query.getPaymentId())
            .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + query.getPaymentId()));

        // Map to DTO
        return mapToDto(payment);
    }

    /**
     * Maps Payment entity to PaymentDto.
     *
     * @param payment the payment entity
     * @return the payment DTO
     */
    private PaymentDto mapToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setInvoiceId(payment.getInvoice().getId());
        dto.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
        dto.setPaymentAmount(payment.getPaymentAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionReference(payment.getTransactionReference());
        dto.setNotes(payment.getNotes());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
