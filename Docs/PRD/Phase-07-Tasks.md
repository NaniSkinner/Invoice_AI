# Phase 7: AI Chat Assistant

**Time Estimate:** 8-10 hours
**Status:** Not Started
**Prerequisites:** Phases 3-5 (Customer, Invoice, Payment) completed

---

## What You'll Build

- 7 OpenAI function definitions for data queries
- Backend query functions for chat assistant
- **ProcessChatQuery** command with function calling
- Function calling orchestration
- Chat REST endpoint
- Conversation context management
- Natural language invoice queries
- Customer lookup by name
- Payment history queries
- Unit tests with function calling mocks
- Integration test for chat flow

---

## Task 7.1: Define Chat Functions

### Step 7.1.1: Create Function Definitions

```bash
mkdir -p src/main/java/com/invoiceme/infrastructure/ai/functions

cat > src/main/java/com/invoiceme/infrastructure/ai/functions/ChatFunctions.java << 'EOF'
package com.invoiceme.infrastructure.ai.functions;

import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFunctions {

    public static List<ChatFunction> getAllFunctions() {
        return Arrays.asList(
            getInvoiceById(),
            getInvoicesByCustomer(),
            getOverdueInvoices(),
            getCustomerByName(),
            getPaymentHistory(),
            getInvoicesByStatus(),
            getTotalRevenue()
        );
    }

    private static ChatFunction getInvoiceById() {
        return ChatFunction.builder()
            .name("get_invoice_by_id")
            .description("Retrieves detailed information about a specific invoice by its ID or invoice number")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(Map.of(
                    "invoice_id", Map.of(
                        "type", "string",
                        "description", "The UUID or invoice number (e.g., INV-000001)"
                    )
                ))
                .required(List.of("invoice_id"))
                .build())
            .build();
    }

    private static ChatFunction getInvoicesByCustomer() {
        return ChatFunction.builder()
            .name("get_invoices_by_customer")
            .description("Retrieves all invoices for a specific customer")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(Map.of(
                    "customer_id", Map.of(
                        "type", "string",
                        "description", "The UUID of the customer"
                    )
                ))
                .required(List.of("customer_id"))
                .build())
            .build();
    }

    private static ChatFunction getOverdueInvoices() {
        return ChatFunction.builder()
            .name("get_overdue_invoices")
            .description("Retrieves all invoices that are currently overdue")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(new HashMap<>())
                .build())
            .build();
    }

    private static ChatFunction getCustomerByName() {
        return ChatFunction.builder()
            .name("get_customer_by_name")
            .description("Finds a customer by their business name or contact name")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(Map.of(
                    "name", Map.of(
                        "type", "string",
                        "description", "The business name or contact name to search for"
                    )
                ))
                .required(List.of("name"))
                .build())
            .build();
    }

    private static ChatFunction getPaymentHistory() {
        return ChatFunction.builder()
            .name("get_payment_history")
            .description("Retrieves payment history for a specific invoice")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(Map.of(
                    "invoice_id", Map.of(
                        "type", "string",
                        "description", "The UUID or invoice number of the invoice"
                    )
                ))
                .required(List.of("invoice_id"))
                .build())
            .build();
    }

    private static ChatFunction getInvoicesByStatus() {
        return ChatFunction.builder()
            .name("get_invoices_by_status")
            .description("Retrieves all invoices with a specific status")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(Map.of(
                    "status", Map.of(
                        "type", "string",
                        "description", "The invoice status: DRAFT, SENT, PAID, or CANCELLED",
                        "enum", List.of("DRAFT", "SENT", "PAID", "CANCELLED")
                    )
                ))
                .required(List.of("status"))
                .build())
            .build();
    }

    private static ChatFunction getTotalRevenue() {
        return ChatFunction.builder()
            .name("get_total_revenue")
            .description("Calculates total revenue from all paid invoices")
            .parameters(ChatFunctionParameter.builder()
                .type("object")
                .properties(new HashMap<>())
                .build())
            .build();
    }
}
EOF
```

---

## Task 7.2: Create Function Executor

### Step 7.2.1: Create ChatFunctionExecutor

