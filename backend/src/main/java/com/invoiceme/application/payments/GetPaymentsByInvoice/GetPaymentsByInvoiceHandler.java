package com.invoiceme.application.payments.GetPaymentsByInvoice;

import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the GetPaymentsByInvoiceQuery.
 * Retrieves all payments for a specific invoice.
 */
@Service
public class GetPaymentsByInvoiceHandler {

    private final PaymentRepository paymentRepository;

    public GetPaymentsByInvoiceHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Handles retrieving all payments for a specific invoice.
     *
     * @param query the get payments by invoice query
     * @return list of payment DTOs
     * @throws IllegalArgumentException if invoice ID is null
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> handle(GetPaymentsByInvoiceQuery query) {
        if (query.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        // Find payments by invoice ID
        List<Payment> payments = paymentRepository.findByInvoiceId(query.getInvoiceId());

        // Map to DTOs
        return payments.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
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
