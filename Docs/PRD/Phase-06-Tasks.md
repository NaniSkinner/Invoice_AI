# Phase 6: AI Email Reminder System

**Time Estimate:** 6-8 hours
**Status:** Not Started
**Prerequisites:** Phase 4 (Invoice Management) completed

---

## What You'll Build

- OpenAI API client configuration
- **GenerateEmailReminder** command with AI integration
- Scheduled task to check overdue invoices daily
- Email reminder generation using GPT-4o-mini
- Reminder frequency management (prevent spam)
- Mock email sending (console output)
- Unit tests with OpenAI mocking
- Integration test for reminder generation

---

## Task 6.1: Add OpenAI Dependencies

### Step 6.1.1: Update pom.xml

```bash
cd ~/dev/Gauntlet/Invoice_AI/backend

# Add OpenAI dependency to pom.xml
cat > temp_pom_addition.xml << 'EOF'
<!-- Add this inside <dependencies> section -->
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
EOF

# Manual step: Add the dependency above to pom.xml
```

**Manual Action Required:**
Open [pom.xml](../../backend/pom.xml) and add the OpenAI dependency inside the `<dependencies>` section.

### Step 6.1.2: Update application.properties

```bash
cat >> src/main/resources/application.properties << 'EOF'

# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY:your-api-key-here}
openai.model=gpt-4o-mini
EOF
```

### Step 6.1.3: Create .env File for Local Development

```bash
cat > .env << 'EOF'
OPENAI_API_KEY=sk-your-actual-api-key-here
EOF

# Add .env to .gitignore if not already there
echo ".env" >> .gitignore
```

**Action Required:** Replace `sk-your-actual-api-key-here` with your actual OpenAI API key.

---

## Task 6.2: Create OpenAI Service

### Step 6.2.1: Create OpenAI Configuration

```bash
mkdir -p src/main/java/com/invoiceme/infrastructure/ai

cat > src/main/java/com/invoiceme/infrastructure/ai/OpenAIConfig.java << 'EOF'
package com.invoiceme.infrastructure.ai;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(30));
    }
}
EOF
```

### Step 6.2.2: Create EmailReminderService

```bash
cat > src/main/java/com/invoiceme/infrastructure/ai/EmailReminderService.java << 'EOF'
package com.invoiceme.infrastructure.ai;

import com.invoiceme.domain.invoice.Invoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailReminderService {

    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    public String generateReminderEmail(Invoice invoice) {
        long daysOverdue = ChronoUnit.DAYS.between(
            invoice.getDueDate(),
            LocalDate.now()
        );

        String prompt = buildPrompt(invoice, daysOverdue);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(
                new ChatMessage("system", getSystemPrompt()),
                new ChatMessage("user", prompt)
            ))
            .temperature(0.7)
            .maxTokens(500)
            .build();

        try {
            String emailContent = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();

            log.info("Generated email reminder for invoice: {}",
                invoice.getInvoiceNumber().getValue());

            return emailContent;
        } catch (Exception e) {
            log.error("Error generating email reminder", e);
            throw new RuntimeException(
                "Failed to generate email reminder: " + e.getMessage()
            );
        }
    }

    private String getSystemPrompt() {
        return """
            You are a professional billing assistant helping to draft polite,
            professional payment reminder emails. Your emails should:

            1. Be courteous and professional
            2. Clearly state the overdue amount and invoice number
            3. Include the number of days overdue
            4. Provide a payment link for convenience
            5. Offer to help if there are any issues
            6. Use a friendly but firm tone
            7. Keep the email concise (under 200 words)

            Format the email with:
            - Subject line
            - Greeting
            - Body with key details
            - Clear call-to-action
            - Professional closing
            """;
    }

    private String buildPrompt(Invoice invoice, long daysOverdue) {
        return String.format("""
            Generate a payment reminder email for:

            Customer: %s
            Invoice Number: %s
            Amount Due: $%.2f
            Due Date: %s
            Days Overdue: %d
            Payment Link: %s

            The tone should be %s.
            """,
            invoice.getCustomer().getBusinessName(),
            invoice.getInvoiceNumber().getValue(),
            invoice.getTotalAmount(),
            invoice.getDueDate(),
            daysOverdue,
            invoice.getPaymentLink() != null ?
                "https://invoiceme.com" + invoice.getPaymentLink() :
                "Available upon request",
            getToneForDaysOverdue(daysOverdue)
        );
    }

    private String getToneForDaysOverdue(long daysOverdue) {
        if (daysOverdue <= 7) {
            return "friendly and gentle";
        } else if (daysOverdue <= 30) {
            return "professional and firm";
        } else {
            return "serious and urgent";
        }
    }
}
EOF
```