```bash
cat > src/main/java/com/invoiceme/infrastructure/ai/functions/ChatFunctionExecutor.java << 'EOF'
package com.invoiceme.infrastructure.ai.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceme.application.customers.GetCustomer.GetCustomerHandler;
import com.invoiceme.application.invoices.GetInvoice.GetInvoiceHandler;
import com.invoiceme.application.invoices.GetInvoice.InvoiceDto;
import com.invoiceme.application.invoices.GetOverdueInvoices.GetOverdueInvoicesHandler;
import com.invoiceme.application.invoices.ListInvoices.ListInvoicesHandler;
import com.invoiceme.application.payments.GetPaymentsByInvoice.GetPaymentsByInvoiceHandler;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatFunctionExecutor {

    private final GetInvoiceHandler getInvoiceHandler;
    private final ListInvoicesHandler listInvoicesHandler;
    private final GetOverdueInvoicesHandler getOverdueInvoicesHandler;
    private final GetCustomerHandler getCustomerHandler;
    private final GetPaymentsByInvoiceHandler getPaymentsByInvoiceHandler;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final ObjectMapper objectMapper;

    public String executeFunction(String functionName, String argumentsJson) {
        try {
            log.info("Executing function: {} with args: {}", functionName, argumentsJson);

            JsonNode args = objectMapper.readTree(argumentsJson);

            return switch (functionName) {
                case "get_invoice_by_id" -> getInvoiceById(args);
                case "get_invoices_by_customer" -> getInvoicesByCustomer(args);
                case "get_overdue_invoices" -> getOverdueInvoices();
                case "get_customer_by_name" -> getCustomerByName(args);
                case "get_payment_history" -> getPaymentHistory(args);
                case "get_invoices_by_status" -> getInvoicesByStatus(args);
                case "get_total_revenue" -> getTotalRevenue();
                default -> "{\"error\": \"Unknown function: " + functionName + "\"}";
            };
        } catch (Exception e) {
            log.error("Error executing function: " + functionName, e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getInvoiceById(JsonNode args) throws Exception {
        String invoiceIdStr = args.get("invoice_id").asText();

        // Try as UUID first
        try {
            UUID invoiceId = UUID.fromString(invoiceIdStr);
            InvoiceDto invoice = getInvoiceHandler.handle(invoiceId);
            return objectMapper.writeValueAsString(invoice);
        } catch (IllegalArgumentException e) {
            // Try as invoice number
            var invoice = invoiceRepository.findByInvoiceNumber(invoiceIdStr)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Invoice not found: " + invoiceIdStr
                ));
            InvoiceDto dto = getInvoiceHandler.handle(invoice.getId());
            return objectMapper.writeValueAsString(dto);
        }
    }

    private String getInvoicesByCustomer(JsonNode args) throws Exception {
        UUID customerId = UUID.fromString(args.get("customer_id").asText());
        List<InvoiceDto> invoices = listInvoicesHandler.handleByCustomer(customerId);
        return objectMapper.writeValueAsString(invoices);
    }

    private String getOverdueInvoices() throws Exception {
        List<InvoiceDto> invoices = getOverdueInvoicesHandler.handle();
        return objectMapper.writeValueAsString(invoices);
    }

    private String getCustomerByName(JsonNode args) throws Exception {
        String name = args.get("name").asText();
        var customers = customerRepository.findByBusinessNameContainingIgnoreCase(name);

        if (customers.isEmpty()) {
            customers = customerRepository.findByContactNameContainingIgnoreCase(name);
        }

        return objectMapper.writeValueAsString(customers);
    }

    private String getPaymentHistory(JsonNode args) throws Exception {
        String invoiceIdStr = args.get("invoice_id").asText();

        UUID invoiceId;
        try {
            invoiceId = UUID.fromString(invoiceIdStr);
        } catch (IllegalArgumentException e) {
            var invoice = invoiceRepository.findByInvoiceNumber(invoiceIdStr)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Invoice not found: " + invoiceIdStr
                ));
            invoiceId = invoice.getId();
        }

        var history = getPaymentsByInvoiceHandler.handle(invoiceId);
        return objectMapper.writeValueAsString(history);
    }

    private String getInvoicesByStatus(JsonNode args) throws Exception {
        String statusStr = args.get("status").asText();
        InvoiceStatus status = InvoiceStatus.valueOf(statusStr);
        List<InvoiceDto> invoices = listInvoicesHandler.handleByStatus(status);
        return objectMapper.writeValueAsString(invoices);
    }

    private String getTotalRevenue() throws Exception {
        List<InvoiceDto> paidInvoices = listInvoicesHandler
            .handleByStatus(InvoiceStatus.PAID);

        BigDecimal total = paidInvoices.stream()
            .map(InvoiceDto::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return objectMapper.writeValueAsString(Map.of(
            "totalRevenue", total,
            "paidInvoiceCount", paidInvoices.size()
        ));
    }
}
EOF
```

