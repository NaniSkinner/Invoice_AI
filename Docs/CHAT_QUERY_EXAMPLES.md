# AI Chat Assistant - Query Examples

## Quick Reference Guide

This document provides example queries you can use with the InvoiceMe AI Chat Assistant.

## Invoice Status Queries

### Overdue Invoices
```
How many overdue invoices do I have?
Show me overdue invoices
Which invoices are past due?
Tell me about late invoices
```

### Draft Invoices
```
Show me all draft invoices
How many draft invoices do I have?
List my draft invoices
What drafts are ready to send?
```

### Sent Invoices
```
Show me sent invoices
How many invoices are outstanding?
What's pending payment?
List all sent invoices
```

### Paid Invoices (NEW!)
```
Show me paid invoices
Show me paid invoices this month
List all paid invoices this year
How many invoices have been paid?
```

### Status Summary
```
How many invoices do I have?
Give me an invoice status summary
Show me invoice counts by status
What's my invoice breakdown?
```

### Specific Invoice Lookup (NEW!)
```
Tell me about invoice INV-001
Show me invoice INV-123
What's the status of INV-001?
Give me details for invoice INV-001
```

## Revenue & Financial Queries

### Total Revenue
```
What's my total revenue?
How much have I earned?
Show me total income
What are my total earnings?
```

### Monthly Revenue
```
What's my total revenue this month?
How much did I make this month?
Show me this month's income
What's my revenue for the current month?
```

### Yearly Revenue
```
What's my total revenue this year?
How much have I earned this year?
Show me this year's income
What's my revenue for the current year?
```

### Outstanding Balance (NEW!)
```
What's the total amount we're still owed?
Show me outstanding balances
What's our total accounts receivable?
How much is outstanding?
What's our aging report?
```

### Payment History (NEW!)
```
Show me payments received this week
What payments did I receive this month?
Show me payments this year
List recent payments
How much was paid this month?
```

### Invoice Statistics (NEW!)
```
What's my average invoice amount?
Show me invoice statistics
Give me invoice analytics
What are my invoice stats this month?
What's the average days to payment?
```

## Customer Queries

### General Customer Information
```
How many customers do I have?
Tell me about my customers
Show me my client list
How many active customers do I have?
List my customers
```

### Customer-Specific Invoices (NEW!)
```
Show me all invoices for Acme Corp
List invoices for TechStart Inc
What invoices does Microsoft have?
Show me invoices for john@example.com
Give me invoices from Acme Corp
```

### Customer Summary (NEW!)
```
Give me a summary for Acme Corp
Tell me about TechStart Inc
Show me Microsoft's account status
What's the summary for john@example.com?
Give me details about Acme Corp
```

## Help & Instructions

### General Help
```
What can you do?
Help
Show me what you can help with
What features do you have?
```

### Creating Invoices
```
How do I create an invoice?
Create a new invoice
Help me create an invoice for a customer
How to make a new invoice?
```

### Sending Reminders
```
How do I send a reminder?
Send a reminder for an invoice
Help me send a payment reminder
How to remind customers about payment?
```

## Tips for Best Results

1. **Be Natural:** The AI understands natural language, so you can ask questions conversationally
2. **Use Keywords:** Include words like "invoice", "customer", "revenue", "overdue", etc.
3. **Time Periods:** Specify "this month" or "this year" for time-based queries
4. **Follow Suggestions:** Click on the suggestion chips for quick follow-up questions
5. **Be Specific:** More specific questions often get better answers

## Sample Conversation Flow

