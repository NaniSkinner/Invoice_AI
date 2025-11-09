package com.invoiceme.interfaces.rest;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.domain.payment.PaymentMethod;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import com.invoiceme.infrastructure.persistence.ReminderEmailRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for generating mockup demo data.
 * Only use in development/demo environments!
 */
@RestController
@RequestMapping("/api/mockup")
public class MockupDataController {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReminderEmailRepository reminderEmailRepository;

    public MockupDataController(CustomerRepository customerRepository,
                                InvoiceRepository invoiceRepository,
                                PaymentRepository paymentRepository,
                                ReminderEmailRepository reminderEmailRepository) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.reminderEmailRepository = reminderEmailRepository;
    }

    /**
     * Generate comprehensive mockup data for demo/testing.
     * Creates 10 customers, 25 invoices, and 7 payments.
     * Clears existing data first to avoid duplicates.
     */
    @PostMapping("/generate")
    public ResponseEntity<MockupDataResponse> generateMockupData() {
        // Clear existing data first to avoid duplicates
        reminderEmailRepository.deleteAll();
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();

        // Create customers first
        List<Customer> customers = createCustomers();

        // Create invoices with various statuses
        List<Invoice> invoices = createInvoices(customers);

        // Create payments for paid invoices
        List<Payment> payments = createPayments(invoices);

        MockupDataResponse response = new MockupDataResponse();
        response.setCustomersCreated(customers.size());
        response.setInvoicesCreated(invoices.size());
        response.setPaymentsCreated(payments.size());
        response.setMessage("Mockup data generated successfully!");

        return ResponseEntity.ok(response);
    }

    private List<Customer> createCustomers() {
        List<Customer> customers = new ArrayList<>();

        // Tech Companies
        customers.add(createCustomer("Acme Corporation", "John Smith", "john.smith@acmecorp.com",
            "+1-555-0101", "123 Innovation Drive", "San Francisco", "CA", "94105"));

        customers.add(createCustomer("TechStart Solutions", "Jane Doe", "jane.doe@techstart.io",
            "+1-555-0102", "456 Startup Lane", "Austin", "TX", "78701"));

        customers.add(createCustomer("Global Dynamics Inc", "Robert Johnson", "r.johnson@globaldynamics.com",
            "+1-555-0103", "789 Enterprise Blvd", "New York", "NY", "10001"));

        // Consulting & Services
        customers.add(createCustomer("Blue Ocean Consulting", "Sarah Williams", "sarah@blueocean.com",
            "+1-555-0104", "321 Strategy Court", "Chicago", "IL", "60601"));

        customers.add(createCustomer("Peak Performance LLC", "Michael Brown", "mbrown@peakperf.com",
            "+1-555-0105", "654 Excellence Way", "Seattle", "WA", "98101"));

        // Manufacturing & Retail
        customers.add(createCustomer("Summit Manufacturing", "Emily Davis", "e.davis@summitmfg.com",
            "+1-555-0106", "987 Industrial Pkwy", "Detroit", "MI", "48201"));

        customers.add(createCustomer("Metro Retailers Group", "David Martinez", "david.m@metroretail.com",
            "+1-555-0107", "147 Commerce St", "Los Angeles", "CA", "90001"));

        // Healthcare & Education
        customers.add(createCustomer("HealthFirst Medical", "Dr. Lisa Anderson", "l.anderson@healthfirst.com",
            "+1-555-0108", "258 Wellness Ave", "Boston", "MA", "02101"));

        customers.add(createCustomer("Bright Future Academy", "Thomas Wilson", "t.wilson@brightfuture.edu",
            "+1-555-0109", "369 Learning Lane", "Portland", "OR", "97201"));

        // Small Business
        customers.add(createCustomer("Sunset Cafe & Bakery", "Maria Garcia", "maria@sunsetcafe.com",
            "+1-555-0110", "741 Foodie Street", "Miami", "FL", "33101"));

        return customerRepository.saveAll(customers);
    }

    private Customer createCustomer(String businessName, String contactName, String email,
                                    String phone, String street, String city, String state, String zip) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setBusinessName(businessName);
        customer.setContactName(contactName);
        customer.setEmail(email);
        customer.setPhone(phone);

        // Create and set billing address
        Address billingAddress = new Address(street, city, state, zip, "USA");
        customer.setBillingAddress(billingAddress);

        customer.setActive(true);
        customer.setCreatedAt(LocalDateTime.now().minusDays(30));
        customer.setUpdatedAt(LocalDateTime.now().minusDays(30));
        return customer;
    }

    private List<Invoice> createInvoices(List<Customer> customers) {
        List<Invoice> invoices = new ArrayList<>();
        int invoiceNumber = 1;

        // Create 5 DRAFT invoices
        for (int i = 0; i < 5; i++) {
            Invoice invoice = createInvoice(customers.get(i % customers.size()), invoiceNumber++,
                InvoiceStatus.DRAFT, LocalDate.now(), 30, new BigDecimal("5000"), false);
            invoice.addLineItem(createLineItem("Consulting Services", 40, new BigDecimal("125")));
            invoices.add(invoice);
        }

        // Create 8 SENT invoices
        for (int i = 0; i < 8; i++) {
            Invoice invoice = createInvoice(customers.get((i + 5) % customers.size()), invoiceNumber++,
                InvoiceStatus.SENT, LocalDate.now().minusDays(10), 30, new BigDecimal("8000"), true);
            invoice.addLineItem(createLineItem("Software Development", 1, new BigDecimal("8000")));
            invoices.add(invoice);
        }

        // Create 5 overdue invoices (still SENT status but past due)
        for (int i = 0; i < 5; i++) {
            LocalDate issueDate = LocalDate.now().minusDays(45);
            Invoice invoice = createInvoice(customers.get(i % customers.size()), invoiceNumber++,
                InvoiceStatus.SENT, issueDate, 30, new BigDecimal("6000"), true);
            invoice.addLineItem(createLineItem("Training Services", 1, new BigDecimal("6000")));
            invoices.add(invoice);
        }

        // Create 5 PAID invoices
        for (int i = 0; i < 5; i++) {
            Invoice invoice = createInvoice(customers.get(i % customers.size()), invoiceNumber++,
                InvoiceStatus.SENT, LocalDate.now().minusDays(60), 30, new BigDecimal("10000"), false);
            invoice.addLineItem(createLineItem("Enterprise License", 1, new BigDecimal("10000")));
            invoice.markAsPaid();  // This will change status to PAID
            invoices.add(invoice);
        }

        // Create 2 CANCELLED invoices
        for (int i = 0; i < 2; i++) {
            Invoice invoice = createInvoice(customers.get(i % customers.size()), invoiceNumber++,
                InvoiceStatus.CANCELLED, LocalDate.now().minusDays(20), 30, new BigDecimal("3000"), false);
            invoice.addLineItem(createLineItem("Cancelled Service", 1, new BigDecimal("3000")));
            invoice.setCancellationReason("Project cancelled by customer");
            invoices.add(invoice);
        }

        return invoiceRepository.saveAll(invoices);
    }

    private Invoice createInvoice(Customer customer, int number, InvoiceStatus status,
                                  LocalDate issueDate, int daysUntilDue, BigDecimal subtotal, boolean hasPaymentLink) {
        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setInvoiceNumber(String.format("INV-202501-%04d", number));
        invoice.setCustomer(customer);
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(issueDate.plusDays(daysUntilDue));
        invoice.setStatus(status);
        invoice.setSubtotal(subtotal);

        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.10"));  // 10% tax
        invoice.setTaxAmount(taxAmount);

        BigDecimal totalAmount = subtotal.add(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceRemaining(totalAmount);
        invoice.setTerms("Net 30");
        invoice.setAllowsPartialPayment(true);

        if (hasPaymentLink) {
            invoice.setPaymentLink("payment-link-" + UUID.randomUUID().toString().substring(0, 8));
        }

        invoice.setCreatedAt(LocalDateTime.now().minusDays(30));
        invoice.setUpdatedAt(LocalDateTime.now().minusDays(30));

        return invoice;
    }

    private LineItem createLineItem(String description, int quantity, BigDecimal unitPrice) {
        LineItem item = new LineItem();
        item.setId(UUID.randomUUID());
        item.setDescription(description);
        item.setQuantity(BigDecimal.valueOf(quantity));
        item.setUnitPrice(unitPrice);
        item.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private List<Payment> createPayments(List<Invoice> invoices) {
        List<Payment> payments = new ArrayList<>();

        // Find PAID invoices and create payments for them
        List<Invoice> paidInvoices = invoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
            .toList();

        for (Invoice invoice : paidInvoices) {
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setInvoice(invoice);
            payment.setPaymentAmount(invoice.getTotalAmount());
            payment.setPaymentDate(LocalDate.now().minusDays(10));
            payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            payment.setTransactionReference("WIRE-" + UUID.randomUUID().toString().substring(0, 8));
            payment.setNotes("Full payment received");
            payment.setCreatedAt(LocalDateTime.now().minusDays(10));
            payments.add(payment);
        }

        return paymentRepository.saveAll(payments);
    }

    /**
     * Response DTO for mockup data generation.
     */
    public static class MockupDataResponse {
        private int customersCreated;
        private int invoicesCreated;
        private int paymentsCreated;
        private String message;

        public MockupDataResponse() {}

        public int getCustomersCreated() {
            return customersCreated;
        }

        public void setCustomersCreated(int customersCreated) {
            this.customersCreated = customersCreated;
        }

        public int getInvoicesCreated() {
            return invoicesCreated;
        }

        public void setInvoicesCreated(int invoicesCreated) {
            this.invoicesCreated = invoicesCreated;
        }

        public int getPaymentsCreated() {
            return paymentsCreated;
        }

        public void setPaymentsCreated(int paymentsCreated) {
            this.paymentsCreated = paymentsCreated;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
