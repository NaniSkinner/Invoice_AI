# Phase 6: AI Integration - Email Reminder System

**Estimated Time:** 6-8 hours
**Dependencies:** Phase 4 (Invoice Management)
**Status:** Not Started

## Overview

Implement AI-powered overdue invoice reminder generation using OpenAI API, complete with scheduled detection, user review workflow, and reminder frequency management.

## Objectives

- Configure OpenAI API client and service layer
- Implement GenerateEmailReminder feature with prompt engineering
- Implement overdue invoice scheduler (daily cron job)
- Implement reminder frequency management and suppression
- Create REST endpoints for reminder generation
- Mock email sending for demo

## AI Features

1. **Automated Detection:** Daily scheduler identifies overdue invoices
2. **AI Generation:** OpenAI generates personalized, professional reminders
3. **Human-in-the-Loop:** User reviews and edits before sending
4. **Frequency Control:** "Remind Me Later" options with suppression
5. **Mock Email:** Demo-friendly email "sending" without actual SMTP

---

## Tasks

### 6.1 Configure OpenAI API Client

**Package:** `com.invoiceme.infrastructure.ai`

**Add OpenAI Dependency (pom.xml):**

```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

**OpenAIConfig.java:**

```java
package com.invoiceme.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(30));
    }

    @Bean
    public String openAiModel() {
        return model;
    }
}
```

**application.properties:**

```properties
# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY}
openai.api.model=gpt-4o-mini

# Scheduling
scheduling.overdue-check.cron=0 0 0 * * *
```

**Action Items:**
- [ ] Add OpenAI dependency
- [ ] Create OpenAIConfig
- [ ] Add environment variables
- [ ] Test OpenAI connection

---

### 6.2 Implement GenerateEmailReminder Feature

**Package:** `com.invoiceme.application.ai.GenerateEmailReminder`

**GenerateEmailReminderCommand.java:**

```java
package com.invoiceme.application.ai.GenerateEmailReminder;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GenerateEmailReminderCommand {
    private UUID invoiceId;
}
```

**EmailReminderDto.java:**

```java
package com.invoiceme.application.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmailReminderDto {
    private UUID invoiceId;
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;
    private String subject;
    private String emailBody;
    private String paymentLink;
}
```

**OpenAIService.java:**

```java
package com.invoiceme.infrastructure.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class OpenAIServiceWrapper {

    private final OpenAiService openAiService;
    private final String openAiModel;

    public String generateEmailReminder(String prompt) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(openAiModel)
            .messages(Arrays.asList(
                new ChatMessage("system",
                    "You are a professional accounts receivable assistant for a small business. " +
                    "Your role is to generate polite, professional, yet friendly email reminders " +
                    "for overdue invoices. The tone should encourage payment without being " +
                    "aggressive or damaging the customer relationship."),
                new ChatMessage("user", prompt)
            ))
            .temperature(0.7)
            .maxTokens(500)
            .build();

        return openAiService.createChatCompletion(request)
            .getChoices()
            .get(0)
            .getMessage()
            .getContent();
    }

    public String generateEmailSubject(String customerName, String invoiceNumber, int daysOverdue) {
        String prompt = String.format(
            "Generate a professional email subject line for an overdue invoice reminder. " +
            "Customer: %s, Invoice: %s, Days Overdue: %d. " +
            "Keep it under 60 characters.",
            customerName, invoiceNumber, daysOverdue
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(openAiModel)
            .messages(Arrays.asList(
                new ChatMessage("system", "You are an expert at writing professional email subject lines."),
                new ChatMessage("user", prompt)
            ))
            .temperature(0.5)
            .maxTokens(50)
            .build();

        return openAiService.createChatCompletion(request)
            .getChoices()
            .get(0)
            .getMessage()
            .getContent()
            .replace("\"", ""); // Remove quotes if present
    }
}
```

**GenerateEmailReminderHandler.java:**

```java
package com.invoiceme.application.ai.GenerateEmailReminder;

