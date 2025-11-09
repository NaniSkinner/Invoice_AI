package com.invoiceme.application.payments.ListPayments;

import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the ListPaymentsQuery.
 * Retrieves all payments or filters by invoice ID.
 */
@Service
public class ListPaymentsHandler {

    private final PaymentRepository paymentRepository;

    public ListPaymentsHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Handles retrieving a list of payments.
     *
     * @param query the list payments query
     * @return list of payment DTOs
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> handle(ListPaymentsQuery query) {
        List<Payment> payments;

        // Filter by invoice ID if provided
        if (query.getInvoiceId() != null) {
            payments = paymentRepository.findByInvoiceId(query.getInvoiceId());
        } else {
            payments = paymentRepository.findAll();
        }

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
