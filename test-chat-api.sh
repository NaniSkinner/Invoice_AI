#!/bin/bash

# Test script for InvoiceMe AI Chat Assistant API
# Usage: ./test-chat-api.sh

API_URL="${API_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-password}"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}InvoiceMe Chat API Test Suite${NC}"
echo -e "${BLUE}================================${NC}\n"

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

# Test 8: Create Invoice Help
test_query "How do I create an invoice?" "Create Invoice Help"

# Test 9: Reminder Help
test_query "How do I send a reminder?" "Reminder Help"

# Test 10: General Help
test_query "What can you do?" "General Help Query"

# Test 11: Unknown Query (should provide helpful fallback)
test_query "What's the weather like?" "Unknown Query Handling"

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}All tests completed!${NC}"
echo -e "${BLUE}================================${NC}"
