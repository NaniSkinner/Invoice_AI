# Phase 7: AI Chat Assistant - Virtual Accountant

**Estimated Time:** 8-10 hours
**Dependencies:** Phase 5 (Payment Processing)
**Status:** Not Started

## Overview

Implement a conversational AI assistant using OpenAI's function calling feature that allows users to query invoicing data using natural language. The assistant acts as a "Virtual Accountant" with read-only access to business data.

## Objectives

- Define OpenAI function calling schemas for 7 query types
- Implement backend query functions (getOverdueInvoices, getTotalAmountOwed, etc.)
- Implement ProcessChatQuery handler with function calling orchestration
- Create AI Chat REST controller with conversation management
- Write integration tests with mocked OpenAI responses

## Supported Queries (Minimum Viable)

1. **getOverdueInvoices** - Count and details of overdue invoices
2. **getTotalAmountOwed** - Total balance across all sent invoices
3. **getInvoicesByCustomer** - List invoices for specific customer
4. **getInvoicesByStatus** - Count and total by status
5. **getPaymentHistory** - Recent payments with dates
6. **getCustomerSummary** - Customer invoice/payment summary
7. **getInvoiceStatistics** - Aggregated stats by period

---

## Tasks

### 7.1 Define OpenAI Function Calling Schemas

**Package:** `com.invoiceme.infrastructure.ai.functions`

**FunctionDefinitions.java:**

```java
package com.invoiceme.infrastructure.ai.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.ChatFunction;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FunctionDefinitions {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<ChatFunction> getAllFunctions() {
        return Arrays.asList(
            getOverdueInvoicesFunction(),
            getTotalAmountOwedFunction(),
            getInvoicesByCustomerFunction(),
            getInvoicesByStatusFunction(),
            getPaymentHistoryFunction(),
            getCustomerSummaryFunction(),
            getInvoiceStatisticsFunction()
        );
    }

    private ChatFunction getOverdueInvoicesFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();
        ObjectNode monthProperty = mapper.createObjectNode();
        monthProperty.put("type", "string");
        monthProperty.put("description", "Optional month filter in YYYY-MM format, e.g., '2025-11'");
        properties.set("month", monthProperty);

        parameters.set("properties", properties);

        return ChatFunction.builder()
            .name("getOverdueInvoices")
            .description("Get the count and details of overdue invoices, optionally filtered by month")
            .parameters(parameters)
            .build();
    }

    private ChatFunction getTotalAmountOwedFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");
        parameters.set("properties", mapper.createObjectNode());

        return ChatFunction.builder()
            .name("getTotalAmountOwed")
            .description("Get the total balance across all sent invoices")
            .parameters(parameters)
            .build();
    }

    private ChatFunction getInvoicesByCustomerFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();
        ObjectNode customerNameProperty = mapper.createObjectNode();
        customerNameProperty.put("type", "string");
        customerNameProperty.put("description", "Customer business name or contact name");
        properties.set("customerName", customerNameProperty);

        parameters.set("properties", properties);

        ObjectNode required = mapper.createArrayNode().add("customerName");
        parameters.set("required", required);

        return ChatFunction.builder()
            .name("getInvoicesByCustomer")
            .description("List invoices for a specific customer")
            .parameters(parameters)
            .build();
    }

    private ChatFunction getInvoicesByStatusFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();
        ObjectNode statusProperty = mapper.createObjectNode();
        statusProperty.put("type", "string");
        statusProperty.set("enum", mapper.createArrayNode().add("draft").add("sent").add("paid").add("cancelled"));
        statusProperty.put("description", "Invoice status");
        properties.set("status", statusProperty);

        parameters.set("properties", properties);

        ObjectNode required = mapper.createArrayNode().add("status");
        parameters.set("required", required);

        return ChatFunction.builder()
            .name("getInvoicesByStatus")
            .description("Get count and total for invoices by status")
            .parameters(parameters)
            .build();
    }

    private ChatFunction getPaymentHistoryFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();

        ObjectNode invoiceIdProperty = mapper.createObjectNode();
        invoiceIdProperty.put("type", "string");
        invoiceIdProperty.put("description", "Optional invoice UUID");
        properties.set("invoiceId", invoiceIdProperty);

        ObjectNode daysProperty = mapper.createObjectNode();
        daysProperty.put("type", "integer");
        daysProperty.put("description", "Number of days to look back (default 30)");
        properties.set("days", daysProperty);

        parameters.set("properties", properties);

        return ChatFunction.builder()
            .name("getPaymentHistory")
            .description("List recent payments, optionally for a specific invoice")
            .parameters(parameters)
            .build();
    }

    private ChatFunction getCustomerSummaryFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();
        ObjectNode customerIdProperty = mapper.createObjectNode();
        customerIdProperty.put("type", "string");
        customerIdProperty.put("description", "Customer UUID");
        properties.set("customerId", customerIdProperty);

        parameters.set("properties", properties);

        ObjectNode required = mapper.createArrayNode().add("customerId");
        parameters.set("required", required);

        return ChatFunction.builder()
            .name("getCustomerSummary")
            .description("Get summary of invoices and payments for a customer")
            .parameters(parameters)
            .build();
    }

    private ChatFunction getInvoiceStatisticsFunction() {
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();
        ObjectNode periodProperty = mapper.createObjectNode();
        periodProperty.put("type", "string");
        periodProperty.set("enum", mapper.createArrayNode().add("month").add("quarter").add("year"));
        periodProperty.put("description", "Time period for statistics");
        properties.set("period", periodProperty);

        parameters.set("properties", properties);

        return ChatFunction.builder()
            .name("getInvoiceStatistics")
            .description("Get aggregated invoice statistics for a time period")
            .parameters(parameters)
            .build();
    }
}
```