**Verification:**
```bash
ls -la src/main/java/com/invoiceme/infrastructure/ai/
```

**Expected Output:**
```
-rw-r--r-- OpenAIConfig.java
-rw-r--r-- EmailReminderService.java
```

---

## Task 6.3: Create Email Sending Service (Mock)

### Step 6.3.1: Create EmailService Interface

```bash
mkdir -p src/main/java/com/invoiceme/infrastructure/email

cat > src/main/java/com/invoiceme/infrastructure/email/EmailService.java << 'EOF'
package com.invoiceme.infrastructure.email;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
EOF
```

### Step 6.3.2: Create Mock Email Service Implementation

```bash
cat > src/main/java/com/invoiceme/infrastructure/email/MockEmailService.java << 'EOF'
package com.invoiceme.infrastructure.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockEmailService implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("=".repeat(80));
        log.info("MOCK EMAIL SENT");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("-".repeat(80));
        log.info("Body:\n{}", body);
        log.info("=".repeat(80));

        // In production, this would integrate with SendGrid, AWS SES, etc.
        // For now, we just log to console
    }
}
EOF
```

---

## Task 6.4: Add Reminder Tracking to Invoice

### Step 6.4.1: Update Invoice Entity

```bash
# Read current Invoice.java
cat src/main/java/com/invoiceme/domain/invoice/Invoice.java

# Add these fields to Invoice class (manual edit required)
```

**Manual Action Required:**
Open [Invoice.java](../../backend/src/main/java/com/invoiceme/domain/invoice/Invoice.java) and add these fields:

```java
@Column(name = "last_reminder_sent")
private LocalDateTime lastReminderSent;

@Column(name = "reminder_count")
private Integer reminderCount = 0;
```

### Step 6.4.2: Create Migration for Reminder Fields

```bash
cat > src/main/resources/db/migration/V2__add_reminder_fields.sql << 'EOF'
-- Add reminder tracking fields to invoices table
ALTER TABLE invoices
ADD COLUMN last_reminder_sent TIMESTAMP,
ADD COLUMN reminder_count INTEGER DEFAULT 0;

-- Add index for efficient overdue invoice queries
CREATE INDEX idx_invoices_status_due_date
ON invoices(status, due_date);
EOF
```

---

## Task 6.5: Implement GenerateEmailReminder Command

### Step 6.5.1: Create Application Structure

```bash
mkdir -p src/main/java/com/invoiceme/application/ai/GenerateEmailReminder
```

### Step 6.5.2: Create GenerateEmailReminderCommand

```bash
cat > src/main/java/com/invoiceme/application/ai/GenerateEmailReminder/GenerateEmailReminderCommand.java << 'EOF'
package com.invoiceme.application.ai.GenerateEmailReminder;

import java.util.UUID;

public record GenerateEmailReminderCommand(UUID invoiceId) {}
EOF
```

### Step 6.5.3: Create GenerateEmailReminderHandler