### Step 7.2.2: Add Custom Repository Methods

```bash
# Update CustomerRepository
cat > src/main/java/com/invoiceme/infrastructure/persistence/CustomerRepository.java << 'EOF'
package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmail(String email);
    List<Customer> findByBusinessNameContainingIgnoreCase(String businessName);
    List<Customer> findByContactNameContainingIgnoreCase(String contactName);
}
EOF
```

```bash
# Update InvoiceRepository
cat > src/main/java/com/invoiceme/infrastructure/persistence/InvoiceRepository.java << 'EOF'
package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByCustomerId(UUID customerId);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceNumber.value = :invoiceNumber")
    Optional<Invoice> findByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);
}
EOF
```

---

## Task 7.3: Create ChatService

### Step 7.3.1: Create ChatService

```bash
cat > src/main/java/com/invoiceme/infrastructure/ai/ChatService.java << 'EOF'
package com.invoiceme.infrastructure.ai;

import com.invoiceme.infrastructure.ai.functions.ChatFunctionExecutor;
import com.invoiceme.infrastructure.ai.functions.ChatFunctions;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final OpenAiService openAiService;
    private final ChatFunctionExecutor functionExecutor;

    @Value("${openai.model}")
    private String model;

    private static final int MAX_FUNCTION_CALLS = 5;

    public String processQuery(String userMessage, List<ChatMessage> conversationHistory) {
        List<ChatMessage> messages = new ArrayList<>(conversationHistory);
        messages.add(new ChatMessage("user", userMessage));

        int functionCallCount = 0;

        while (functionCallCount < MAX_FUNCTION_CALLS) {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .functions(ChatFunctions.getAllFunctions())
                .functionCall("auto")
                .temperature(0.7)
                .maxTokens(1000)
                .build();

            var response = openAiService.createChatCompletion(request);
            var choice = response.getChoices().get(0);
            var assistantMessage = choice.getMessage();

            messages.add(assistantMessage);

            // Check if function call is needed
            ChatFunctionCall functionCall = assistantMessage.getFunctionCall();
            if (functionCall == null) {
                // No function call, return final answer
                return assistantMessage.getContent();
            }

            // Execute function
            functionCallCount++;
            log.info("Executing function: {} (call {}/{})",
                functionCall.getName(), functionCallCount, MAX_FUNCTION_CALLS);

            String functionResult = functionExecutor.executeFunction(
                functionCall.getName(),
                functionCall.getArguments()
            );

            // Add function result to conversation
            ChatMessage functionMessage = new ChatMessage(
                "function",
                functionResult
            );
            functionMessage.setName(functionCall.getName());
            messages.add(functionMessage);
        }

        // If we hit max function calls, return what we have
        log.warn("Hit max function calls limit");
        return "I apologize, but I needed to make too many lookups. " +
               "Could you rephrase your question to be more specific?";
    }

    public List<ChatMessage> createSystemContext() {
        List<ChatMessage> messages = new ArrayList<>();

        String systemPrompt = """
            You are a helpful AI assistant for the InvoiceMe billing system.
            Your role is to help users query and understand their invoices,
            customers, and payments.

            You have access to functions that can:
            1. Look up invoices by ID or number
            2. Find invoices for a specific customer
            3. List overdue invoices
            4. Search for customers by name
            5. Get payment history for an invoice
            6. Filter invoices by status (DRAFT, SENT, PAID, CANCELLED)
            7. Calculate total revenue from paid invoices

            Guidelines:
            - Be concise and professional
            - Use the functions to get accurate data
            - Format currency amounts with $ symbol
            - Format dates in a readable format
            - If asked about multiple items, summarize the results
            - Suggest helpful follow-up questions
            - If you need clarification, ask the user

            When presenting invoice data:
            - Show invoice number, customer name, amount, and status
            - Highlight overdue invoices
            - Calculate totals when showing multiple invoices
            """;

        messages.add(new ChatMessage("system", systemPrompt));
        return messages;
    }
}
EOF
```