import com.invoiceme.application.ai.EmailReminderDto;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.ai.OpenAIServiceWrapper;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class GenerateEmailReminderHandler {

    private final InvoiceRepository invoiceRepository;
    private final OpenAIServiceWrapper openAIService;

    @Transactional(readOnly = true)
    public EmailReminderDto handle(GenerateEmailReminderCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Calculate days overdue
        long daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());

        // Build prompt
        String prompt = buildPrompt(invoice, daysOverdue);

        // Generate email body
        String emailBody = openAIService.generateEmailReminder(prompt);

        // Generate subject line
        String subject = openAIService.generateEmailSubject(
            invoice.getCustomer().getBusinessName(),
            invoice.getInvoiceNumber(),
            (int) daysOverdue
        );

        // Build full payment link URL
        String paymentLink = "http://localhost:3000/pay/" + invoice.getPaymentLink();

        return new EmailReminderDto(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            invoice.getCustomer().getBusinessName(),
            invoice.getCustomer().getEmail(),
            subject,
            emailBody,
            paymentLink
        );
    }

    private String buildPrompt(Invoice invoice, long daysOverdue) {
        StringBuilder lineItemsSummary = new StringBuilder();
        invoice.getLineItems().forEach(item -> {
            lineItemsSummary.append(String.format("- %s (Qty: %.2f @ $%.2f = $%.2f)\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()));
        });

        return String.format(
            "Generate an overdue invoice reminder email with the following details:\n\n" +
            "Customer Name: %s\n" +
            "Invoice Number: %s\n" +
            "Invoice Date: %s\n" +
            "Due Date: %s\n" +
            "Days Overdue: %d\n" +
            "Total Amount: $%.2f\n" +
            "Amount Paid: $%.2f\n" +
            "Balance Remaining: $%.2f\n\n" +
            "Line Items Summary:\n%s\n" +
            "Instructions:\n" +
            "- Use a friendly but professional tone\n" +
            "- Acknowledge the business relationship\n" +
            "- Clearly state the amount owed and how long it's been overdue\n" +
            "- Include a brief summary of what the invoice covers\n" +
            "- Mention that a payment link will be provided\n" +
            "- Offer assistance if there are any questions or issues\n" +
            "- Close with a polite call to action\n" +
            "- Keep the email concise (under 200 words)\n\n" +
            "Generate ONLY the email body, no subject line.",
            invoice.getCustomer().getBusinessName(),
            invoice.getInvoiceNumber(),
            invoice.getIssueDate(),
            invoice.getDueDate(),
            daysOverdue,
            invoice.getTotalAmount(),
            invoice.getAmountPaid(),
            invoice.getBalanceRemaining(),
            lineItemsSummary.toString()
        );
    }
}
```

**Action Items:**
- [ ] Create GenerateEmailReminderCommand
- [ ] Create EmailReminderDto
- [ ] Create OpenAIServiceWrapper
- [ ] Create GenerateEmailReminderHandler
- [ ] Test prompt generation
- [ ] Test OpenAI response parsing

---

### 6.3 Implement Overdue Invoice Scheduler

**Package:** `com.invoiceme.infrastructure.scheduling`

**SchedulingConfig.java:**

```java
package com.invoiceme.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
}
```

**OverdueInvoiceScheduler.java:**

```java
package com.invoiceme.infrastructure.scheduling;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueInvoiceScheduler {

    private final InvoiceRepository invoiceRepository;

    /**
     * Run daily at midnight to identify overdue invoices
     */
    @Scheduled(cron = "${scheduling.overdue-check.cron}")
    public void checkOverdueInvoices() {
        log.info("Starting overdue invoice check...");

        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository
            .findByStatusAndDueDateBefore(InvoiceStatus.SENT, today);

        // Filter invoices that need reminders
        List<Invoice> invoicesNeedingReminders = overdueInvoices.stream()
            .filter(this::shouldSendReminder)
            .collect(Collectors.toList());

        log.info("Found {} overdue invoices, {} need reminders",
            overdueInvoices.size(),
            invoicesNeedingReminders.size());

        // In a real system, this would trigger notifications to the UI
        // For demo, we just log the results
        invoicesNeedingReminders.forEach(invoice -> {
            log.info("Reminder needed for invoice {} (Customer: {}, Days overdue: {})",
                invoice.getInvoiceNumber(),
                invoice.getCustomer().getBusinessName(),
                java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), today));
        });
    }

    private boolean shouldSendReminder(Invoice invoice) {
        // Don't send if reminders are suppressed
        if (invoice.isRemindersSuppressed()) {
            return false;
        }

        LocalDateTime lastReminder = invoice.getLastReminderSentAt();

        // If never sent, send reminder
        if (lastReminder == null) {
            return true;
        }

        // Check if enough time has passed (default: 1 day)
        // In production, this could be configurable per invoice
        LocalDateTime nextReminderDate = lastReminder.plusDays(1);
        return LocalDateTime.now().isAfter(nextReminderDate);
    }
}
```

**Action Items:**
- [ ] Create SchedulingConfig
- [ ] Create OverdueInvoiceScheduler
- [ ] Test scheduler manually (don't wait for cron)
- [ ] Verify overdue detection logic

---

### 6.4 Implement Reminder Frequency Management

**Package:** `com.invoiceme.application.ai.UpdateReminderSettings`

**UpdateReminderSettingsCommand.java:**

```java
package com.invoiceme.application.ai.UpdateReminderSettings;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateReminderSettingsCommand {
    private UUID invoiceId;
    private ReminderAction action;

    public enum ReminderAction {
        SENT,              // Mark reminder as sent now
        REMIND_TOMORROW,   // Remind in 1 day
        REMIND_IN_3_DAYS,  // Remind in 3 days
        REMIND_IN_1_WEEK,  // Remind in 1 week
        SUPPRESS           // Don't remind again
    }
}
```

**UpdateReminderSettingsHandler.java:**

```java
package com.invoiceme.application.ai.UpdateReminderSettings;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateReminderSettingsHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void handle(UpdateReminderSettingsCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        switch (command.getAction()) {
            case SENT:
                invoice.setLastReminderSentAt(LocalDateTime.now());
                break;

            case REMIND_TOMORROW:
                invoice.setLastReminderSentAt(LocalDateTime.now().minusDays(1).plusMinutes(1));
                break;

            case REMIND_IN_3_DAYS:
                invoice.setLastReminderSentAt(LocalDateTime.now().minusDays(3).plusMinutes(1));
                break;

            case REMIND_IN_1_WEEK:
                invoice.setLastReminderSentAt(LocalDateTime.now().minusDays(7).plusMinutes(1));
                break;

            case SUPPRESS:
                invoice.setRemindersSuppressed(true);
                break;
        }

        invoiceRepository.save(invoice);
    }
}
```

**Action Items:**
- [ ] Create UpdateReminderSettingsCommand
- [ ] Create UpdateReminderSettingsHandler
- [ ] Test reminder frequency logic
- [ ] Test suppression flag

---

### 6.5 Create AI REST Controller

**Package:** `com.invoiceme.api`

**AIController.java:**

```java
package com.invoiceme.api;