**Action Items:**
- [ ] Create FunctionDefinitions with all 7 functions
- [ ] Validate JSON schema structure
- [ ] Test function definitions with OpenAI

---

### 7.2 Implement Backend Query Functions

**Package:** `com.invoiceme.application.ai.chat`

**ChatQueryService.java:**

```java
package com.invoiceme.application.ai.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatQueryService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String executeFunction(String functionName, JsonNode arguments) {
        try {
            switch (functionName) {
                case "getOverdueInvoices":
                    return getOverdueInvoices(arguments);
                case "getTotalAmountOwed":
                    return getTotalAmountOwed();
                case "getInvoicesByCustomer":
                    return getInvoicesByCustomer(arguments);
                case "getInvoicesByStatus":
                    return getInvoicesByStatus(arguments);
                case "getPaymentHistory":
                    return getPaymentHistory(arguments);
                case "getCustomerSummary":
                    return getCustomerSummary(arguments);
                case "getInvoiceStatistics":
                    return getInvoiceStatistics(arguments);
                default:
                    return "{\"error\": \"Unknown function: " + functionName + "\"}";
            }
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getOverdueInvoices(JsonNode arguments) {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository
            .findByStatusAndDueDateBefore(InvoiceStatus.SENT, today);

        // Apply month filter if provided
        if (arguments.has("month")) {
            String monthStr = arguments.get("month").asText();
            // Filter by month (format: YYYY-MM)
            overdueInvoices = overdueInvoices.stream()
                .filter(inv -> inv.getDueDate().toString().startsWith(monthStr))
                .collect(Collectors.toList());
        }

        BigDecimal totalOverdue = overdueInvoices.stream()
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("count", overdueInvoices.size());
        result.put("totalAmount", totalOverdue);
        result.put("invoices", overdueInvoices.stream()
            .map(inv -> Map.of(
                "invoiceNumber", inv.getInvoiceNumber(),
                "customerName", inv.getCustomer().getBusinessName(),
                "amount", inv.getBalanceRemaining(),
                "daysOverdue", java.time.temporal.ChronoUnit.DAYS.between(inv.getDueDate(), today)
            ))
            .collect(Collectors.toList()));

        return toJson(result);
    }

    private String getTotalAmountOwed() {
        List<Invoice> sentInvoices = invoiceRepository.findByStatus(InvoiceStatus.SENT);

        BigDecimal total = sentInvoices.stream()
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmountOwed", total);
        result.put("invoiceCount", sentInvoices.size());

        return toJson(result);
    }

    private String getInvoicesByCustomer(JsonNode arguments) {
        String customerName = arguments.get("customerName").asText();

        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Invoice> customerInvoices = allInvoices.stream()
            .filter(inv -> inv.getCustomer().getBusinessName().toLowerCase()
                .contains(customerName.toLowerCase()))
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("customerName", customerName);
        result.put("invoiceCount", customerInvoices.size());
        result.put("invoices", customerInvoices.stream()
            .map(inv -> Map.of(
                "invoiceNumber", inv.getInvoiceNumber(),
                "status", inv.getStatus().toString(),
                "totalAmount", inv.getTotalAmount(),
                "balanceRemaining", inv.getBalanceRemaining()
            ))
            .collect(Collectors.toList()));

        return toJson(result);
    }

    private String getInvoicesByStatus(JsonNode arguments) {
        String statusStr = arguments.get("status").asText().toUpperCase();
        InvoiceStatus status = InvoiceStatus.valueOf(statusStr);

        List<Invoice> invoices = invoiceRepository.findByStatus(status);

        BigDecimal totalAmount = invoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("status", status.toString());
        result.put("count", invoices.size());
        result.put("totalAmount", totalAmount);

        return toJson(result);
    }

    private String getPaymentHistory(JsonNode arguments) {
        int days = arguments.has("days") ? arguments.get("days").asInt() : 30;
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<Payment> payments;
        if (arguments.has("invoiceId")) {
            UUID invoiceId = UUID.fromString(arguments.get("invoiceId").asText());
            payments = paymentRepository.findByInvoiceId(invoiceId);
        } else {
            payments = paymentRepository.findAll();
        }

        // Filter by date
        payments = payments.stream()
            .filter(p -> p.getCreatedAt().isAfter(since))
            .collect(Collectors.toList());

        BigDecimal totalPaid = payments.stream()
            .map(Payment::getPaymentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentCount", payments.size());
        result.put("totalPaid", totalPaid);
        result.put("payments", payments.stream()
            .map(p -> Map.of(
                "invoiceNumber", p.getInvoice().getInvoiceNumber(),
                "amount", p.getPaymentAmount(),
                "date", p.getPaymentDate().toString(),
                "method", p.getPaymentMethod().toString()
            ))
            .collect(Collectors.toList()));

        return toJson(result);
    }

    private String getCustomerSummary(JsonNode arguments) {
        UUID customerId = UUID.fromString(arguments.get("customerId").asText());

        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Invoice> invoices = invoiceRepository.findByCustomerId(customerId);

        BigDecimal totalInvoiced = invoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = invoices.stream()
            .map(Invoice::getAmountPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = invoices.stream()
            .map(Invoice::getBalanceRemaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("customerName", customer.getBusinessName());
        result.put("totalInvoices", invoices.size());
        result.put("totalInvoiced", totalInvoiced);
        result.put("totalPaid", totalPaid);
        result.put("totalOutstanding", totalOutstanding);

        return toJson(result);
    }

    private String getInvoiceStatistics(JsonNode arguments) {
        String period = arguments.has("period") ? arguments.get("period").asText() : "month";

        LocalDate startDate;
        switch (period.toLowerCase()) {
            case "month":
                startDate = LocalDate.now().minusMonths(1);
                break;
            case "quarter":
                startDate = LocalDate.now().minusMonths(3);
                break;
            case "year":
                startDate = LocalDate.now().minusYears(1);
                break;
            default:
                startDate = LocalDate.now().minusMonths(1);
        }

        List<Invoice> invoices = invoiceRepository.findAll().stream()
            .filter(inv -> inv.getCreatedAt().toLocalDate().isAfter(startDate))
            .collect(Collectors.toList());

        BigDecimal totalRevenue = invoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgInvoiceAmount = invoices.stream()
            .mapToDouble(inv -> inv.getTotalAmount().doubleValue())
            .average()
            .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("totalInvoices", invoices.size());
        result.put("totalRevenue", totalRevenue);
        result.put("averageInvoiceAmount", avgInvoiceAmount);
        result.put("paidInvoices", invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).count());
        result.put("overdueInvoices", invoices.stream().filter(Invoice::isOverdue).count());

        return toJson(result);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\": \"Failed to serialize result\"}";
        }
    }
}
```

