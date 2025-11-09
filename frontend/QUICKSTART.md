# InvoiceMe - Quick Start Guide

## Prerequisites

1. **Backend API** must be running on `http://localhost:8080`
2. **Node.js 18+** or **Bun** installed

## Installation (2 minutes)

```bash
cd /Users/nanis/dev/Gauntlet/Invoice_AI/frontend

# Install dependencies
bun install

# Create environment file
cp .env.local.example .env.local

# Start the app
bun dev
```

## Access the Application

Open your browser to: **http://localhost:3000**

## Login

Use the default credentials:
- **Username:** `demo`
- **Password:** `password`

You'll be automatically redirected to the dashboard.

## Test the Complete Flow (5 minutes)

### 1. Create a Customer
1. Click "Customers" in the sidebar
2. Click "Add Customer" button
3. Fill in the form:
   - Business Name: "Acme Corp"
   - Contact Name: "John Doe"
   - Email: "john@acme.com"
   - Phone: "555-0123"
   - Billing Address:
     - Street: "123 Main St"
     - City: "San Francisco"
     - State: "CA"
     - Postal Code: "94102"
     - Country: "USA"
4. Click "Create Customer"

### 2. Create an Invoice
1. Click "Invoices" in the sidebar
2. Click "Create Invoice" button
3. Fill in the form:
   - Customer: Select "Acme Corp"
   - Issue Date: Today
   - Due Date: 30 days from today
   - Line Items:
     - Description: "Web Development"
     - Quantity: 40
     - Unit Price: 100
   - Click "Add Item" to add more if needed
4. Add optional notes/terms
5. Click "Create Invoice"

### 3. Send the Invoice
1. You'll be on the invoice detail page
2. Click "Send Invoice" button
3. Confirm the action
4. Status changes to "SENT"
5. Payment link is now available

### 4. Test Public Payment Portal
1. From the invoice page, click "Copy Payment Link"
2. Open a new **incognito/private** browser window
3. Paste the payment link
4. You'll see the invoice without logging in
5. Fill in the payment form:
   - Payment Amount: (shows full balance)
   - Payment Date: Today
   - Payment Method: "Bank Transfer"
   - Transaction Reference: "TXN-12345"
6. Click "Submit Payment"
7. See success confirmation

### 5. Verify Payment
1. Go back to your logged-in browser
2. Refresh the invoice page
3. See the payment in "Payment History"
4. Invoice status is now "PAID"
5. Balance remaining is $0.00

### 6. Test Reminders
1. Create another invoice with a due date in the past
2. Go to "Reminders" in the sidebar
3. See the overdue invoice listed
4. Click "Send Reminder"
5. Select reminder type (e.g., "7 Days Overdue")
6. Preview the email
7. Click "Send Reminder"
8. View reminder history on invoice page

## Feature Checklist

Use this to test all features:

### Dashboard
- [ ] View total revenue
- [ ] See invoice counts by status
- [ ] Check recent invoices table
- [ ] Check recent payments table
- [ ] View overdue alerts

### Customers
- [ ] List all customers
- [ ] Search customers
- [ ] Create new customer
- [ ] View customer details
- [ ] Edit customer
- [ ] Delete customer
- [ ] View customer's invoices

### Invoices
- [ ] List all invoices
- [ ] Filter by status
- [ ] Create new invoice
- [ ] Add/remove line items
- [ ] See auto-calculations
- [ ] Edit draft invoice
- [ ] Send invoice
- [ ] Copy payment link
- [ ] Record payment
- [ ] Send reminder
- [ ] Mark as paid
- [ ] Cancel invoice
- [ ] View payment history
- [ ] View reminder history

### Payments
- [ ] List all payments
- [ ] View payment details
- [ ] Navigate to invoice from payment

### Public Portal
- [ ] Access without login
- [ ] View invoice details
- [ ] See line items
- [ ] Submit payment
- [ ] Get confirmation

### Reminders
- [ ] View overdue invoices
- [ ] See severity indicators
- [ ] Preview reminder email
- [ ] Send reminder
- [ ] View statistics

## Common Issues

### Port Already in Use
```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9

# Or use a different port
PORT=3001 bun dev
```

### Backend Not Running
Make sure the Spring Boot backend is running on port 8080:
```bash
cd ../backend
./mvnw spring-boot:run
```

### Login Not Working
Check that:
1. Backend is accessible at http://localhost:8080
2. Using correct credentials: demo/password
3. No CORS errors in browser console

### Payment Link Not Working
1. Ensure invoice status is "SENT"
2. Copy the full link including the hash
3. Use an incognito window to test (no auth cookies)

## File Structure Reference

```
src/
├── app/                    # Pages
│   ├── dashboard/         # Dashboard
│   ├── customers/         # Customer pages
│   ├── invoices/          # Invoice pages
│   ├── payments/          # Payment pages
│   ├── reminders/         # Reminder pages
│   ├── public/payment/    # Public portal
│   └── login/             # Login
├── components/            # React components
│   ├── ui/               # Reusable UI
│   ├── customers/        # Customer forms
│   ├── invoices/         # Invoice forms
│   └── Nav/              # Navigation
├── lib/
│   ├── api/              # API clients
│   ├── format.ts         # Formatters
│   └── validation.ts     # Zod schemas
├── store/                # Zustand stores
├── types/                # TypeScript types
└── contexts/             # React contexts
```

## API Endpoints Used

All endpoints are prefixed with `http://localhost:8080/api`

- **GET** `/customers` - List customers
- **POST** `/customers` - Create customer
- **GET** `/customers/{id}` - Get customer
- **PUT** `/customers/{id}` - Update customer
- **DELETE** `/customers/{id}` - Delete customer

- **GET** `/invoices` - List invoices
- **POST** `/invoices` - Create invoice
- **GET** `/invoices/{id}` - Get invoice
- **PUT** `/invoices/{id}` - Update invoice
- **POST** `/invoices/{id}/send` - Send invoice
- **POST** `/invoices/{id}/mark-paid` - Mark as paid
- **POST** `/invoices/{id}/cancel` - Cancel invoice
- **GET** `/invoices/payment-link/{link}` - Get invoice by link

- **GET** `/payments` - List payments
- **POST** `/payments` - Record payment
- **GET** `/payments/{id}` - Get payment

- **GET** `/reminders/overdue` - Get overdue invoices
- **POST** `/reminders/send` - Send reminder
- **GET** `/reminders/history/{invoiceId}` - Get reminder history
- **GET** `/reminders/preview/{invoiceId}` - Preview reminder

## Development Tips

1. **Hot Reload:** Changes automatically refresh in browser
2. **Type Safety:** TypeScript catches errors before runtime
3. **Form Validation:** Zod validates all form inputs
4. **Error Handling:** Check browser console for API errors
5. **Loading States:** Watch for spinners during API calls

## Next Steps

After testing the basic flow, try:
1. Creating multiple customers
2. Creating invoices with multiple line items
3. Testing partial payments
4. Sending multiple reminders
5. Filtering invoices by status
6. Searching customers

## Need Help?

Check the main README.md for:
- Detailed feature documentation
- Architecture overview
- API integration details
- Troubleshooting guide

## Production Build

When ready to deploy:

```bash
# Build for production
bun run build

# Test production build locally
bun run start

# Access at http://localhost:3000
```

---

**Enjoy using InvoiceMe!** 🚀