import com.invoiceme.application.ai.EmailReminderDto;
import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderCommand;
import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderHandler;
import com.invoiceme.application.ai.UpdateReminderSettings.UpdateReminderSettingsCommand;
import com.invoiceme.application.ai.UpdateReminderSettings.UpdateReminderSettingsHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GenerateEmailReminderHandler generateEmailReminderHandler;
    private final UpdateReminderSettingsHandler updateReminderSettingsHandler;

    @PostMapping("/generate-reminder")
    public ResponseEntity<EmailReminderDto> generateReminder(@RequestBody GenerateReminderRequest request) {
        try {
            EmailReminderDto reminder = generateEmailReminderHandler.handle(
                new GenerateEmailReminderCommand(request.getInvoiceId())
            );
            return ResponseEntity.ok(reminder);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/send-reminder")
    public ResponseEntity<SendReminderResponse> sendReminder(@RequestBody SendReminderRequest request) {
        try {
            // Mock email sending for demo
            // In production, this would integrate with Gmail API or SMTP

            // Update reminder sent timestamp
            updateReminderSettingsHandler.handle(
                new UpdateReminderSettingsCommand(
                    request.getInvoiceId(),
                    UpdateReminderSettingsCommand.ReminderAction.SENT
                )
            );

            return ResponseEntity.ok(new SendReminderResponse(
                true,
                "Email sent successfully to " + request.getRecipientEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new SendReminderResponse(
                false,
                "Failed to send email: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/reminder-settings")
    public ResponseEntity<Void> updateReminderSettings(@RequestBody UpdateReminderSettingsRequest request) {
        updateReminderSettingsHandler.handle(
            new UpdateReminderSettingsCommand(
                request.getInvoiceId(),
                request.getAction()
            )
        );
        return ResponseEntity.ok().build();
    }

    @Data
    public static class GenerateReminderRequest {
        private UUID invoiceId;
    }

    @Data
    public static class SendReminderRequest {
        private UUID invoiceId;
        private String recipientEmail;
        private String subject;
        private String body;
    }

    @Data
    @AllArgsConstructor
    public static class SendReminderResponse {
        private boolean success;
        private String message;
    }

    @Data
    public static class UpdateReminderSettingsRequest {
        private UUID invoiceId;
        private UpdateReminderSettingsCommand.ReminderAction action;
    }
}
```

**Action Items:**
- [ ] Create AIController
- [ ] Test generate reminder endpoint
- [ ] Test send reminder endpoint (mock)
- [ ] Test reminder settings endpoint

---

### 6.6 Write Integration Tests

**GenerateEmailReminderIntegrationTest.java:**

```java
package com.invoiceme.application.ai;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GenerateEmailReminderIntegrationTest {

    @Autowired
    private GenerateEmailReminderHandler handler;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldGenerateEmailReminderForOverdueInvoice() {
        // Given: Create customer and overdue invoice
        Customer customer = new Customer();
        customer.setBusinessName("Test Corp");
        customer.setEmail("test@example.com");
        customer = customerRepository.save(customer);

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceNumber("INV-2025-0001");
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setIssueDate(LocalDate.now().minusDays(40));
        invoice.setDueDate(LocalDate.now().minusDays(10)); // 10 days overdue
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setBalanceRemaining(new BigDecimal("100.00"));

        LineItem item = new LineItem();
        item.setDescription("Consulting Services");
        item.setQuantity(new BigDecimal("10"));
        item.setUnitPrice(new BigDecimal("10.00"));
        invoice.addLineItem(item);

        invoice = invoiceRepository.save(invoice);

        // When: Generate reminder
        EmailReminderDto result = handler.handle(
            new GenerateEmailReminderCommand(invoice.getId())
        );

        // Then: Verify email content
        assertNotNull(result);
        assertEquals(invoice.getId(), result.getInvoiceId());
        assertNotNull(result.getSubject());
        assertNotNull(result.getEmailBody());
        assertTrue(result.getEmailBody().contains("Test Corp"));
        assertTrue(result.getEmailBody().contains("INV-2025-0001"));
        assertTrue(result.getEmailBody().contains("100.00"));
    }
}
```

**Action Items:**
- [ ] Write integration test for email generation
- [ ] Write test for scheduler detection
- [ ] Mock OpenAI responses for consistent testing
- [ ] Verify prompt quality with various invoice scenarios

---

## Verification Checklist

After completing Phase 6, verify:

- [ ] OpenAI API client configured and connected
- [ ] Email reminder generation works with real OpenAI calls
- [ ] Generated emails are professional and contextually accurate
- [ ] Overdue invoice scheduler runs and identifies correct invoices
- [ ] Reminder frequency management works (sent, tomorrow, 3 days, week, suppress)
- [ ] REST endpoints functional
- [ ] Mock email sending returns success response
- [ ] Integration tests pass
- [ ] Error handling for OpenAI failures implemented

## API Endpoints Summary

```
POST   /api/ai/generate-reminder      - Generate AI email reminder
POST   /api/ai/send-reminder          - Send reminder (mock)
POST   /api/ai/reminder-settings      - Update reminder frequency
```

## Next Steps

Proceed to [Phase 7: AI Chat Assistant](Phase-07-AI-Chat-Assistant.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 3.1: AI-Assisted Overdue Invoice Reminder)
- OpenAI Prompt Templates: PRD Section 3.1.4
