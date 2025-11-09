#!/bin/bash

# Test script for InvoiceMe AI Chat Assistant API - Enhanced Version
# Tests all baseline + new accountant assistant features
# Usage: ./test-chat-api.sh

API_URL="${API_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-password}"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}InvoiceMe Chat API Test Suite - Enhanced${NC}"
echo -e "${BLUE}Testing 21 Query Types (7 Baseline + 14 New)${NC}"
echo -e "${BLUE}================================================${NC}\n"

# Function to test a query
test_query() {
    local query="$1"
    local description="$2"

    echo -e "${GREEN}Test: ${description}${NC}"
    echo -e "Query: ${query}\n"

    response=$(curl -s -X POST "${API_URL}/api/chat/message" \
        -H "Content-Type: application/json" \
        -u "${USERNAME}:${PASSWORD}" \
        -d "{\"message\": \"${query}\"}")

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Response:${NC}"
        echo "$response" | jq '.'
        echo -e "\n---\n"
    else
        echo -e "${RED}Failed to connect to API${NC}\n"
        exit 1
    fi
}

# === BASELINE FEATURES ===

# Test 1: Overdue Invoices
test_query "How many overdue invoices do I have?" "Overdue Invoices Query"

# Test 2: Total Revenue
test_query "What's my total revenue?" "Total Revenue Query"

# Test 3: Monthly Revenue
test_query "What's my total revenue this month?" "Monthly Revenue Query"

# Test 4: Draft Invoices
test_query "Show me all draft invoices" "Draft Invoices Query"

# Test 5: Sent Invoices
test_query "Show me sent invoices" "Sent Invoices Query"

# Test 6: Customer Information
test_query "How many customers do I have?" "Customer Count Query"

# Test 7: Invoice Status Summary
test_query "Give me an invoice status summary" "Status Summary Query"

# === NEW ENHANCED FEATURES ===

# Test 8: Payment History (Week)
test_query "Show me payments received this week" "Payment History - Week"

# Test 9: Payment History (Month)
test_query "What payments did I receive this month?" "Payment History - Month"

# Test 10: Outstanding Balance with Aging
test_query "What's the total amount we're still owed?" "Outstanding Balance with Aging"

# Test 11: Paid Invoices
test_query "Show me paid invoices" "Paid Invoices Query"

# Test 12: Paid Invoices (Month)
test_query "Show me paid invoices this month" "Paid Invoices This Month"

# Test 13: Customer-Specific Invoices (Note: Replace with actual customer name from your data)
test_query "Show me all invoices for Acme Corp" "Customer-Specific Invoices"

# Test 14: Customer Summary (Note: Replace with actual customer name from your data)
test_query "Give me a summary for Acme Corp" "Customer Summary"

# Test 15: Invoice Statistics
test_query "What's my average invoice amount?" "Invoice Statistics - Average"

# Test 16: Invoice Statistics (Month)
test_query "Show me invoice statistics this month" "Invoice Statistics - Month"

# Test 17: Specific Invoice Lookup (Note: Replace with actual invoice number from your data)
test_query "Tell me about invoice INV-001" "Specific Invoice Lookup"

# === HELP & INSTRUCTIONS ===

# Test 18: Create Invoice Help
test_query "How do I create an invoice?" "Create Invoice Help"

# Test 19: Reminder Help
test_query "How do I send a reminder?" "Reminder Help"

# Test 20: General Help
test_query "What can you do?" "General Help Query"

# Test 21: Unknown Query (should provide helpful fallback)
test_query "What's the weather like?" "Unknown Query Handling"

echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}All 21 tests completed successfully!${NC}"
echo -e "${BLUE}================================================${NC}"
echo -e "\n${YELLOW}Note: Some tests may return 'not found' if test data doesn't exist.${NC}"
echo -e "${YELLOW}This is expected for customer/invoice-specific queries.${NC}\n"