```bash
cat > src/main/java/com/invoiceme/application/ai/GenerateEmailReminder/GenerateEmailReminderHandler.java << 'EOF'
package com.invoiceme.application.ai.GenerateEmailReminder;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.ai.EmailReminderService;
import com.invoiceme.infrastructure.email.EmailService;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateEmailReminderHandler {

    private final InvoiceRepository invoiceRepository;
    private final EmailReminderService emailReminderService;
    private final EmailService emailService;

    private static final int MIN_DAYS_BETWEEN_REMINDERS = 7;

    @Transactional
    public void handle(GenerateEmailReminderCommand command) {
        // Fetch invoice
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Invoice not found: " + command.invoiceId()
            ));

        // Validate invoice is overdue and sent
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            log.warn("Invoice {} is not in SENT status, skipping reminder",
                invoice.getInvoiceNumber().getValue());
            return;
        }

        if (!invoice.getDueDate().isBefore(LocalDate.now())) {
            log.warn("Invoice {} is not overdue yet, skipping reminder",
                invoice.getInvoiceNumber().getValue());
            return;
        }

        // Check if reminder was sent recently
        if (wasReminderSentRecently(invoice)) {
            log.info("Reminder was sent recently for invoice {}, skipping",
                invoice.getInvoiceNumber().getValue());
            return;
        }

        // Generate AI email content
        String emailContent = emailReminderService.generateReminderEmail(invoice);

        // Extract subject line (first line of generated content)
        String subject = extractSubject(emailContent);

        // Send email
        emailService.sendEmail(
            invoice.getCustomer().getEmail(),
            subject,
            emailContent
        );

        // Update reminder tracking
        invoice.setLastReminderSent(LocalDateTime.now());
        invoice.setReminderCount(
            (invoice.getReminderCount() == null ? 0 : invoice.getReminderCount()) + 1
        );
        invoiceRepository.save(invoice);

        log.info("Sent reminder #{} for invoice {}",
            invoice.getReminderCount(),
            invoice.getInvoiceNumber().getValue());
    }

    private boolean wasReminderSentRecently(Invoice invoice) {
        if (invoice.getLastReminderSent() == null) {
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now()
            .minusDays(MIN_DAYS_BETWEEN_REMINDERS);

        return invoice.getLastReminderSent().isAfter(threshold);
    }

    private String extractSubject(String emailContent) {
        // Look for "Subject:" line in generated content
        String[] lines = emailContent.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().startsWith("subject:")) {
                return line.substring(8).trim();
            }
        }
        // Fallback
        return "Payment Reminder";
    }
}
EOF
```

---

## Task 6.6: Create Scheduled Task for Overdue Reminders

### Step 6.6.1: Enable Scheduling

```bash
# Update main application class to enable scheduling
cat > src/main/java/com/invoiceme/InvoiceMeApplication.java << 'EOF'
package com.invoiceme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvoiceMeApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceMeApplication.class, args);
    }
}
EOF
```

### Step 6.6.2: Create Scheduled Task

```bash
mkdir -p src/main/java/com/invoiceme/infrastructure/scheduler

cat > src/main/java/com/invoiceme/infrastructure/scheduler/ReminderScheduler.java << 'EOF'
package com.invoiceme.infrastructure.scheduler;

import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderCommand;
import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderHandler;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final GenerateEmailReminderHandler reminderHandler;

    // Run every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendOverdueReminders() {
        log.info("Starting daily overdue invoice reminder check");

        LocalDate today = LocalDate.now();
        var overdueInvoices = invoiceRepository.findOverdueInvoices(today);

        log.info("Found {} overdue invoices", overdueInvoices.size());

        for (var invoice : overdueInvoices) {
            try {
                reminderHandler.handle(
                    new GenerateEmailReminderCommand(invoice.getId())
                );
            } catch (Exception e) {
                log.error("Failed to send reminder for invoice {}",
                    invoice.getInvoiceNumber().getValue(), e);
                // Continue with next invoice
            }
        }

        log.info("Completed daily overdue invoice reminder check");
    }

    // For testing: run every 2 minutes (comment out in production)
    // @Scheduled(fixedRate = 120000)
    public void sendOverdueRemindersTestMode() {
        log.info("TEST MODE: Checking for overdue invoices");
        sendOverdueReminders();
    }
}
EOF
```

---

## Task 6.7: Create AI Controller

### Step 6.7.1: Create AIController

```bash
cat > src/main/java/com/invoiceme/api/AIController.java << 'EOF'
package com.invoiceme.api;

import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderCommand;
import com.invoiceme.application.ai.GenerateEmailReminder.GenerateEmailReminderHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GenerateEmailReminderHandler reminderHandler;

    @PostMapping("/invoices/{invoiceId}/send-reminder")
    public ResponseEntity<Void> sendReminder(@PathVariable UUID invoiceId) {
        GenerateEmailReminderCommand command =
            new GenerateEmailReminderCommand(invoiceId);
        reminderHandler.handle(command);
        return ResponseEntity.noContent().build();
    }
}
EOF
```

---

## Task 6.8: Build and Run

### Step 6.8.1: Clean Build

```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
./mvnw clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 20.345 s
```

### Step 6.8.2: Run Application

```bash
# Make sure OPENAI_API_KEY is set
export OPENAI_API_KEY=sk-your-actual-key

./mvnw spring-boot:run
```

**Expected Output:**
```
Started InvoiceMeApplication in 4.123 seconds
Scheduled task enabled
```

---