**Action Items:**
- [ ] Create ChatQueryService with all 7 functions
- [ ] Test each function independently
- [ ] Verify JSON response format
- [ ] Handle edge cases (empty results, invalid IDs)

---

### 7.3 Implement ProcessChatQuery Handler

**Package:** `com.invoiceme.application.ai.chat`

**ProcessChatQueryCommand.java:**

```java
package com.invoiceme.application.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProcessChatQueryCommand {
    private String message;
    private List<ChatMessageDto> conversationHistory;

    @Data
    @AllArgsConstructor
    public static class ChatMessageDto {
        private String role; // "user" or "assistant"
        private String content;
    }
}
```

**ChatResponseDto.java:**

```java
package com.invoiceme.application.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponseDto {
    private String message;
    private boolean functionCalled;
    private String functionName;
}
```

**ProcessChatQueryHandler.java:**

```java
package com.invoiceme.application.ai.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceme.infrastructure.ai.functions.FunctionDefinitions;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessChatQueryHandler {

    private final OpenAiService openAiService;
    private final String openAiModel;
    private final FunctionDefinitions functionDefinitions;
    private final ChatQueryService chatQueryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponseDto handle(ProcessChatQueryCommand command) {
        // Build message history
        List<ChatMessage> messages = new ArrayList<>();

        // System message
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
            "You are a helpful virtual accountant assistant. " +
            "You have access to invoice and payment data through function calls. " +
            "Answer questions clearly and concisely. " +
            "When presenting financial data, format amounts with currency symbols."));

        // Add conversation history
        if (command.getConversationHistory() != null) {
            command.getConversationHistory().forEach(msg ->
                messages.add(new ChatMessage(msg.getRole(), msg.getContent()))
            );
        }

        // Add current user message
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), command.getMessage()));

        // Create chat completion request with functions
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(openAiModel)
            .messages(messages)
            .functions(functionDefinitions.getAllFunctions())
            .functionCall("auto")
            .temperature(0.7)
            .build();

        // Call OpenAI
        var response = openAiService.createChatCompletion(request);
        var choice = response.getChoices().get(0);
        var responseMessage = choice.getMessage();

        // Check if function was called
        if (responseMessage.getFunctionCall() != null) {
            String functionName = responseMessage.getFunctionCall().getName();
            String argumentsJson = responseMessage.getFunctionCall().getArguments();

            try {
                // Parse arguments
                JsonNode arguments = objectMapper.readTree(argumentsJson);

                // Execute function
                String functionResult = chatQueryService.executeFunction(functionName, arguments);

                // Send function result back to OpenAI
                messages.add(responseMessage);
                messages.add(new ChatMessage(ChatMessageRole.FUNCTION.value(), functionResult, functionName));

                ChatCompletionRequest followUpRequest = ChatCompletionRequest.builder()
                    .model(openAiModel)
                    .messages(messages)
                    .temperature(0.7)
                    .build();

                var followUpResponse = openAiService.createChatCompletion(followUpRequest);
                String finalMessage = followUpResponse.getChoices().get(0).getMessage().getContent();

                return new ChatResponseDto(finalMessage, true, functionName);

            } catch (Exception e) {
                return new ChatResponseDto(
                    "I encountered an error while retrieving that information: " + e.getMessage(),
                    true,
                    functionName
                );
            }
        } else {
            // No function call, return direct response
            return new ChatResponseDto(responseMessage.getContent(), false, null);
        }
    }
}
```

