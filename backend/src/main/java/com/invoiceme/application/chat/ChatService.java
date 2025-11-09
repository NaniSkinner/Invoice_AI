package com.invoiceme.application.chat;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI Chat Assistant Service with template-based NLP.
 * Provides pattern matching for common invoice-related queries without external AI APIs.
 */
@Service
public class ChatService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final Map<String, String> conversationContext;

    public ChatService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.conversationContext = new HashMap<>();
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
        if (matchesPattern(message, "overdue|past due|late")) {
            return handleOverdueInvoicesQuery(response);
        } else if (matchesPattern(message, "revenue|income|earnings|total.*paid")) {
            return handleRevenueQuery(message, response);
        } else if (matchesPattern(message, "draft")) {
            return handleDraftInvoicesQuery(response);
        } else if (matchesPattern(message, "sent|pending|outstanding")) {
            return handleSentInvoicesQuery(response);
        } else if (matchesPattern(message, "customer|client")) {
            return handleCustomerQuery(message, response);
        } else if (matchesPattern(message, "create.*invoice|new invoice")) {
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
            "- Finding overdue invoices\n" +
            "- Calculating revenue (total, this month, this year)\n" +
            "- Viewing draft, sent, or paid invoices\n" +
            "- Checking customer information\n" +
            "- Getting invoice status summaries\n" +
            "- Instructions for creating invoices\n" +
            "- Guidance on sending reminders\n\n" +
            "Just ask me a question in plain English!";

        response.setResponse(message);
        response.setSuggestions(Arrays.asList(
            "Show me overdue invoices",
            "What's my total revenue?",
            "How many draft invoices do I have?"
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
     * Check if message matches a pattern (case-insensitive).
     */
    private boolean matchesPattern(String message, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(message);
        return m.find();
    }
}