## Task 6.9: Test with curl

### Step 6.9.1: Create Overdue Invoice

```bash
# Create customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "businessName": "Overdue Test Corp",
    "contactName": "Bob Johnson",
    "email": "bob@overduetest.com",
    "phone": "555-7777",
    "address": {
      "street": "789 Pine St",
      "city": "Seattle",
      "state": "WA",
      "zipCode": "98101",
      "country": "USA"
    }
  }'

# Save customerId

# Create invoice with past due date
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "customerId": "CUSTOMER_UUID",
    "issueDate": "2024-12-01",
    "dueDate": "2025-01-01",
    "lineItems": [
      {
        "description": "Overdue Consulting",
        "quantity": 5,
        "unitPrice": 200.00
      }
    ]
  }'

# Save invoiceId
```

### Step 6.9.2: Send Invoice

```bash
curl -X POST http://localhost:8080/api/invoices/INVOICE_UUID/send \
  -u demo:password
```

**Expected Output:**
```json
{"paymentLink": "/pay/some-token"}
```

### Step 6.9.3: Manually Trigger Reminder

```bash
curl -X POST http://localhost:8080/api/ai/invoices/INVOICE_UUID/send-reminder \
  -u demo:password
```

**Expected Output:**
```
HTTP 204 No Content
```

### Step 6.9.4: Check Console for Mock Email Output

**Expected Console Output:**
```
================================================================================
MOCK EMAIL SENT
To: bob@overduetest.com
Subject: Friendly Reminder: Invoice INV-000001 Payment Due
--------------------------------------------------------------------------------
Body:
Subject: Friendly Reminder: Invoice INV-000001 Payment Due

Dear Bob Johnson,

I hope this message finds you well. I'm reaching out regarding Invoice INV-000001
for $1,000.00, which became overdue on January 1, 2025 (7 days ago).

We understand that oversights happen, and we wanted to send a gentle reminder
about this outstanding payment. You can conveniently settle this invoice using
our secure payment link: https://invoiceme.com/pay/some-token

If you've already processed this payment, please disregard this message. If you're
experiencing any issues or have questions about this invoice, please don't hesitate
to reach out. We're here to help!

Thank you for your prompt attention to this matter.

Best regards,
InvoiceMe Billing Team
================================================================================
```

### Step 6.9.5: Verify Reminder Count Updated

```bash
curl -X GET http://localhost:8080/api/invoices/INVOICE_UUID \
  -u demo:password | jq '.reminderCount, .lastReminderSent'
```

**Expected Output:**
```json
1
"2025-01-08T10:30:00"
```

### Step 6.9.6: Test Reminder Frequency Limit

```bash
# Try to send another reminder immediately
curl -X POST http://localhost:8080/api/ai/invoices/INVOICE_UUID/send-reminder \
  -u demo:password
```

**Expected Console Output:**
```
INFO: Reminder was sent recently for invoice INV-000001, skipping
```

---

## Task 6.10: Write Unit Tests

### Step 6.10.1: Create GenerateEmailReminderHandlerTest