**Action Items:**
- [ ] Create ProcessChatQueryCommand
- [ ] Create ChatResponseDto
- [ ] Create ProcessChatQueryHandler
- [ ] Test function calling flow
- [ ] Test follow-up questions with context

---

### 7.4 Create AI Chat REST Controller

**ChatController.java:**

```java
package com.invoiceme.api;

import com.invoiceme.application.ai.chat.ChatResponseDto;
import com.invoiceme.application.ai.chat.ProcessChatQueryCommand;
import com.invoiceme.application.ai.chat.ProcessChatQueryHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ProcessChatQueryHandler processChatQueryHandler;

    @PostMapping
    public ResponseEntity<ChatResponseDto> sendMessage(@RequestBody ChatRequest request) {
        try {
            ChatResponseDto response = processChatQueryHandler.handle(
                new ProcessChatQueryCommand(
                    request.getMessage(),
                    request.getHistory()
                )
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponseDto(
                "I'm having trouble processing that request. Please try rephrasing your question.",
                false,
                null
            ));
        }
    }

    @Data
    public static class ChatRequest {
        private String message;
        private List<ProcessChatQueryCommand.ChatMessageDto> history;
    }
}
```

**Action Items:**
- [ ] Create ChatController
- [ ] Test chat endpoint with various queries
- [ ] Test conversation context retention
- [ ] Verify error handling