```
You: Hello!
AI: [Shows welcome message with capabilities]

You: How many overdue invoices do I have?
AI: You have 3 overdue invoices with a total outstanding balance of $4,500.00.
    [Lists invoices with details]

    Suggestions:
    - Show me all sent invoices
    - What's my total revenue?
    - How many draft invoices do I have?

You: What's the total amount we're still owed?
AI: Total outstanding balance: $15,250.00 across 8 invoices.

    Aging Breakdown:
    - Current (not yet due): 3 invoices - $7,500.00
    - 0-7 days overdue: 2 invoices - $3,000.00
    - 7-14 days overdue: 2 invoices - $3,250.00
    - 30+ days overdue: 1 invoice - $1,500.00

You: Show me all invoices for Acme Corp
AI: Invoices for Acme Corp (acme@example.com):

    Total Invoices: 12
    - Draft: 1
    - Sent: 3
    - Paid: 8
    - Cancelled: 0

    Financial Summary:
    - Total Invoiced: $25,400.00
    - Total Paid: $18,900.00
    - Outstanding Balance: $5,500.00

You: Give me a summary for Acme Corp
AI: Customer Summary: Acme Corp

    Contact Information:
    - Email: acme@example.com
    - Phone: (555) 123-4567
    - Status: Active

    Invoice Summary:
    - Total Invoices: 12
    - Overdue Invoices: 1
    - Total Invoiced: $25,400.00
    - Total Paid: $18,900.00
    - Outstanding Balance: $5,500.00

    Recent Activity:
    - Latest Invoice: INV-123 on 2025-11-01
    - Latest Payment: $2,500.00 on 2025-11-05

You: What's my average invoice amount?
AI: Invoice Statistics (all time):

    Overview:
    - Total Invoices: 45
    - Paid Invoices: 32
    - Payment Rate: 84.2%

    Financial Metrics:
    - Average Invoice Amount: $3,250.00
    - Total Value: $146,250.00

    Performance:
    - Average Days to Payment: 18.5 days

You: Tell me about invoice INV-001
AI: Invoice Details: INV-001

    Customer:
    - Acme Corp
    - acme@example.com

    Financial Information:
    - Total Amount: $2,500.00
    - Amount Paid: $1,000.00
    - Balance Remaining: $1,500.00

    Status: SENT (OVERDUE by 15 days)
    - Issue Date: 2025-10-01
    - Due Date: 2025-10-15

    Payment History:
    - $1,000.00 on 2025-10-20 (CREDIT_CARD)

You: Show me payments received this month
AI: You received 8 payments totaling $12,450.00 this month.

    Recent payments:
    - INV-123: $2,500.00 on 2025-11-05
    - INV-118: $1,800.00 on 2025-11-03
    - INV-115: $3,200.00 on 2025-11-01
    [... more payments]
```

## Pattern Matching Reference

The AI uses pattern matching to understand your queries. Here are the key patterns:

| Pattern | Keywords |
|---------|----------|
| Invoice Lookup | invoice INV-xxx, tell me about INV-xxx |
| Customer Invoices | invoices for [customer name] |
| Customer Summary | summary for [customer name] |
| Payment History | payment, paid this week/month, received |
| Outstanding Balance | total owed, outstanding balance, receivable |
| Statistics | average, statistic, analytics, stats |
| Overdue | overdue, past due, late |
| Revenue | revenue, income, earnings |
| Paid Invoices | paid invoice |
| Draft | draft |
| Sent | sent, pending, outstanding |
| Customer | customer, client |
| Status | status, count, how many |
| Create | create invoice, new invoice |
| Reminder | send reminder, reminder |
| Help | help, what can you do |

## New Features Summary

The AI Chat Assistant has been enhanced with comprehensive accountant capabilities:

### ✅ Invoice Queries
- Overdue, draft, sent, and paid invoices
- Status summaries and breakdowns
- **Specific invoice lookup by invoice number**
- **Invoice statistics and averages**

### ✅ Financial Information
- Revenue calculations (total, monthly, yearly)
- **Outstanding balance with aging breakdown**
- **Payment history (weekly, monthly, yearly)**

### ✅ Customer Management
- Customer counts and lists
- **Customer-specific invoice queries**
- **Comprehensive customer summaries with financial metrics**

### ✅ Analytics
- **Average invoice amounts**
- **Average days to payment**
- **Payment rates and trends**

## Limitations

The AI Chat Assistant currently:
- Does **NOT** execute actions (create invoices, send emails, etc.)
- Does **NOT** modify data - it only provides information and instructions
- Uses template-based pattern matching, not advanced AI language models
- Works best with the predefined query patterns listed above

For unsupported queries, the AI will provide helpful suggestions for what it can do.

## Need More Help?

If you have a question the AI can't answer, you can:
1. Try rephrasing your question using different keywords
2. Ask "What can you do?" to see all capabilities
3. Use the main InvoiceMe interface for direct actions
4. Consult the user documentation

---

**Remember:** The AI Chat Assistant is designed to help you quickly get information about your invoices and customers. For creating, editing, or sending invoices, use the main application interface.