---

## Task 7.4: Implement ProcessChatQuery Command

### Step 7.4.1: Create Application Structure

```bash
mkdir -p src/main/java/com/invoiceme/application/ai/ProcessChatQuery
```

### Step 7.4.2: Create ProcessChatQueryCommand

```bash
cat > src/main/java/com/invoiceme/application/ai/ProcessChatQuery/ProcessChatQueryCommand.java << 'EOF'
package com.invoiceme.application.ai.ProcessChatQuery;

import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public record ProcessChatQueryCommand(
    String message,
    List<ChatMessage> conversationHistory
) {}
EOF
```

### Step 7.4.3: Create ChatResponseDto

```bash
cat > src/main/java/com/invoiceme/application/ai/ProcessChatQuery/ChatResponseDto.java << 'EOF'
package com.invoiceme.application.ai.ProcessChatQuery;

public record ChatResponseDto(String response) {}
EOF
```

### Step 7.4.4: Create ProcessChatQueryHandler

```bash
cat > src/main/java/com/invoiceme/application/ai/ProcessChatQuery/ProcessChatQueryHandler.java << 'EOF'
package com.invoiceme.application.ai.ProcessChatQuery;

import com.invoiceme.infrastructure.ai.ChatService;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessChatQueryHandler {

    private final ChatService chatService;

    public ChatResponseDto handle(ProcessChatQueryCommand command) {
        List<ChatMessage> context = command.conversationHistory() != null
            ? command.conversationHistory()
            : chatService.createSystemContext();

        String response = chatService.processQuery(command.message(), context);

        return new ChatResponseDto(response);
    }
}
EOF
```

---

## Task 7.5: Update AIController

### Step 7.5.1: Add Chat Endpoint

```bash
cat > src/main/java/com/invoiceme/api/AIController.java << 'EOF'
package com.invoiceme.api;

import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderCommand;
import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderHandler;
import com.invoiceme.application.ai.ProcessChatQuery.ChatResponseDto;
import com.invoiceme.application.ai.ProcessChatQuery.ProcessChatQueryCommand;
import com.invoiceme.application.ai.ProcessChatQuery.ProcessChatQueryHandler;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GenerateEmailReminderHandler reminderHandler;
    private final ProcessChatQueryHandler chatQueryHandler;

    @PostMapping("/invoices/{invoiceId}/send-reminder")
    public ResponseEntity<Void> sendReminder(@PathVariable UUID invoiceId) {
        GenerateEmailReminderCommand command =
            new GenerateEmailReminderCommand(invoiceId);
        reminderHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(
        @RequestBody Map<String, Object> request
    ) {
        String message = (String) request.get("message");
        @SuppressWarnings("unchecked")
        List<ChatMessage> history = (List<ChatMessage>) request.get("history");

        ProcessChatQueryCommand command = new ProcessChatQueryCommand(
            message,
            history
        );

        ChatResponseDto response = chatQueryHandler.handle(command);
        return ResponseEntity.ok(response);
    }
}
EOF
```

---

## Task 7.6: Build and Run

### Step 7.6.1: Clean Build

```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
./mvnw clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 22.567 s
```

### Step 7.6.2: Run Application

```bash
export OPENAI_API_KEY=sk-your-actual-key
./mvnw spring-boot:run
```

---

## Task 7.7: Test with curl

### Step 7.7.1: Create Test Data

