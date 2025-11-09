# InvoiceMe Frontend

A comprehensive invoice management system built with Next.js 14, TypeScript, and Tailwind CSS.

## Features

### Complete Application Suite
- **Authentication:** Basic Auth with protected routes
- **Dashboard:** Metrics, recent activity, overdue alerts
- **Customer Management:** Full CRUD with search/filter
- **Invoice Management:** Complete lifecycle (Draft → Sent → Paid)
- **Payment Processing:** Record payments, public payment portal
- **Reminder System:** Automated reminders for overdue invoices

### Key Capabilities
- Dynamic invoice line items with auto-calculation
- Partial payment support
- Unique payment links for customers
- Multi-type reminder emails with preview
- Responsive design (mobile, tablet, desktop)
- Professional UI with Tailwind CSS

## Quick Start

### Prerequisites
- Node.js 18+ or Bun
- Backend API running on http://localhost:8080

### Installation

```bash
# Install dependencies
bun install
# or npm install

# Create environment file
cp .env.local.example .env.local

# Start development server
bun run dev
# or npm run dev
```

Access at: **http://localhost:3000**

### Default Login
- Username: `demo`
- Password: `password`

## Project Structure

```
frontend/
├── src/
│   ├── app/                      # Next.js pages
│   │   ├── customers/           # Customer pages
│   │   ├── invoices/            # Invoice pages
│   │   ├── payments/            # Payment pages
│   │   ├── reminders/           # Reminder pages
│   │   ├── public/payment/      # Public portal (no auth)
│   │   ├── dashboard/           # Dashboard
│   │   └── login/               # Login
│   ├── components/              # React components
│   │   ├── ui/                  # Reusable UI (Button, Input, etc.)
│   │   ├── customers/           # Customer forms
│   │   ├── invoices/            # Invoice forms & modals
│   │   ├── Nav/                 # Sidebar & Header
│   │   └── AppLayout.tsx        # Main layout wrapper
│   ├── lib/
│   │   ├── api/                 # API client services
│   │   ├── format.ts            # Formatting utilities
│   │   └── validation.ts        # Zod schemas
│   ├── store/                   # Zustand state management
│   ├── types/                   # TypeScript types
│   └── contexts/                # React contexts
└── package.json
```

## Technology Stack

- **Framework:** Next.js 14 (App Router)
- **Language:** TypeScript (strict mode)
- **Styling:** Tailwind CSS
- **HTTP:** Axios with interceptors
- **State:** Zustand
- **Forms:** React Hook Form + Zod
- **Dates:** date-fns

## Pages & Routes

### Authenticated Routes
- `/dashboard` - Overview with metrics
- `/customers` - Customer list
- `/customers/new` - Create customer
- `/customers/[id]` - Customer details
- `/customers/[id]/edit` - Edit customer
- `/invoices` - Invoice list with filters
- `/invoices/new` - Create invoice
- `/invoices/[id]` - Invoice details with actions
- `/invoices/[id]/edit` - Edit draft invoice
- `/payments` - Payment list
- `/payments/[id]` - Payment details
- `/reminders` - Overdue invoices

### Public Routes
- `/login` - Login page
- `/public/payment/[link]` - Public payment portal

## API Integration

Backend: `http://localhost:8080/api`

### Endpoints
- `GET/POST/PUT/DELETE /customers`
- `GET/POST/PUT/DELETE /invoices`
- `POST /invoices/{id}/send`
- `POST /invoices/{id}/mark-paid`
- `POST /invoices/{id}/cancel`
- `GET/POST /payments`
- `GET/POST /reminders/*`

### Authentication
All authenticated requests include Basic Auth headers via Axios interceptor.

## Key Features Detail

### Invoice Workflow
1. Create as DRAFT with line items
2. Send to customer (status → SENT, generates payment link)
3. Customer pays via public link or manual entry
4. Record payment (full or partial)
5. Mark as PAID or CANCELLED

### Payment Portal
Customers receive unique link to:
- View invoice details
- See line items and totals
- Submit payment without login
- Get instant confirmation

### Reminder System
- Automatic overdue detection
- 5 reminder types (Before Due, On Due, 7/14/30 Days Overdue)
- Email preview before sending
- Reminder history tracking
- Severity indicators

## Development

### Run Development Server
```bash
bun run dev
```

### Build for Production
```bash
bun run build
bun run start
```

### Linting
```bash
bun run lint
```

## Environment Variables

Create `.env.local`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## UI Components

All in `src/components/ui/`:
- **Button** - Variants, sizes, loading states
- **Input** - Validation, errors, labels
- **Select** - Dropdown with validation
- **Card** - Container with title/actions
- **Table** - Generic data table
- **Badge** - Status indicators
- **Modal** - Dialog overlays
- **Loading** - Spinner with text

## State Management

### Zustand Stores
- `authStore` - Authentication state
- `customerStore` - Customer cache
- `invoiceStore` - Invoice cache

### Local Storage
- Auth credentials (Base64)
- User session info

## Form Validation

Zod schemas in `src/lib/validation.ts`:
- Customer, Invoice, Payment, Reminder schemas
- Real-time validation
- Type-safe error messages

## Styling

Tailwind CSS with professional theme:
- Primary: Blue (#2563eb)
- Success: Green (#10b981)
- Warning: Yellow/Orange (#f59e0b)
- Danger: Red (#ef4444)
- Neutral: Gray scale

Responsive breakpoints: sm (640px), md (768px), lg (1024px)

## Testing the Application

1. **Login:** Use demo/password
2. **Create Customer:** Add customer with billing address
3. **Create Invoice:** Select customer, add line items
4. **Send Invoice:** Changes status, generates payment link
5. **Record Payment:** Add payment via modal
6. **View Dashboard:** See metrics update
7. **Public Payment:** Use payment link (no login)
8. **Send Reminder:** For overdue invoices

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

Follow existing patterns:
- TypeScript strict mode
- Reusable components
- API services in `lib/api/`
- Forms with validation
- Error handling
- Loading states

## License

MIT