---

### 7.5 Write Integration Tests

**ChatFunctionCallingTest.java:**

```java
package com.invoiceme.application.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ChatFunctionCallingTest {

    @Autowired
    private ChatQueryService chatQueryService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGetTotalAmountOwed() {
        // When
        String result = chatQueryService.executeFunction("getTotalAmountOwed", mapper.createObjectNode());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("totalAmountOwed"));
    }

    @Test
    void shouldGetInvoicesByStatus() {
        // Given
        ObjectNode args = mapper.createObjectNode();
        args.put("status", "sent");

        // When
        String result = chatQueryService.executeFunction("getInvoicesByStatus", args);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("count"));
        assertTrue(result.contains("totalAmount"));
    }

    @Test
    void shouldHandleInvalidFunction() {
        // When
        String result = chatQueryService.executeFunction("invalidFunction", mapper.createObjectNode());

        // Then
        assertTrue(result.contains("error"));
    }
}
```

**Action Items:**
- [ ] Write tests for all 7 functions
- [ ] Mock OpenAI responses for consistent testing
- [ ] Test error handling for invalid arguments
- [ ] Test conversation context handling
- [ ] Verify security (no write operations possible)

---

## Verification Checklist

After completing Phase 7, verify:

- [ ] All 7 OpenAI function definitions created
- [ ] All 7 backend query functions implemented and tested
- [ ] Function calling orchestration works correctly
- [ ] Chat endpoint handles messages and returns responses
- [ ] Conversation context maintained across messages
- [ ] Follow-up questions work correctly
- [ ] Error handling graceful when AI or functions fail
- [ ] Security verified (read-only, no data modification)
- [ ] Integration tests pass with mocked OpenAI
- [ ] Real OpenAI integration tested manually

## Example Conversation

```
User: "Hi, what's my current outstanding balance?"
AI: "Your current outstanding balance across all sent invoices is $45,230.00."

User: "Which customers owe the most?"
AI: "Here are your top customers by outstanding balance:
1. Acme Corp - $15,000.00 (2 invoices)
2. TechStart Inc - $12,450.00 (1 invoice)
3. Global Widgets - $8,900.00 (3 invoices)"

User: "How many invoices are overdue?"
AI: "You currently have 5 overdue invoices totaling $12,450.00."
```

## API Endpoints Summary

```
POST   /api/ai/chat   - Send chat message with optional conversation history
```

## Next Steps

Proceed to [Phase 8: Frontend - Customer Management UI](Phase-08-Frontend-Customers.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 3.2: AI Chat Assistant)
- OpenAI Function Definitions: PRD Appendix C