```bash
# Create customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "businessName": "Acme Corporation",
    "contactName": "John Smith",
    "email": "john@acme.com",
    "phone": "555-1111",
    "address": {
      "street": "100 Market St",
      "city": "San Francisco",
      "state": "CA",
      "zipCode": "94111",
      "country": "USA"
    }
  }'

# Create invoice
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "customerId": "CUSTOMER_UUID",
    "issueDate": "2025-01-01",
    "dueDate": "2025-01-31",
    "lineItems": [
      {
        "description": "Website Development",
        "quantity": 1,
        "unitPrice": 5000.00
      }
    ]
  }'

# Send invoice
curl -X POST http://localhost:8080/api/invoices/INVOICE_UUID/send \
  -u demo:password
```

### Step 7.7.2: Test Chat - Find Customer

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "message": "Can you find information about Acme Corporation?"
  }'
```

**Expected Response:**
```json
{
  "response": "I found Acme Corporation in the system. Here are the details:\n\n- Business Name: Acme Corporation\n- Contact: John Smith\n- Email: john@acme.com\n- Phone: 555-1111\n- Address: 100 Market St, San Francisco, CA 94111, USA\n\nWould you like to see their invoices or any other information?"
}
```

### Step 7.7.3: Test Chat - Find Invoice

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "message": "Show me invoice INV-000001"
  }'
```

**Expected Response:**
```json
{
  "response": "Here's the information for Invoice INV-000001:\n\n- Customer: Acme Corporation\n- Amount: $5,000.00\n- Status: SENT\n- Issue Date: January 1, 2025\n- Due Date: January 31, 2025\n- Line Items:\n  - Website Development: 1 × $5,000.00 = $5,000.00\n\nThe invoice has been sent and is awaiting payment. Would you like to see the payment link or payment history?"
}
```

### Step 7.7.4: Test Chat - List Overdue Invoices

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "message": "What invoices are overdue?"
  }'
```

### Step 7.7.5: Test Chat - Calculate Revenue

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "message": "What is our total revenue from paid invoices?"
  }'
```

### Step 7.7.6: Test Chat - Complex Multi-Step Query

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "message": "Find all invoices for Acme Corporation and tell me if any are overdue"
  }'
