package com.invoiceme.application.chat;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI Chat Assistant Service with template-based NLP.
 * Provides pattern matching for common invoice-related queries without external AI APIs.
 * Enhanced with comprehensive accountant assistant features.
 */
@Service
public class ChatService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    public ChatService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository, PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Process a chat message and generate an appropriate response.
     *
     * @param request the chat message request
     * @return the chat response with data and suggestions
     */
    public ChatMessageResponse processMessage(ChatMessageRequest request) {
        String message = request.getMessage().toLowerCase().trim();
        String conversationId = request.getConversationId();

        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        ChatMessageResponse response = new ChatMessageResponse();
        response.setConversationId(conversationId);

        // Pattern matching for different query types
        // Try specific invoice lookup first (more specific pattern)
        if (matchesPattern(message, "invoice\\s+(inv-\\d+|\\d+)") || matchesPattern(message, "(inv-\\d+)")) {
            return handleInvoiceLookupQuery(message, response);
        }
        // Customer-specific queries
        else if (matchesPattern(message, "invoices?\\s+(for|from|by|of)\\s+([a-zA-Z0-9\\s@\\.]+)")) {
            return handleCustomerInvoiceQuery(message, response);
        } else if (matchesPattern(message, "summary\\s+(for|of|about)\\s+([a-zA-Z0-9\\s@\\.]+)")) {
            return handleCustomerSummaryQuery(message, response);
        }
        // Payment queries
        else if (matchesPattern(message, "payment|paid.*this.*week|received.*week")) {
            return handlePaymentHistoryQuery(message, response);
        }
        // Outstanding balance queries
        else if (matchesPattern(message, "total.*owe|outstanding.*balance|receivable|still.*owe")) {
            return handleOutstandingBalanceQuery(response);
        }
        // Statistics queries
        else if (matchesPattern(message, "average|statistic|analytics|stat")) {
            return handleInvoiceStatisticsQuery(message, response);
        }
        // Overdue invoices
        else if (matchesPattern(message, "overdue|past due|late")) {
            return handleOverdueInvoicesQuery(response);
        }
        // Revenue queries
        else if (matchesPattern(message, "revenue|income|earnings")) {
            return handleRevenueQuery(message, response);
        }
        // Paid invoices
        else if (matchesPattern(message, "paid.*invoice")) {
            return handlePaidInvoicesQuery(message, response);
        }
        // Draft invoices
        else if (matchesPattern(message, "draft")) {
            return handleDraftInvoicesQuery(response);
        }
        // Sent invoices
        else if (matchesPattern(message, "sent|pending|outstanding.*invoice")) {
            return handleSentInvoicesQuery(response);
        }
        // Customer queries (general)
        else if (matchesPattern(message, "customer|client")) {
            return handleCustomerQuery(message, response);
        }
        // Help and instructions
        else if (matchesPattern(message, "create.*invoice|new invoice")) {
            return handleCreateInvoiceHelp(response);
        } else if (matchesPattern(message, "send.*reminder|reminder")) {
            return handleReminderHelp(response);
        } else if (matchesPattern(message, "status|count|how many")) {
            return handleInvoiceStatusQuery(response);
        } else if (matchesPattern(message, "help|what can you do")) {
            return handleHelpQuery(response);
        } else {
            return handleUnknownQuery(message, response);
        }
    }

    /**
     * Handle queries about overdue invoices.
     */
    private ChatMessageResponse handleOverdueInvoicesQuery(ChatMessageResponse response) {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository.findByStatusAndDueDateBefore(
            InvoiceStatus.SENT, today
        );

        BigDecimal totalOverdue = overdueInvoices.stream()
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String message;
        if (overdueInvoices.isEmpty()) {
            message = "Good news! You have no overdue invoices at the moment.";
        } else {
            message = String.format(
                "You have %d overdue invoice%s with a total outstanding balance of $%.2f.",
                overdueInvoices.size(),
                overdueInvoices.size() == 1 ? "" : "s",
                totalOverdue
            );

            if (overdueInvoices.size() <= 5) {
                message += "\n\nOverdue invoices:";
                for (Invoice invoice : overdueInvoices) {
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), today);
                    message += String.format(
                        "\n- %s: $%.2f (Due: %s, %d days overdue)",
                        invoice.getInvoiceNumber(),
                        invoice.getBalanceRemaining(),
                        invoice.getDueDate(),
                        daysOverdue
                    );
                }
            }
        }

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me all sent invoices",
            "What's my total revenue?",
            "How many draft invoices do I have?"
        ));

        return response;
    }

    /**
     * Handle queries about revenue.
     */
    private ChatMessageResponse handleRevenueQuery(String message, ChatMessageResponse response) {
        List<Invoice> allInvoices = invoiceRepository.findAll();

        // Check if query is for specific time period
        boolean isThisMonth = message.contains("this month") || message.contains("current month");
        boolean isThisYear = message.contains("this year") || message.contains("current year");

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int invoiceCount = 0;
        String period = "";

        if (isThisMonth) {
            YearMonth currentMonth = YearMonth.now();
            for (Invoice invoice : allInvoices) {
                if (invoice.getStatus() == InvoiceStatus.PAID &&
                    invoice.getPaidAt() != null &&
                    YearMonth.from(invoice.getPaidAt()).equals(currentMonth)) {
                    totalRevenue = totalRevenue.add(invoice.getTotalAmount());
                    invoiceCount++;
                }
            }
            period = " this month";
        } else if (isThisYear) {
            int currentYear = LocalDate.now().getYear();
            for (Invoice invoice : allInvoices) {
                if (invoice.getStatus() == InvoiceStatus.PAID &&
                    invoice.getPaidAt() != null &&
                    invoice.getPaidAt().getYear() == currentYear) {
                    totalRevenue = totalRevenue.add(invoice.getTotalAmount());
                    invoiceCount++;
                }
            }
            period = " this year";
        } else {
            // Total revenue (all time)
            for (Invoice invoice : allInvoices) {
                if (invoice.getStatus() == InvoiceStatus.PAID) {
                    totalRevenue = totalRevenue.add(invoice.getTotalAmount());
                    invoiceCount++;
                }
            }
            period = " (all time)";
        }

        String responseMessage = String.format(
            "Your total revenue%s is $%.2f from %d paid invoice%s.",
            period,
            totalRevenue,
            invoiceCount,
            invoiceCount == 1 ? "" : "s"
        );

        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "How many customers do I have?",
            "Show me draft invoices"
        ));

        return response;
    }

    /**
     * Handle queries about draft invoices.
     */
    private ChatMessageResponse handleDraftInvoicesQuery(ChatMessageResponse response) {
        List<Invoice> drafts = invoiceRepository.findByStatus(InvoiceStatus.DRAFT);

        String message;
        if (drafts.isEmpty()) {
            message = "You don't have any draft invoices at the moment.";
        } else {
            message = String.format("You have %d draft invoice%s ready to be sent.",
                drafts.size(),
                drafts.size() == 1 ? "" : "s"
            );

            if (drafts.size() <= 5) {
                message += "\n\nDraft invoices:";
                for (Invoice invoice : drafts) {
                    message += String.format(
                        "\n- %s: %s - $%.2f",
                        invoice.getInvoiceNumber(),
                        invoice.getCustomer().getBusinessName(),
                        invoice.getTotalAmount()
                    );
                }
            }
        }

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me sent invoices",
            "Create a new invoice",
            "What's my total revenue?"
        ));

        return response;
    }

    /**
     * Handle queries about sent invoices.
     */
    private ChatMessageResponse handleSentInvoicesQuery(ChatMessageResponse response) {
        List<Invoice> sentInvoices = invoiceRepository.findByStatus(InvoiceStatus.SENT);

        BigDecimal totalOutstanding = sentInvoices.stream()
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String message;
        if (sentInvoices.isEmpty()) {
            message = "You don't have any sent invoices awaiting payment.";
        } else {
            message = String.format(
                "You have %d sent invoice%s with a total outstanding balance of $%.2f.",
                sentInvoices.size(),
                sentInvoices.size() == 1 ? "" : "s",
                totalOutstanding
            );
        }

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "Show me draft invoices",
            "How many customers do I have?"
        ));

        return response;
    }

    /**
     * Handle queries about customers.
     */
    private ChatMessageResponse handleCustomerQuery(String message, ChatMessageResponse response) {
        List<com.invoiceme.domain.customer.Customer> customers = customerRepository.findAll();
        List<com.invoiceme.domain.customer.Customer> activeCustomers =
            customerRepository.findByActiveTrue();

        String responseMessage = String.format(
            "You have %d customer%s in your system (%d active).",
            customers.size(),
            customers.size() == 1 ? "" : "s",
            activeCustomers.size()
        );

        if (!activeCustomers.isEmpty() && activeCustomers.size() <= 5) {
            responseMessage += "\n\nActive customers:";
            for (com.invoiceme.domain.customer.Customer customer : activeCustomers) {
                responseMessage += String.format(
                    "\n- %s (%s)",
                    customer.getBusinessName(),
                    customer.getEmail()
                );
            }
        }

        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "Create a new invoice",
            "What's my total revenue?"
        ));

        return response;
    }

    /**
     * Handle queries about invoice status summary.
     */
    private ChatMessageResponse handleInvoiceStatusQuery(ChatMessageResponse response) {
        List<Invoice> allInvoices = invoiceRepository.findAll();

        Map<InvoiceStatus, Long> statusCounts = allInvoices.stream()
            .collect(Collectors.groupingBy(Invoice::getStatus, Collectors.counting()));

        String message = "Here's your invoice status summary:\n";
        message += String.format("- Draft: %d\n", statusCounts.getOrDefault(InvoiceStatus.DRAFT, 0L));
        message += String.format("- Sent: %d\n", statusCounts.getOrDefault(InvoiceStatus.SENT, 0L));
        message += String.format("- Paid: %d\n", statusCounts.getOrDefault(InvoiceStatus.PAID, 0L));
        message += String.format("- Cancelled: %d\n", statusCounts.getOrDefault(InvoiceStatus.CANCELLED, 0L));
        message += String.format("\nTotal invoices: %d", allInvoices.size());

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "What's my total revenue?",
            "How many customers do I have?"
        ));

        return response;
    }

    /**
     * Handle queries about creating invoices.
     */
    private ChatMessageResponse handleCreateInvoiceHelp(ChatMessageResponse response) {
        String message = "To create a new invoice:\n\n" +
            "1. Navigate to the Invoices page\n" +
            "2. Click the 'Create Invoice' button\n" +
            "3. Select a customer from the dropdown\n" +
            "4. Add line items with descriptions and amounts\n" +
            "5. Set the due date\n" +
            "6. Save as draft or send immediately\n\n" +
            "Would you like help with anything else?";

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me draft invoices",
            "How many customers do I have?",
            "Send a reminder"
        ));

        return response;
    }

    /**
     * Handle queries about sending reminders.
     */
    private ChatMessageResponse handleReminderHelp(ChatMessageResponse response) {
        String message = "To send a payment reminder:\n\n" +
            "1. Go to the Invoices page\n" +
            "2. Find the sent invoice you want to remind about\n" +
            "3. Click the 'Send Reminder' button\n" +
            "4. An email reminder will be sent to the customer\n\n" +
            "Automatic reminders are also sent for overdue invoices.";

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "Show me sent invoices",
            "Create a new invoice"
        ));

        return response;
    }

    /**
     * Handle help queries.
     */
    private ChatMessageResponse handleHelpQuery(ChatMessageResponse response) {
        String message = "I can help you with:\n\n" +
            "Invoice Queries:\n" +
            "- Finding overdue, draft, sent, or paid invoices\n" +
            "- Getting invoice status summaries\n" +
            "- Looking up specific invoices (e.g., 'Tell me about INV-001')\n" +
            "- Calculating invoice statistics and averages\n\n" +
            "Financial Information:\n" +
            "- Calculating revenue (total, this month, this year)\n" +
            "- Checking outstanding balances with aging breakdown\n" +
            "- Viewing payment history (this week, this month)\n\n" +
            "Customer Information:\n" +
            "- Viewing customer lists and counts\n" +
            "- Getting invoices for specific customers\n" +
            "- Getting comprehensive customer summaries\n\n" +
            "Instructions & Help:\n" +
            "- How to create invoices\n" +
            "- How to send payment reminders\n\n" +
            "Just ask me a question in plain English!";

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "What's the total amount we're still owed?",
            "Show me payments received this month"
        ));

        return response;
    }

    /**
     * Handle unknown queries with helpful fallback.
     */
    private ChatMessageResponse handleUnknownQuery(String message, ChatMessageResponse response) {
        String responseMessage = "I'm not sure I understand that question. Here are some things I can help with:\n\n" +
            "- Invoice status and summaries\n" +
            "- Overdue invoices\n" +
            "- Revenue calculations\n" +
            "- Customer information\n" +
            "- Creating invoices\n" +
            "- Sending reminders\n\n" +
            "Try asking 'What can you do?' to see all available options.";

        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "What can you do?",
            "Show me overdue invoices",
            "What's my total revenue?"
        ));

        return response;
    }

    /**
     * Handle queries about payment history.
     */
    private ChatMessageResponse handlePaymentHistoryQuery(String message, ChatMessageResponse response) {
        List<Payment> allPayments = paymentRepository.findAll();
        
        // Determine time period
        boolean isThisWeek = message.contains("this week") || message.contains("last week");
        boolean isThisMonth = message.contains("this month") || message.contains("current month");
        boolean isThisYear = message.contains("this year") || message.contains("current year");
        
        LocalDate filterDate = LocalDate.now();
        String period = " (all time)";
        
        if (isThisWeek) {
            filterDate = LocalDate.now().minusDays(7);
            period = " this week";
        } else if (isThisMonth) {
            filterDate = LocalDate.now().minusMonths(1);
            period = " this month";
        } else if (isThisYear) {
            filterDate = LocalDate.now().minusYears(1);
            period = " this year";
        }
        
        // Filter payments by date
        LocalDate finalFilterDate = filterDate;
        List<Payment> filteredPayments = allPayments.stream()
            .filter(p -> !isThisWeek && !isThisMonth && !isThisYear || p.getPaymentDate().isAfter(finalFilterDate))
            .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
            .collect(Collectors.toList());
        
        BigDecimal totalPaid = filteredPayments.stream()
            .map(Payment::getPaymentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String responseMessage;
        if (filteredPayments.isEmpty()) {
            responseMessage = String.format("No payments received%s.", period);
        } else {
            responseMessage = String.format(
                "You received %d payment%s totaling $%.2f%s.",
                filteredPayments.size(),
                filteredPayments.size() == 1 ? "" : "s",
                totalPaid,
                period
            );
            
            if (filteredPayments.size() <= 10) {
                responseMessage += "\n\nRecent payments:";
                for (Payment payment : filteredPayments) {
                    responseMessage += String.format(
                        "\n- %s: $%.2f on %s (Invoice: %s)",
                        payment.getInvoice().getInvoiceNumber(),
                        payment.getPaymentAmount(),
                        payment.getPaymentDate(),
                        payment.getInvoice().getInvoiceNumber()
                    );
                }
            }
        }
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "What's my total revenue?",
            "Show me overdue invoices",
            "What's the total amount we're still owed?"
        ));
        
        return response;
    }
    
    /**
     * Handle queries about total outstanding balance.
     */
    private ChatMessageResponse handleOutstandingBalanceQuery(ChatMessageResponse response) {
        List<Invoice> sentInvoices = invoiceRepository.findByStatus(InvoiceStatus.SENT);
        LocalDate today = LocalDate.now();
        
        BigDecimal totalOutstanding = sentInvoices.stream()
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Aging breakdown
        long current = 0, overdue7 = 0, overdue14 = 0, overdue30 = 0;
        BigDecimal currentAmount = BigDecimal.ZERO;
        BigDecimal overdue7Amount = BigDecimal.ZERO;
        BigDecimal overdue14Amount = BigDecimal.ZERO;
        BigDecimal overdue30Amount = BigDecimal.ZERO;
        
        for (Invoice invoice : sentInvoices) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), today);
            BigDecimal balance = invoice.getBalanceRemaining();
            
            if (daysOverdue < 0) {
                current++;
                currentAmount = currentAmount.add(balance);
            } else if (daysOverdue < 7) {
                overdue7++;
                overdue7Amount = overdue7Amount.add(balance);
            } else if (daysOverdue < 14) {
                overdue14++;
                overdue14Amount = overdue14Amount.add(balance);
            } else {
                overdue30++;
                overdue30Amount = overdue30Amount.add(balance);
            }
        }
        
        String responseMessage;
        if (sentInvoices.isEmpty()) {
            responseMessage = "Great news! You have no outstanding invoices. All invoices are either paid or in draft.";
        } else {
            responseMessage = String.format(
                "Total outstanding balance: $%.2f across %d invoice%s.\n\n" +
                "Aging Breakdown:\n" +
                "- Current (not yet due): %d invoice%s - $%.2f\n" +
                "- 0-7 days overdue: %d invoice%s - $%.2f\n" +
                "- 7-14 days overdue: %d invoice%s - $%.2f\n" +
                "- 30+ days overdue: %d invoice%s - $%.2f",
                totalOutstanding,
                sentInvoices.size(),
                sentInvoices.size() == 1 ? "" : "s",
                current, current == 1 ? "" : "s", currentAmount,
                overdue7, overdue7 == 1 ? "" : "s", overdue7Amount,
                overdue14, overdue14 == 1 ? "" : "s", overdue14Amount,
                overdue30, overdue30 == 1 ? "" : "s", overdue30Amount
            );
        }
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "What's my total revenue?",
            "Show me payments received this month"
        ));
        
        return response;
    }
    
    /**
     * Handle queries about paid invoices.
     */
    private ChatMessageResponse handlePaidInvoicesQuery(String message, ChatMessageResponse response) {
        List<Invoice> paidInvoices = invoiceRepository.findByStatus(InvoiceStatus.PAID);
        
        // Check for time filtering
        boolean isThisMonth = message.contains("this month") || message.contains("current month");
        boolean isThisYear = message.contains("this year") || message.contains("current year");
        
        List<Invoice> filteredInvoices = paidInvoices;
        String period = "";
        
        if (isThisMonth) {
            YearMonth currentMonth = YearMonth.now();
            filteredInvoices = paidInvoices.stream()
                .filter(inv -> inv.getPaidAt() != null && YearMonth.from(inv.getPaidAt()).equals(currentMonth))
                .collect(Collectors.toList());
            period = " this month";
        } else if (isThisYear) {
            int currentYear = LocalDate.now().getYear();
            filteredInvoices = paidInvoices.stream()
                .filter(inv -> inv.getPaidAt() != null && inv.getPaidAt().getYear() == currentYear)
                .collect(Collectors.toList());
            period = " this year";
        }
        
        BigDecimal totalPaid = filteredInvoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String responseMessage;
        if (filteredInvoices.isEmpty()) {
            responseMessage = String.format("No paid invoices%s.", period);
        } else {
            responseMessage = String.format(
                "You have %d paid invoice%s%s with a total value of $%.2f.",
                filteredInvoices.size(),
                filteredInvoices.size() == 1 ? "" : "s",
                period,
                totalPaid
            );
            
            if (filteredInvoices.size() <= 5) {
                responseMessage += "\n\nPaid invoices:";
                for (Invoice invoice : filteredInvoices) {
                    responseMessage += String.format(
                        "\n- %s: %s - $%.2f (Paid: %s)",
                        invoice.getInvoiceNumber(),
                        invoice.getCustomer().getBusinessName(),
                        invoice.getTotalAmount(),
                        invoice.getPaidAt() != null ? invoice.getPaidAt().toLocalDate() : "Unknown"
                    );
                }
            }
        }
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "Show me sent invoices",
            "What's my total revenue this month?",
            "Show me overdue invoices"
        ));
        
        return response;
    }
    
    /**
     * Handle queries about invoices for a specific customer.
     */
    private ChatMessageResponse handleCustomerInvoiceQuery(String message, ChatMessageResponse response) {
        // Extract customer name from query
        String customerIdentifier = extractCustomerIdentifier(message);
        
        if (customerIdentifier == null || customerIdentifier.isEmpty()) {
            response.setResponse("I couldn't identify which customer you're asking about. Please try again with the customer's business name or email.");
            response.setSuggestions(Arrays.asList(
                "How many customers do I have?",
                "Show me overdue invoices",
                "What can you do?"
            ));
            return response;
        }
        
        // Find customer
        com.invoiceme.domain.customer.Customer customer = findCustomerByIdentifier(customerIdentifier);
        
        if (customer == null) {
            response.setResponse(String.format(
                "I couldn't find a customer matching '%s'. Please check the name or try using their email address.",
                customerIdentifier
            ));
            response.setSuggestions(Arrays.asList(
                "How many customers do I have?",
                "Show me all invoices",
                "What can you do?"
            ));
            return response;
        }
        
        // Get all invoices for this customer
        List<Invoice> customerInvoices = invoiceRepository.findByCustomerId(customer.getId());
        
        if (customerInvoices.isEmpty()) {
            response.setResponse(String.format(
                "No invoices found for %s (%s).",
                customer.getBusinessName(),
                customer.getEmail()
            ));
            response.setSuggestions(Arrays.asList(
                "Create a new invoice",
                "How many customers do I have?",
                "Show me all invoices"
            ));
            return response;
        }
        
        // Calculate metrics
        Map<InvoiceStatus, Long> statusCounts = customerInvoices.stream()
            .collect(Collectors.groupingBy(Invoice::getStatus, Collectors.counting()));
        
        BigDecimal totalInvoiced = customerInvoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = customerInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOutstanding = customerInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.SENT)
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String responseMessage = String.format(
            "Invoices for %s (%s):\n\n" +
            "Total Invoices: %d\n" +
            "- Draft: %d\n" +
            "- Sent: %d\n" +
            "- Paid: %d\n" +
            "- Cancelled: %d\n\n" +
            "Financial Summary:\n" +
            "- Total Invoiced: $%.2f\n" +
            "- Total Paid: $%.2f\n" +
            "- Outstanding Balance: $%.2f",
            customer.getBusinessName(),
            customer.getEmail(),
            customerInvoices.size(),
            statusCounts.getOrDefault(InvoiceStatus.DRAFT, 0L),
            statusCounts.getOrDefault(InvoiceStatus.SENT, 0L),
            statusCounts.getOrDefault(InvoiceStatus.PAID, 0L),
            statusCounts.getOrDefault(InvoiceStatus.CANCELLED, 0L),
            totalInvoiced,
            totalPaid,
            totalOutstanding
        );
        
        // Add recent invoices if not too many
        if (customerInvoices.size() <= 5) {
            responseMessage += "\n\nInvoices:";
            for (Invoice invoice : customerInvoices) {
                responseMessage += String.format(
                    "\n- %s: $%.2f (%s)",
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount(),
                    invoice.getStatus()
                );
            }
        }
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            String.format("Give me a summary for %s", customer.getBusinessName()),
            "Show me overdue invoices",
            "What's my total revenue?"
        ));
        
        return response;
    }
    
    /**
     * Handle queries about customer summary.
     */
    private ChatMessageResponse handleCustomerSummaryQuery(String message, ChatMessageResponse response) {
        // Extract customer name from query
        String customerIdentifier = extractCustomerIdentifier(message);
        
        if (customerIdentifier == null || customerIdentifier.isEmpty()) {
            response.setResponse("I couldn't identify which customer you're asking about. Please try again with the customer's business name or email.");
            response.setSuggestions(Arrays.asList(
                "How many customers do I have?",
                "Show me overdue invoices",
                "What can you do?"
            ));
            return response;
        }
        
        // Find customer
        com.invoiceme.domain.customer.Customer customer = findCustomerByIdentifier(customerIdentifier);
        
        if (customer == null) {
            response.setResponse(String.format(
                "I couldn't find a customer matching '%s'. Please check the name or try using their email address.",
                customerIdentifier
            ));
            response.setSuggestions(Arrays.asList(
                "How many customers do I have?",
                "Show me all customers",
                "What can you do?"
            ));
            return response;
        }
        
        // Get comprehensive customer data
        List<Invoice> customerInvoices = invoiceRepository.findByCustomerId(customer.getId());
        List<Payment> customerPayments = new ArrayList<>();
        for (Invoice invoice : customerInvoices) {
            customerPayments.addAll(paymentRepository.findByInvoiceId(invoice.getId()));
        }
        
        // Calculate detailed metrics
        long overdueCount = customerInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.SENT && 
                          inv.getDueDate().isBefore(LocalDate.now()))
            .count();
        
        BigDecimal totalInvoiced = customerInvoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = customerInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOutstanding = customerInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.SENT)
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Find latest invoice and payment
        Optional<Invoice> latestInvoice = customerInvoices.stream()
            .max(Comparator.comparing(Invoice::getCreatedAt));
        
        Optional<Payment> latestPayment = customerPayments.stream()
            .max(Comparator.comparing(Payment::getPaymentDate));
        
        String responseMessage = String.format(
            "Customer Summary: %s\n\n" +
            "Contact Information:\n" +
            "- Email: %s\n" +
            "- Phone: %s\n" +
            "- Status: %s\n\n" +
            "Invoice Summary:\n" +
            "- Total Invoices: %d\n" +
            "- Overdue Invoices: %d\n" +
            "- Total Invoiced: $%.2f\n" +
            "- Total Paid: $%.2f\n" +
            "- Outstanding Balance: $%.2f\n\n" +
            "Recent Activity:\n" +
            "- Latest Invoice: %s\n" +
            "- Latest Payment: %s",
            customer.getBusinessName(),
            customer.getEmail(),
            customer.getPhone() != null ? customer.getPhone() : "Not provided",
            customer.isActive() ? "Active" : "Inactive",
            customerInvoices.size(),
            overdueCount,
            totalInvoiced,
            totalPaid,
            totalOutstanding,
            latestInvoice.map(inv ->
                String.format("%s on %s", inv.getInvoiceNumber(), inv.getCreatedAt().toLocalDate()))
                .orElse("None"),
            latestPayment.map(pmt ->
                String.format("$%.2f on %s", pmt.getPaymentAmount(), pmt.getPaymentDate()))
                .orElse("None")
        );
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            String.format("Show me invoices for %s", customer.getBusinessName()),
            "Show me overdue invoices",
            "How many customers do I have?"
        ));
        
        return response;
    }
    
    /**
     * Handle queries about invoice statistics.
     */
    private ChatMessageResponse handleInvoiceStatisticsQuery(String message, ChatMessageResponse response) {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        // Check for time filtering
        boolean isThisMonth = message.contains("this month") || message.contains("current month");
        boolean isThisYear = message.contains("this year") || message.contains("current year");
        
        List<Invoice> filteredInvoices = allInvoices;
        String period = " (all time)";
        
        if (isThisMonth) {
            YearMonth currentMonth = YearMonth.now();
            filteredInvoices = allInvoices.stream()
                .filter(inv -> YearMonth.from(inv.getCreatedAt()).equals(currentMonth))
                .collect(Collectors.toList());
            period = " this month";
        } else if (isThisYear) {
            int currentYear = LocalDate.now().getYear();
            filteredInvoices = allInvoices.stream()
                .filter(inv -> inv.getCreatedAt().getYear() == currentYear)
                .collect(Collectors.toList());
            period = " this year";
        }
        
        if (filteredInvoices.isEmpty()) {
            response.setResponse(String.format("No invoices found%s to calculate statistics.", period));
            response.setSuggestions(Arrays.asList(
                "Create a new invoice",
                "What can you do?",
                "Show me all invoices"
            ));
            return response;
        }
        
        // Calculate average invoice amount
        BigDecimal totalAmount = filteredInvoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageAmount = filteredInvoices.isEmpty() ? 
            BigDecimal.ZERO : 
            totalAmount.divide(BigDecimal.valueOf(filteredInvoices.size()), 2, RoundingMode.HALF_UP);
        
        // Calculate average days to payment for paid invoices in period
        List<Invoice> filteredPaidInvoices = filteredInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID && inv.getPaidAt() != null)
            .collect(Collectors.toList());
        
        double averageDaysToPayment = 0;
        if (!filteredPaidInvoices.isEmpty()) {
            long totalDays = filteredPaidInvoices.stream()
                .mapToLong(inv -> java.time.temporal.ChronoUnit.DAYS.between(
                    inv.getCreatedAt().toLocalDate(), 
                    inv.getPaidAt().toLocalDate()
                ))
                .sum();
            averageDaysToPayment = (double) totalDays / filteredPaidInvoices.size();
        }
        
        // Calculate payment rate
        long paidCount = filteredInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
            .count();
        
        long totalNonDraftCount = filteredInvoices.stream()
            .filter(inv -> inv.getStatus() != InvoiceStatus.DRAFT)
            .count();
        
        double paymentRate = totalNonDraftCount > 0 ? 
            ((double) paidCount / totalNonDraftCount) * 100 : 0;
        
        String responseMessage = String.format(
            "Invoice Statistics%s:\n\n" +
            "Overview:\n" +
            "- Total Invoices: %d\n" +
            "- Paid Invoices: %d\n" +
            "- Payment Rate: %.1f%%\n\n" +
            "Financial Metrics:\n" +
            "- Average Invoice Amount: $%.2f\n" +
            "- Total Value: $%.2f\n\n" +
            "Performance:\n" +
            "- Average Days to Payment: %.1f days",
            period,
            filteredInvoices.size(),
            paidCount,
            paymentRate,
            averageAmount,
            totalAmount,
            averageDaysToPayment
        );
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            "What's my total revenue?",
            "Show me overdue invoices",
            "How many customers do I have?"
        ));
        
        return response;
    }
    
    /**
     * Handle queries about a specific invoice.
     */
    private ChatMessageResponse handleInvoiceLookupQuery(String message, ChatMessageResponse response) {
        // Extract invoice number from message
        Pattern pattern = Pattern.compile("(INV-\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message.toUpperCase());
        
        String invoiceNumber = null;
        if (matcher.find()) {
            invoiceNumber = matcher.group(1);
        }
        
        if (invoiceNumber == null) {
            response.setResponse("I couldn't find an invoice number in your message. Please include the invoice number (e.g., INV-001).");
            response.setSuggestions(Arrays.asList(
                "Show me all invoices",
                "Show me overdue invoices",
                "What can you do?"
            ));
            return response;
        }
        
        // Find the invoice
        String finalInvoiceNumber = invoiceNumber;
        Optional<Invoice> invoiceOpt = invoiceRepository.findAll().stream()
            .filter(inv -> inv.getInvoiceNumber().equalsIgnoreCase(finalInvoiceNumber))
            .findFirst();
        
        if (invoiceOpt.isEmpty()) {
            response.setResponse(String.format(
                "Invoice %s not found. Please check the invoice number and try again.",
                invoiceNumber
            ));
            response.setSuggestions(Arrays.asList(
                "Show me all invoices",
                "Show me overdue invoices",
                "What can you do?"
            ));
            return response;
        }

        Invoice invoice = invoiceOpt.orElseThrow(() ->
            new IllegalStateException("Invoice should exist but was not found"));
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        
        String statusInfo = String.format("Status: %s", invoice.getStatus());
        if (invoice.getStatus() == InvoiceStatus.SENT && invoice.getDueDate().isBefore(LocalDate.now())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
            statusInfo += String.format(" (OVERDUE by %d days)", daysOverdue);
        }
        
        String responseMessage = String.format(
            "Invoice Details: %s\n\n" +
            "Customer:\n" +
            "- %s\n" +
            "- %s\n\n" +
            "Financial Information:\n" +
            "- Total Amount: $%.2f\n" +
            "- Amount Paid: $%.2f\n" +
            "- Balance Remaining: $%.2f\n\n" +
            "%s\n" +
            "- Issue Date: %s\n" +
            "- Due Date: %s\n" +
            "%s",
            invoice.getInvoiceNumber(),
            invoice.getCustomer().getBusinessName(),
            invoice.getCustomer().getEmail(),
            invoice.getTotalAmount(),
            invoice.getAmountPaid(),
            invoice.getBalanceRemaining(),
            statusInfo,
            invoice.getIssueDate(),
            invoice.getDueDate(),
            invoice.getPaidAt() != null ? String.format("- Paid On: %s", invoice.getPaidAt().toLocalDate()) : ""
        );
        
        // Add payment history if exists
        if (!payments.isEmpty()) {
            responseMessage += "\n\nPayment History:";
            for (Payment payment : payments) {
                responseMessage += String.format(
                    "\n- $%.2f on %s (%s)",
                    payment.getPaymentAmount(),
                    payment.getPaymentDate(),
                    payment.getPaymentMethod()
                );
            }
        }
        
        response.setResponse(responseMessage);
        response.setSuggestions(Arrays.asList(
            String.format("Show me invoices for %s", invoice.getCustomer().getBusinessName()),
            "Show me overdue invoices",
            "What's my total revenue?"
        ));
        
        return response;
    }
    
    /**
     * Extract customer identifier from message.
     */
    private String extractCustomerIdentifier(String message) {
        // Try to extract after common prepositions
        Pattern pattern = Pattern.compile("(?:for|from|by|of|about)\\s+([a-zA-Z0-9\\s@\\.]+?)(?:\\s+\\?|\\s*$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    /**
     * Find customer by business name or email.
     */
    private com.invoiceme.domain.customer.Customer findCustomerByIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }
        
        List<com.invoiceme.domain.customer.Customer> allCustomers = customerRepository.findAll();
        
        // Try exact match first (case-insensitive)
        for (com.invoiceme.domain.customer.Customer customer : allCustomers) {
            if (customer.getBusinessName().equalsIgnoreCase(identifier) || 
                customer.getEmail().equalsIgnoreCase(identifier)) {
                return customer;
            }
        }
        
        // Try partial match
        for (com.invoiceme.domain.customer.Customer customer : allCustomers) {
            if (customer.getBusinessName().toLowerCase().contains(identifier.toLowerCase()) ||
                customer.getEmail().toLowerCase().contains(identifier.toLowerCase())) {
                return customer;
            }
        }
        
        return null;
    }

    /**
     * Check if message matches a pattern (case-insensitive).
     */
    private boolean matchesPattern(String message, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(message);
        return m.find();
    }
}
