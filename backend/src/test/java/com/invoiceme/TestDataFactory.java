package com.invoiceme;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 * Provides builder methods for creating domain entities with sensible defaults.
 */
public class TestDataFactory {

    // Customer Builders

    public static CustomerBuilder aCustomer() {
        return new CustomerBuilder();
    }

    public static class CustomerBuilder {
        private UUID id = UUID.randomUUID();
        private String businessName = "Test Company Inc.";
        private String contactName = "John Doe";
        private String email = "john.doe@testcompany.com";
        private String phone = "555-0100";
        private Address billingAddress = defaultAddress();
        private Address shippingAddress = null;
        private boolean active = true;

        public CustomerBuilder withId(UUID id) {
            this.id = id;
            return this;
        }

        public CustomerBuilder withBusinessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public CustomerBuilder withContactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public CustomerBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public CustomerBuilder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public CustomerBuilder withBillingAddress(Address address) {
            this.billingAddress = address;
            return this;
        }

        public CustomerBuilder withShippingAddress(Address address) {
            this.shippingAddress = address;
            return this;
        }

        public CustomerBuilder inactive() {
            this.active = false;
            return this;
        }

        public Customer build() {
            Customer customer = new Customer();
            customer.setId(id);
            customer.setBusinessName(businessName);
            customer.setContactName(contactName);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setBillingAddress(billingAddress);
            customer.setShippingAddress(shippingAddress);
            customer.setActive(active);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            return customer;
        }
    }

    // Invoice Builders

    public static InvoiceBuilder anInvoice() {
        return new InvoiceBuilder();
    }

    public static class InvoiceBuilder {
        private UUID id = UUID.randomUUID();
        private String invoiceNumber = "INV-TEST-" + System.currentTimeMillis();
        private Customer customer;
        private LocalDate issueDate = LocalDate.now();
        private LocalDate dueDate = LocalDate.now().plusDays(30);
        private InvoiceStatus status = InvoiceStatus.DRAFT;
        private List<LineItem> lineItems = new ArrayList<>();
        private BigDecimal taxAmount = BigDecimal.ZERO;
        private boolean allowsPartialPayment = false;
        private String notes = "Test invoice notes";

        public InvoiceBuilder withId(UUID id) {
            this.id = id;
            return this;
        }

        public InvoiceBuilder withInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public InvoiceBuilder withCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public InvoiceBuilder withIssueDate(LocalDate issueDate) {
            this.issueDate = issueDate;
            return this;
        }

        public InvoiceBuilder withDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public InvoiceBuilder withStatus(InvoiceStatus status) {
            this.status = status;
            return this;
        }

        public InvoiceBuilder withLineItem(String description, int quantity, BigDecimal unitPrice) {
            LineItem item = new LineItem();
            item.setId(UUID.randomUUID());
            item.setDescription(description);
            item.setQuantity(BigDecimal.valueOf(quantity));
            item.setUnitPrice(unitPrice);
            item.setLineTotal(BigDecimal.valueOf(quantity).multiply(unitPrice));
            item.setLineOrder(lineItems.size());
            lineItems.add(item);
            return this;
        }

        public InvoiceBuilder withTaxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        public InvoiceBuilder allowsPartialPayment() {
            this.allowsPartialPayment = true;
            return this;
        }

        public InvoiceBuilder withNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Invoice build() {
            if (customer == null) {
                customer = aCustomer().build();
            }

            Invoice invoice = new Invoice();
            invoice.setId(id);
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setCustomer(customer);
            invoice.setIssueDate(issueDate);
            invoice.setDueDate(dueDate);
            invoice.setStatus(status);
            invoice.setTaxAmount(taxAmount);
            invoice.setAllowsPartialPayment(allowsPartialPayment);
            invoice.setNotes(notes);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());

            // Set line items and calculate totals
            for (LineItem item : lineItems) {
                item.setInvoice(invoice);
            }
            invoice.setLineItems(lineItems);
            invoice.calculateTotals();

            return invoice;
        }
    }

    // Payment Builders

    public static PaymentBuilder aPayment() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private UUID id = UUID.randomUUID();
        private Invoice invoice;
        private BigDecimal paymentAmount = new BigDecimal("100.00");
        private LocalDate paymentDate = LocalDate.now();
        private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        private String transactionReference = "TEST-TXN-" + System.currentTimeMillis();
        private String notes = "Test payment";

        public PaymentBuilder withId(UUID id) {
            this.id = id;
            return this;
        }

        public PaymentBuilder withInvoice(Invoice invoice) {
            this.invoice = invoice;
            return this;
        }

        public PaymentBuilder withAmount(BigDecimal amount) {
            this.paymentAmount = amount;
            return this;
        }

        public PaymentBuilder withPaymentDate(LocalDate date) {
            this.paymentDate = date;
            return this;
        }

        public PaymentBuilder withPaymentMethod(PaymentMethod method) {
            this.paymentMethod = method;
            return this;
        }

        public PaymentBuilder withTransactionReference(String reference) {
            this.transactionReference = reference;
            return this;
        }

        public PaymentBuilder withNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Payment build() {
            if (invoice == null) {
                throw new IllegalStateException("Payment must have an invoice");
            }

            Payment payment = new Payment();
            payment.setId(id);
            payment.setInvoice(invoice);
            payment.setPaymentAmount(paymentAmount);
            payment.setPaymentDate(paymentDate);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionReference(transactionReference);
            payment.setNotes(notes);
            payment.setCreatedAt(LocalDateTime.now());

            return payment;
        }
    }

    // Address Helper

    public static Address defaultAddress() {
        return new Address(
            "123 Test Street",
            "Test City",
            "TS",
            "12345",
            "USA"
        );
    }

    public static Address addressWithStreet(String street) {
        return new Address(
            street,
            "Test City",
            "TS",
            "12345",
            "USA"
        );
    }

    // Common Test Scenarios

    public static Customer createCustomerWithInvoices(int invoiceCount) {
        Customer customer = aCustomer().build();
        for (int i = 0; i < invoiceCount; i++) {
            anInvoice()
                .withCustomer(customer)
                .withInvoiceNumber("INV-TEST-" + i)
                .withLineItem("Service " + i, 1, new BigDecimal("100.00"))
                .build();
        }
        return customer;
    }

    public static Invoice createInvoiceWithLineItems(Customer customer, int lineItemCount) {
        InvoiceBuilder builder = anInvoice().withCustomer(customer);
        for (int i = 0; i < lineItemCount; i++) {
            builder.withLineItem("Line Item " + (i + 1), 2, new BigDecimal("50.00"));
        }
        return builder.build();
    }

    public static Invoice createOverdueInvoice(Customer customer) {
        return anInvoice()
            .withCustomer(customer)
            .withStatus(InvoiceStatus.SENT)
            .withDueDate(LocalDate.now().minusDays(10))
            .withLineItem("Overdue Service", 1, new BigDecimal("500.00"))
            .build();
    }
}