```bash
cat > src/test/java/com/invoiceme/application/ai/GenerateEmailReminder/GenerateEmailReminderHandlerTest.java << 'EOF'
package com.invoiceme.application.ai.GenerateEmailReminder;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceNumber;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.ai.EmailReminderService;
import com.invoiceme.infrastructure.email.EmailService;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateEmailReminderHandlerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private EmailReminderService emailReminderService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private GenerateEmailReminderHandler handler;

    private Invoice overdueInvoice;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setBusinessName("Test Corp");
        customer.setEmail("test@example.com");

        overdueInvoice = new Invoice();
        overdueInvoice.setId(UUID.randomUUID());
        overdueInvoice.setInvoiceNumber(new InvoiceNumber("INV-000001"));
        overdueInvoice.setCustomer(customer);
        overdueInvoice.setIssueDate(LocalDate.now().minusDays(40));
        overdueInvoice.setDueDate(LocalDate.now().minusDays(10));
        overdueInvoice.setStatus(InvoiceStatus.SENT);
        overdueInvoice.setTotalAmount(BigDecimal.valueOf(1000));
        overdueInvoice.setPaymentLink("/pay/test-link");
    }

    @Test
    void handle_ShouldSendReminder_ForOverdueInvoice() {
        // Arrange
        String generatedEmail = """
            Subject: Payment Reminder

            Dear Customer, please pay your invoice.
            """;

        when(invoiceRepository.findById(overdueInvoice.getId()))
            .thenReturn(Optional.of(overdueInvoice));
        when(emailReminderService.generateReminderEmail(overdueInvoice))
            .thenReturn(generatedEmail);

        GenerateEmailReminderCommand command =
            new GenerateEmailReminderCommand(overdueInvoice.getId());

        // Act
        handler.handle(command);

        // Assert
        verify(emailService).sendEmail(
            eq("test@example.com"),
            eq("Payment Reminder"),
            eq(generatedEmail)
        );
        verify(invoiceRepository).save(overdueInvoice);
        assertEquals(1, overdueInvoice.getReminderCount());
        assertNotNull(overdueInvoice.getLastReminderSent());
    }

    @Test
    void handle_ShouldSkipReminder_WhenSentRecently() {
        // Arrange
        overdueInvoice.setLastReminderSent(LocalDateTime.now().minusDays(3));
        overdueInvoice.setReminderCount(1);

        when(invoiceRepository.findById(overdueInvoice.getId()))
            .thenReturn(Optional.of(overdueInvoice));

        GenerateEmailReminderCommand command =
            new GenerateEmailReminderCommand(overdueInvoice.getId());

        // Act
        handler.handle(command);

        // Assert
        verify(emailReminderService, never())
            .generateReminderEmail(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
        assertEquals(1, overdueInvoice.getReminderCount()); // Unchanged
    }

    @Test
    void handle_ShouldSkipReminder_WhenInvoiceNotSent() {
        // Arrange
        overdueInvoice.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.findById(overdueInvoice.getId()))
            .thenReturn(Optional.of(overdueInvoice));

        GenerateEmailReminderCommand command =
            new GenerateEmailReminderCommand(overdueInvoice.getId());

        // Act
        handler.handle(command);

        // Assert
        verify(emailReminderService, never())
            .generateReminderEmail(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
EOF
```

### Step 6.10.2: Run Tests

```bash
./mvnw test
```

**Expected Output:**
```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

---

## Task 6.11: Git Commit

### Step 6.11.1: Check Status

```bash
git status
```

### Step 6.11.2: Add All Changes

```bash
git add .
```

### Step 6.11.3: Commit

```bash
git commit -m "$(cat <<'EOF'
Phase 6: AI Email Reminder System

Implemented AI-powered email reminders for overdue invoices:
- OpenAI API integration with GPT-4o-mini
- EmailReminderService with context-aware tone adjustment
- GenerateEmailReminder command handler
- Reminder frequency management (min 7 days between reminders)
- Scheduled daily task to check overdue invoices (9 AM cron)
- Mock email service for testing
- Database migration for reminder tracking fields
- AIController for manual reminder triggering
- Unit tests with OpenAI mocking
- Reminder count and last sent tracking

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## Verification Checklist

After completing all tasks, verify:

- [ ] OpenAI dependency added to pom.xml
- [ ] OpenAI API key configured in .env
- [ ] EmailReminderService generates AI emails
- [ ] Tone adjusts based on days overdue
- [ ] Mock email service logs to console
- [ ] Database migration adds reminder fields
- [ ] GenerateEmailReminder handler working
- [ ] Reminders only sent once per 7 days
- [ ] Scheduled task configured (cron: 9 AM daily)
- [ ] Manual reminder endpoint works
- [ ] Reminder count increments correctly
- [ ] lastReminderSent timestamp updates
- [ ] Unit tests passing
- [ ] Git commit created

---

## Troubleshooting

### Issue: "OpenAI API key not configured"
**Solution:** Set OPENAI_API_KEY environment variable or add to .env file

### Issue: "Failed to generate email reminder"
**Solution:** Check OpenAI API key is valid and has credits

### Issue: Scheduled task not running
**Solution:** Verify @EnableScheduling is on main application class

### Issue: Email not sent but no errors
**Solution:** Check invoice is in SENT status and actually overdue

### Issue: Reminders sent too frequently
**Solution:** Verify MIN_DAYS_BETWEEN_REMINDERS constant (default 7)

---

## What's Next?

Continue to [Phase-07-Tasks.md](Phase-07-Tasks.md) for AI Chat Assistant implementation.

---

**Phase 6 Complete!** ✅

You now have an AI-powered email reminder system that automatically sends professional, context-aware payment reminders for overdue invoices.