```

**Expected Response:**
```json
{
  "response": "Acme Corporation has 1 invoice:\n\nINV-000001 - $5,000.00 (SENT) - Due: Jan 31, 2025\n\nGood news! None of their invoices are currently overdue. The invoice INV-000001 is still within its payment window."
}
```

---

## Task 7.8: Write Unit Tests

### Step 7.8.1: Create ChatFunctionExecutorTest

```bash
cat > src/test/java/com/invoiceme/infrastructure/ai/functions/ChatFunctionExecutorTest.java << 'EOF'
package com.invoiceme.infrastructure.ai.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceme.application.customers.GetCustomer.GetCustomerHandler;
import com.invoiceme.application.invoices.GetInvoice.GetInvoiceHandler;
import com.invoiceme.application.invoices.GetInvoice.InvoiceDto;
import com.invoiceme.application.invoices.GetOverdueInvoices.GetOverdueInvoicesHandler;
import com.invoiceme.application.invoices.ListInvoices.ListInvoicesHandler;
import com.invoiceme.application.payments.GetPaymentsByInvoice.GetPaymentsByInvoiceHandler;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatFunctionExecutorTest {

    @Mock
    private GetInvoiceHandler getInvoiceHandler;

    @Mock
    private ListInvoicesHandler listInvoicesHandler;

    @Mock
    private GetOverdueInvoicesHandler getOverdueInvoicesHandler;

    @Mock
    private GetCustomerHandler getCustomerHandler;

    @Mock
    private GetPaymentsByInvoiceHandler getPaymentsByInvoiceHandler;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatFunctionExecutor executor;

    private UUID invoiceId;
    private InvoiceDto invoiceDto;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        invoiceDto = new InvoiceDto(
            invoiceId,
            "INV-000001",
            UUID.randomUUID(),
            "Test Corp",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            InvoiceStatus.SENT,
            BigDecimal.valueOf(1000),
            "/pay/test",
            null, null, null, null,
            List.of()
        );
    }

    @Test
    void executeFunction_GetInvoiceById_ShouldReturnInvoice() throws Exception {
        // Arrange
        when(getInvoiceHandler.handle(invoiceId)).thenReturn(invoiceDto);

        String args = String.format("{\"invoice_id\": \"%s\"}", invoiceId);

        // Act
        String result = executor.executeFunction("get_invoice_by_id", args);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INV-000001"));
        assertTrue(result.contains("Test Corp"));
        verify(getInvoiceHandler).handle(invoiceId);
    }

    @Test
    void executeFunction_GetTotalRevenue_ShouldCalculateCorrectly() throws Exception {
        // Arrange
        List<InvoiceDto> paidInvoices = List.of(
            new InvoiceDto(UUID.randomUUID(), "INV-001", UUID.randomUUID(),
                "Customer A", LocalDate.now(), LocalDate.now(),
                InvoiceStatus.PAID, BigDecimal.valueOf(1000),
                null, null, null, null, null, List.of()),
            new InvoiceDto(UUID.randomUUID(), "INV-002", UUID.randomUUID(),
                "Customer B", LocalDate.now(), LocalDate.now(),
                InvoiceStatus.PAID, BigDecimal.valueOf(2000),
                null, null, null, null, null, List.of())
        );

        when(listInvoicesHandler.handleByStatus(InvoiceStatus.PAID))
            .thenReturn(paidInvoices);

        // Act
        String result = executor.executeFunction("get_total_revenue", "{}");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("3000"));
        assertTrue(result.contains("\"paidInvoiceCount\":2"));
    }

    @Test
    void executeFunction_UnknownFunction_ShouldReturnError() {
        // Act
        String result = executor.executeFunction("unknown_function", "{}");

        // Assert
        assertTrue(result.contains("error"));
        assertTrue(result.contains("Unknown function"));
    }
}
EOF
```

### Step 7.8.2: Run Tests

```bash
./mvnw test
```

**Expected Output:**
```
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

---

## Task 7.9: Git Commit

### Step 7.9.1: Commit Changes

```bash
git add .

git commit -m "$(cat <<'EOF'
Phase 7: AI Chat Assistant

Implemented conversational AI assistant with function calling:
- 7 OpenAI function definitions for data queries
- ChatFunctionExecutor for executing backend queries
- ChatService with function calling orchestration
- ProcessChatQuery command handler
- Natural language customer and invoice lookup
- Multi-step query support with conversation context
- Revenue calculation and reporting
- Payment history queries
- Custom repository methods for name search
- Unit tests with function calling mocks
- Chat endpoint with conversation history support

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## Verification Checklist

After completing all tasks, verify:

- [ ] 7 chat functions defined
- [ ] ChatFunctionExecutor executes all functions
- [ ] ChatService processes queries with function calling
- [ ] ProcessChatQuery handler working
- [ ] Customer search by name works
- [ ] Invoice lookup by number works
- [ ] Overdue invoices query works
- [ ] Revenue calculation works
- [ ] Payment history query works
- [ ] Status filtering works
- [ ] Multi-step queries work
- [ ] Conversation context maintained
- [ ] curl tests successful
- [ ] Unit tests passing
- [ ] Git commit created

---

## Troubleshooting

### Issue: "Unknown function" error
**Solution:** Check function name matches ChatFunctions definitions exactly

### Issue: Function not being called
**Solution:** Verify function description is clear and specific

### Issue: "Too many function calls" error
**Solution:** Increase MAX_FUNCTION_CALLS or rephrase query to be more specific

### Issue: Customer not found by name
**Solution:** Check CustomerRepository has findByBusinessNameContainingIgnoreCase method

### Issue: Invoice not found by number
**Solution:** Verify InvoiceRepository has findByInvoiceNumber method

---

## What's Next?

Continue to [Phase-08-Tasks.md](Phase-08-Tasks.md) for Customer Management UI implementation.

---

**Phase 7 Complete!** ✅

You now have a fully functional AI chat assistant that can answer natural language questions about customers, invoices, and payments using OpenAI function calling.
